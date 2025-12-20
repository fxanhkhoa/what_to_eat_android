package com.fxanhkhoa.what_to_eat_android.services

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.fxanhkhoa.what_to_eat_android.utils.TokenManager
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URISyntaxException
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.math.pow

/**
 * Centralized Socket.IO manager for real-time communication
 */
class SocketIOManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "SocketIOManager"
        private const val BASE_URL = "https://api.eatwhat.io.vn/"
        private const val MAX_RECONNECT_ATTEMPTS = 5
        private const val RECONNECT_DELAY = 2000L // 2 seconds

        @Volatile
        private var INSTANCE: SocketIOManager? = null

        fun getInstance(context: Context): SocketIOManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SocketIOManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // MARK: - Properties
    private val tokenManager = TokenManager.getInstance(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Published Properties
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _connectionStatus = MutableStateFlow(SocketConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<SocketConnectionStatus> = _connectionStatus.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    // Private Properties
    private var socket: Socket? = null
    private var reconnectTimer: Timer? = null
    private var connectionAttempts = 0

    // Event publishers for reactive programming
    private val _eventFlow = MutableSharedFlow<SocketEvent>(replay = 0)
    val eventFlow: SharedFlow<SocketEvent> = _eventFlow.asSharedFlow()

    // Network monitoring
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var isNetworkAvailable = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            isNetworkAvailable = true
            Log.d(TAG, "Network available")
            scope.launch {
                if (isAuthenticated() && !_isConnected.value) {
                    connect()
                }
            }
        }

        override fun onLost(network: Network) {
            isNetworkAvailable = false
            Log.d(TAG, "Network lost")
            disconnect()
        }
    }

    init {
        setupNetworkMonitoring()
        Log.i(TAG, "SocketIOManager initialized")
    }

    // MARK: - Connection Management

    /**
     * Connect to the Socket.IO server
     */
    fun connect() {
        scope.launch {
            if (!isAuthenticated()) {
                Log.w(TAG, "Cannot connect to socket - user not authenticated")
                _lastError.value = "Authentication required"
                return@launch
            }

            if (!isNetworkAvailable) {
                Log.w(TAG, "Cannot connect to socket - no network connection")
                _lastError.value = "No network connection"
                return@launch
            }

            if (_isConnected.value) {
                Log.i(TAG, "Socket already connected")
                return@launch
            }

            setupSocket()
            socket?.connect()
            _connectionStatus.value = SocketConnectionStatus.CONNECTING
            Log.i(TAG, "Attempting to connect to Socket.IO server")
        }
    }

    /**
     * Disconnect from the Socket.IO server
     */
    fun disconnect() {
        socket?.disconnect()
        _connectionStatus.value = SocketConnectionStatus.DISCONNECTED
        cancelReconnectTimer()
        Log.i(TAG, "Disconnected from Socket.IO server")
    }

    /**
     * Force reconnect to the Socket.IO server
     */
    fun reconnect() {
        disconnect()
        scope.launch {
            kotlinx.coroutines.delay(1000)
            connect()
        }
    }

    // MARK: - Message Sending

    /**
     * Send a message to a specific event with JSON string data
     */
    fun emit(event: String, data: String) {
        if (!_isConnected.value) {
            Log.w(TAG, "Cannot emit event '$event' - socket not connected")
            _lastError.value = "Socket not connected"
            return
        }

        Log.d(TAG, "Emitting event: $event with JSON string data")
        socket?.emit(event, data)
        Log.d(TAG, "Emitted event: $event")
    }

    /**
     * Send a message to a specific event with JSONObject
     */
    fun emit(event: String, data: JSONObject) {
        if (!_isConnected.value) {
            Log.w(TAG, "Cannot emit event '$event' - socket not connected")
            _lastError.value = "Socket not connected"
            return
        }

        Log.d(TAG, "Emitting event: $event with data: $data")
        socket?.emit(event, data)
        Log.d(TAG, "Emitted event: $event")
    }

    /**
     * Send a message to a specific event with two JSONObjects
     */
    fun emit(event: String, data1: JSONObject, data2: JSONObject) {
        if (!_isConnected.value) {
            Log.w(TAG, "Cannot emit event '$event' - socket not connected")
            _lastError.value = "Socket not connected"
            return
        }

        Log.d(TAG, "Emitting event: $event with 2 JSON parameters")
        socket?.emit(event, data1, data2)
        Log.d(TAG, "Emitted event: $event")
    }

    /**
     * Send a message with acknowledgment
     */
    fun emitWithAck(
        event: String,
        data: JSONObject,
        callback: (Array<Any>) -> Unit
    ) {
        if (!_isConnected.value) {
            Log.w(TAG, "Cannot emit event '$event' with ack - socket not connected")
            _lastError.value = "Socket not connected"
            return
        }

        socket?.emit(event, data, io.socket.client.Ack { args ->
            callback(args)
        })
    }

    // MARK: - Event Subscription

    /**
     * Subscribe to a socket event
     */
    fun subscribe(event: String, listener: Emitter.Listener) {
        Log.d(TAG, "Subscribing to event: $event")

        if (socket == null) {
            Log.w(TAG, "Socket is not initialized. Call connect() first.")
            return
        }

        socket?.on(event, listener)
    }

    /**
     * Subscribe to a socket event with Flow
     */
    fun subscribeToEvent(event: String): SharedFlow<SocketEvent> {
        Log.d(TAG, "Subscribing to event with Flow: $event")

        if (socket == null) {
            Log.w(TAG, "Socket is not initialized. Call connect() first.")
        } else {
            socket?.on(event) { args ->
                scope.launch {
                    val socketEvent = SocketEvent(
                        name = event,
                        data = args.toList(),
                        timestamp = System.currentTimeMillis()
                    )
                    _eventFlow.emit(socketEvent)
                    Log.d(TAG, "Received event: $event")
                }
            }
        }

        return eventFlow
    }

    /**
     * Unsubscribe from a socket event
     */
    fun unsubscribe(event: String) {
        socket?.off(event)
        Log.d(TAG, "Unsubscribed from event: $event")
    }

    /**
     * Join a room
     */
    fun joinRoom(room: String, data: Map<String, Any> = emptyMap()) {
        val joinData = JSONObject(data.toMutableMap().apply {
            put("roomID", room)
        })
        emit("join-room", joinData)
        Log.i(TAG, "Joining room: $room")
    }

    /**
     * Leave a room
     */
    fun leaveRoom(room: String, data: Map<String, Any> = emptyMap()) {
        val leaveData = JSONObject(data.toMutableMap().apply {
            put("room", room)
        })
        emit("leave-room", leaveData)
        Log.i(TAG, "Leaving room: $room")
    }

    // MARK: - Private Methods

    private suspend fun setupSocket() {
        try {
            val opts = IO.Options().apply {
                reconnection = true
                reconnectionAttempts = MAX_RECONNECT_ATTEMPTS
                reconnectionDelay = RECONNECT_DELAY
                forceNew = false
                multiplex = true

                // Add authentication headers if available
                tokenManager.getAccessToken()?.let { token ->
                    val headers = mutableMapOf<String, List<String>>()
                    headers["Authorization"] = listOf("Bearer $token")
                    extraHeaders = headers
                }
            }

            socket = IO.socket(BASE_URL, opts)
            setupSocketHandlers()

        } catch (e: URISyntaxException) {
            Log.e(TAG, "Invalid Socket.IO URL", e)
            _lastError.value = "Invalid server URL"
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up socket", e)
            _lastError.value = "Socket setup failed: ${e.message}"
        }
    }

    private fun setupSocketHandlers() {
        socket?.apply {
            // Connection events
            on(Socket.EVENT_CONNECT) {
                scope.launch {
                    handleConnected()
                }
            }

            on(Socket.EVENT_DISCONNECT) { args ->
                scope.launch {
                    val reason = args.getOrNull(0) as? String
                    handleDisconnected(reason)
                }
            }

            on(Socket.EVENT_CONNECT_ERROR) { args ->
                scope.launch {
                    handleError(args.toList())
                }
            }

            // Authentication events
            on("auth_error") { args ->
                scope.launch {
                    handleAuthError(args.toList())
                }
            }

            on("auth_success") {
                scope.launch {
                    handleAuthSuccess()
                }
            }
        }
    }

    private suspend fun handleConnected() {
        _isConnected.value = true
        _connectionStatus.value = SocketConnectionStatus.CONNECTED
        connectionAttempts = 0
        _lastError.value = null
        cancelReconnectTimer()
        Log.i(TAG, "Successfully connected to Socket.IO server")

        // Emit authentication if needed
        tokenManager.getAccessToken()?.let { token ->
            val authData = JSONObject().apply {
                put("token", token)
            }
            emit("authenticate", authData)
        }
    }

    private fun handleDisconnected(reason: String?) {
        _isConnected.value = false
        _connectionStatus.value = SocketConnectionStatus.DISCONNECTED
        val disconnectReason = reason ?: "Unknown reason"
        Log.i(TAG, "Disconnected from Socket.IO server. Reason: $disconnectReason")

        // Handle reconnection based on reason
        if (disconnectReason != "io client disconnect" && isNetworkAvailable) {
            scheduleReconnect()
        }
    }

    private fun handleError(data: List<Any>) {
        val errorMessage = data.firstOrNull()?.toString() ?: "Unknown socket error"
        _lastError.value = errorMessage
        Log.e(TAG, "Socket error: $errorMessage")
    }

    private fun handleReconnected() {
        Log.i(TAG, "Successfully reconnected to Socket.IO server")
        connectionAttempts = 0
    }

    private fun handleReconnectAttempt(attempt: Int) {
        connectionAttempts = attempt
        Log.i(TAG, "Reconnection attempt #$attempt")
    }

    private fun handleAuthError(data: List<Any>) {
        val errorMessage = data.firstOrNull()?.toString() ?: "Authentication failed"
        _lastError.value = errorMessage
        Log.e(TAG, "Socket authentication error: $errorMessage")

        // Clear tokens if authentication fails
        scope.launch {
            tokenManager.clearTokens()
        }
        disconnect()
    }

    private fun handleAuthSuccess() {
        Log.i(TAG, "Socket authentication successful")
        _lastError.value = null
    }

    private fun scheduleReconnect() {
        if (connectionAttempts >= MAX_RECONNECT_ATTEMPTS) {
            Log.e(TAG, "Max reconnection attempts reached")
            _lastError.value = "Max reconnection attempts reached"
            return
        }

        cancelReconnectTimer()

        val delay = (RECONNECT_DELAY * 2.0.pow(connectionAttempts.toDouble())).toLong()
        Log.i(TAG, "Scheduling reconnect in ${delay}ms")

        reconnectTimer = Timer().apply {
            schedule(delay) {
                scope.launch {
                    connect()
                }
            }
        }
    }

    private fun cancelReconnectTimer() {
        reconnectTimer?.cancel()
        reconnectTimer = null
    }

    private suspend fun isAuthenticated(): Boolean {
        return tokenManager.getAccessToken() != null
    }

    private fun setupNetworkMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // Check initial network state
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        isNetworkAvailable = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        disconnect()
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Log.w(TAG, "Error unregistering network callback", e)
        }
        socket?.off()
        socket = null
    }
}

// MARK: - Supporting Types

/**
 * Socket connection status
 */
enum class SocketConnectionStatus(val description: String) {
    DISCONNECTED("Disconnected"),
    CONNECTING("Connecting"),
    CONNECTED("Connected"),
    RECONNECTING("Reconnecting")
}

/**
 * Socket event model
 */
data class SocketEvent(
    val name: String,
    val data: List<Any>,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Socket errors
 */
sealed class SocketError(override val message: String) : Exception(message) {
    object NotConnected : SocketError("Socket is not connected")
    object AuthenticationRequired : SocketError("Authentication required for socket connection")
    object Timeout : SocketError("Socket operation timed out")
    object InvalidData : SocketError("Invalid data received from socket")
}
