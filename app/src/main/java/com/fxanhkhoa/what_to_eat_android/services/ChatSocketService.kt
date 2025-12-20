package com.fxanhkhoa.what_to_eat_android.services

import android.content.Context
import android.util.Log
import com.fxanhkhoa.what_to_eat_android.model.ChatHistoryMessage
import com.fxanhkhoa.what_to_eat_android.model.ChatMessage
import com.fxanhkhoa.what_to_eat_android.model.ChatMessageType
import com.fxanhkhoa.what_to_eat_android.model.ChatRoom
import com.fxanhkhoa.what_to_eat_android.model.ChatRoomType
import com.fxanhkhoa.what_to_eat_android.model.ChatRoomUpdated
import com.fxanhkhoa.what_to_eat_android.model.ChatUser
import com.fxanhkhoa.what_to_eat_android.model.ChatUserInApp
import com.fxanhkhoa.what_to_eat_android.utils.TokenManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Timer
import kotlin.concurrent.schedule

/**
 * Service for handling chat and messaging real-time events via Socket.IO
 */
class ChatSocketService private constructor(private val context: Context) {

    companion object {
        private const val TAG = "ChatSocketService"

        @Volatile
        private var INSTANCE: ChatSocketService? = null

        fun getInstance(context: Context): ChatSocketService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ChatSocketService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // MARK: - Properties
    private val socketManager = SocketIOManager.getInstance(context)
    private val tokenManager = TokenManager.getInstance(context)
    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Published Properties
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _onlineUsers = MutableStateFlow<List<ChatUserInApp>>(emptyList())
    val onlineUsers: StateFlow<List<ChatUserInApp>> = _onlineUsers.asStateFlow()

    private val _typingUsers = MutableStateFlow<List<String>>(emptyList())
    val typingUsers: StateFlow<List<String>> = _typingUsers.asStateFlow()

    private val _newMessage = MutableStateFlow<ChatMessage?>(null)
    val newMessage: StateFlow<ChatMessage?> = _newMessage.asStateFlow()

    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _hasMoreMessages = MutableStateFlow(true)
    val hasMoreMessages: StateFlow<Boolean> = _hasMoreMessages.asStateFlow()

    // Private Properties
    private var currentChatRoom: String? = null
    private var typingTimer: Timer? = null
    private var isSubscriptionsSetup = false

    // MARK: - Computed Properties for User Info
    private val currentUserId: String
        get() {
            // Try to get from TokenManager synchronously or return anonymous
            return "user_${System.currentTimeMillis()}" // TODO: Get from auth service
        }

    private val currentUserName: String
        get() {
            // Try to get from TokenManager or return anonymous
            return "Anonymous" // TODO: Get from auth service
        }

    private val currentUserAvatar: String?
        get() {
            return null // TODO: Get from auth service
        }

    init {
        observeConnectionStatus()
    }

    // MARK: - Public Methods

    /**
     * Ensure socket connection is established
     */
    fun connectSocket() {
        if (!socketManager.isConnected.value) {
            socketManager.connect()
        }

        // Setup subscriptions if not already done and we're connected or connecting
        if (!isSubscriptionsSetup) {
            ensureConnectionAndSubscriptions()
        }
    }

    /**
     * Disconnect from the socket
     */
    fun disconnectSocket() {
        currentChatRoom?.let { leaveChatRoom(it) }
        isSubscriptionsSetup = false
    }

    /**
     * Join a chat room (e.g., for a specific vote game or general chat)
     */
    fun joinChatRoom(roomId: String, roomType: ChatRoomType = ChatRoomType.VOTE_GAME) {
        val roomName = "${roomType.value}$roomId"
        Log.i(TAG, "Joining chat room: $roomName as $currentUserName")

        // Ensure socket is connected and subscriptions are setup
        ensureConnectionAndSubscriptions()

        currentChatRoom?.let { leaveChatRoom(it) }

        currentChatRoom = roomName
        val joinData = JSONObject().apply {
            put("roomId", roomId)
            put("roomType", roomType.value)
            put("senderId", currentUserId)
            put("senderName", currentUserName)
            put("userAvatar", currentUserAvatar ?: "")
            put("timestamp", System.currentTimeMillis() / 1000.0)
        }

        socketManager.emit("join_chat_room", joinData)
        Log.i(TAG, "Joined chat room: $roomName as $currentUserName")
    }

    /**
     * Leave current chat room
     */
    fun leaveRoom(roomName: String? = null) {
        leaveChatRoom(roomName)
    }

    /**
     * Leave current chat room
     */
    fun leaveChatRoom(roomName: String? = null) {
        val room = roomName ?: currentChatRoom
        room ?: return

        val leaveData = JSONObject().apply {
            put("room", room)
            put("senderId", currentUserId)
        }

        socketManager.emit("leave_chat_room", leaveData)

        if (room == currentChatRoom) {
            currentChatRoom = null
            _messages.value = emptyList()
            _onlineUsers.value = emptyList()
            _typingUsers.value = emptyList()
        }

        Log.i(TAG, "Left chat room: $room")
    }

    /**
     * Send a message to current chat room
     */
    fun sendMessage(content: String, messageType: ChatMessageType = ChatMessageType.TEXT) {
        val roomName = currentChatRoom
        if (roomName == null) {
            Log.w(TAG, "Cannot send message - not in any chat room")
            return
        }

        val messageData = JSONObject().apply {
            put("content", content)
            put("type", messageType.value)
            put("room", roomName)
            put("senderId", currentUserId)
            put("senderName", currentUserName)
            put("senderAvatar", currentUserAvatar ?: "")
            put("timestamp", System.currentTimeMillis() / 1000.0)
        }

        socketManager.emit("send_message", messageData)
        Log.i(TAG, "Sent message to room: $roomName from $currentUserName")
    }

    /**
     * Send typing indicator
     */
    fun startTyping() {
        val roomName = currentChatRoom ?: return

        val typingData = JSONObject().apply {
            put("room", roomName)
            put("senderId", currentUserId)
            put("senderName", currentUserName)
        }

        socketManager.emit("typing_start", typingData)

        // Auto-stop typing after 3 seconds
        typingTimer?.cancel()
        typingTimer = Timer().apply {
            schedule(3000) {
                scope.launch {
                    stopTyping()
                }
            }
        }
    }

    /**
     * Stop typing indicator
     */
    fun stopTyping() {
        val roomName = currentChatRoom ?: return

        typingTimer?.cancel()

        val typingData = JSONObject().apply {
            put("room", roomName)
            put("senderId", currentUserId)
            put("senderName", currentUserName)
        }

        socketManager.emit("typing_stop", typingData)
    }

    /**
     * Load initial message history
     */
    fun loadMessageHistory(limit: Int = 50, loadMore: Boolean = false) {
        val roomName = currentChatRoom ?: return

        if (loadMore) {
            _isLoadingMore.value = true
        }

        val beforeTimestamp = if (loadMore) {
            _messages.value.firstOrNull()?.timestamp ?: (System.currentTimeMillis() / 1000.0).toLong()
        } else {
            (System.currentTimeMillis() / 1000.0).toLong()
        }

        val historyData = JSONObject().apply {
            put("room", roomName)
            put("limit", limit)
            put("before", beforeTimestamp)
        }

        Log.i(TAG, "Loading message history for room: $roomName, limit: $limit, loadMore: $loadMore")
        socketManager.emit("get_message_history", historyData)
    }

    /**
     * Load more older messages
     */
    fun loadMoreMessages() {
        if (!_hasMoreMessages.value || _isLoadingMore.value) return
        loadMessageHistory(limit = 20, loadMore = true)
    }

    /**
     * React to a message
     */
    fun reactToMessage(messageId: String, reaction: String) {
        val roomName = currentChatRoom ?: return

        val reactionData = JSONObject().apply {
            put("messageId", messageId)
            put("reaction", reaction)
            put("room", roomName)
            put("timestamp", System.currentTimeMillis() / 1000.0)
        }

        socketManager.emit("message_reaction", reactionData)
        Log.i(TAG, "Reacted to message: $messageId with $reaction")
    }

    /**
     * Clear all messages (useful when leaving a room)
     */
    fun clearMessages() {
        _messages.value = emptyList()
    }

    // MARK: - Private Methods

    private fun ensureConnectionAndSubscriptions() {
        // Connect socket if not connected
        if (!socketManager.isConnected.value) {
            socketManager.connect()
        }

        // Setup subscriptions if not already done
        if (!isSubscriptionsSetup) {
            setupSocketSubscriptions()
            isSubscriptionsSetup = true
        }
    }

    private fun setupSocketSubscriptions() {
        // Only setup if we have a connection or are connecting
        if (!socketManager.isConnected.value &&
            socketManager.connectionStatus.value != SocketConnectionStatus.CONNECTING) {
            Log.w(TAG, "Attempted to setup subscriptions without socket connection")
            return
        }

        Log.i(TAG, "Setting up chat socket subscriptions")

        // Subscribe to new messages
        socketManager.subscribe("message_received", handleMessageReceived)

        // Subscribe to message history
        socketManager.subscribe("message_history", handleMessageHistory)

        // Subscribe to user online/offline status
        socketManager.subscribe("user_joined_chat", handleUserJoinedChat)
        socketManager.subscribe("user_left_chat", handleUserLeftChat)

        // Subscribe to typing indicators
        socketManager.subscribe("user_typing_start", handleUserStartedTyping)
        socketManager.subscribe("user_typing_stop", handleUserStoppedTyping)

        // Subscribe to message reactions
        socketManager.subscribe("message_reaction_updated", handleMessageReactionUpdated)

        // Subscribe to room updates
        socketManager.subscribe("chat_room_updated", handleChatRoomUpdated)

        Log.i(TAG, "Chat socket subscriptions setup complete")
    }

    private fun observeConnectionStatus() {
        scope.launch {
            socketManager.isConnected.collect { isConnected ->
                if (isConnected) {
                    _connectionError.value = null
                    // Setup subscriptions when connected
                    if (!isSubscriptionsSetup) {
                        setupSocketSubscriptions()
                        isSubscriptionsSetup = true
                    }
                    rejoinChatRoomAfterReconnection()
                } else {
                    _connectionError.value = "Chat disconnected"
                    _onlineUsers.value = emptyList()
                    _typingUsers.value = emptyList()
                    // Reset subscriptions flag so they get re-setup on reconnection
                    isSubscriptionsSetup = false
                }
            }
        }

        scope.launch {
            socketManager.lastError.collect { error ->
                _connectionError.value = error
            }
        }
    }

    private fun rejoinChatRoomAfterReconnection() {
        val roomName = currentChatRoom ?: return

        // Extract room ID and type from room name
        val components = roomName.split("_", limit = 2)
        if (components.size != 2) return

        val roomType = ChatRoomType.fromValue(components[0]) ?: return
        val roomId = components[1]

        joinChatRoom(roomId, roomType)
        loadMessageHistory() // Reload recent messages

        Log.i(TAG, "Rejoined chat room after reconnection: $roomName")
    }

    // MARK: - Event Handlers

    private val handleMessageReceived = Emitter.Listener { args ->
        scope.launch {
            try {
                val data = args.getOrNull(0) as? org.json.JSONObject
                if (data == null) {
                    Log.e(TAG, "Invalid message data")
                    return@launch
                }

                // Convert JSONObject to JSON string then to ChatMessage
                val jsonString = data.toString()
                val message = gson.fromJson(jsonString, ChatMessage::class.java)

                val currentMessages = _messages.value.toMutableList()
                currentMessages.add(message)
                currentMessages.sortBy { it.timestamp }
                _messages.value = currentMessages

                _newMessage.value = message

                Log.i(TAG, "Received new message from: ${message.senderName}")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode message: ${e.message}", e)
            }
        }
    }

    private val handleMessageHistory = Emitter.Listener { args ->
        scope.launch {
            try {
                val data = args.getOrNull(0) as? org.json.JSONObject
                if (data == null) {
                    Log.e(TAG, "Invalid message history data")
                    _isLoadingMore.value = false
                    return@launch
                }

                val messagesArray = data.optJSONArray("messages")
                if (messagesArray == null) {
                    Log.e(TAG, "Messages array is null")
                    _isLoadingMore.value = false
                    return@launch
                }

                val currentCount = data.optInt("count", messagesArray.length())
                val hasMore = data.optBoolean("hasMore", currentCount >= 20)
                _hasMoreMessages.value = hasMore

                val jsonString = messagesArray.toString()
                val listType = object : TypeToken<List<ChatHistoryMessage>>() {}.type
                val historyMessages: List<ChatHistoryMessage> = gson.fromJson(jsonString, listType)

                Log.i(TAG, "History messages count: ${historyMessages.size}")

                // Convert ChatHistoryMessage to ChatMessage
                val convertedMessages = historyMessages.map { historyMsg ->
                    ChatMessage(
                        id = historyMsg.id,
                        content = historyMsg.content,
                        senderId = historyMsg.senderId,
                        senderName = historyMsg.senderName,
                        senderAvatar = historyMsg.senderAvatar,
                        type = historyMsg.type,
                        timestamp = historyMsg.timestamp,
                        reactions = historyMsg.reactions,
                        roomId = historyMsg.roomId,
                        createdAt = historyMsg.date.toString(),
                        updatedAt = historyMsg.date.toString(),
                        deleted = false
                    )
                }

                val currentMessages = _messages.value.toMutableList()

                if (_isLoadingMore.value) {
                    // For load more, prepend older messages and filter duplicates
                    val uniqueMessages = convertedMessages.filter { convertedMsg ->
                        currentMessages.none { it.id == convertedMsg.id }
                    }
                    currentMessages.addAll(0, uniqueMessages)
                } else {
                    // For initial load, replace all messages
                    currentMessages.clear()
                    currentMessages.addAll(convertedMessages)
                }

                currentMessages.sortBy { it.timestamp }
                _messages.value = currentMessages

                Log.i(TAG, "Loaded ${historyMessages.size} messages from history (hasMore: $hasMore)")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode message history: ${e.message}", e)
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    private val handleUserJoinedChat = Emitter.Listener { args ->
        scope.launch {
            try {
                val data = args.getOrNull(0) as? org.json.JSONObject
                if (data == null) {
                    Log.e(TAG, "Invalid user joined data")
                    return@launch
                }

                val jsonString = data.toString()
                val user = gson.fromJson(jsonString, ChatUser::class.java)

                val currentUsers = _onlineUsers.value.toMutableList()
                if (currentUsers.none { it.id == user.userId }) {
                    currentUsers.add(ChatUserInApp(id = user.userId, name = user.userName, isOnline = true))
                    _onlineUsers.value = currentUsers
                }

                Log.i(TAG, "User joined chat: ${user.userName}")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode user joined data: ${e.message}", e)
            }
        }
    }

    private val handleUserLeftChat = Emitter.Listener { args ->
        scope.launch {
            try {
                val data = args.getOrNull(0) as? org.json.JSONObject
                if (data == null) {
                    Log.e(TAG, "Invalid user left data")
                    return@launch
                }

                val userId = data.optString("userId")
                if (userId.isEmpty()) return@launch

                _onlineUsers.value = _onlineUsers.value.filter { it.id != userId }
                _typingUsers.value = _typingUsers.value.filter { it != userId }

                Log.i(TAG, "User left chat: $userId")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle user left: ${e.message}", e)
            }
        }
    }

    private val handleUserStartedTyping = Emitter.Listener { args ->
        scope.launch {
            try {
                val data = args.getOrNull(0) as? org.json.JSONObject
                if (data == null) {
                    Log.e(TAG, "Invalid typing start data")
                    return@launch
                }

                val userId = data.optString("userId")
                val userName = data.optString("userName")

                if (userId.isEmpty()) return@launch

                val currentTyping = _typingUsers.value.toMutableList()
                if (!currentTyping.contains(userId)) {
                    currentTyping.add(userId)
                    _typingUsers.value = currentTyping
                }

                Log.d(TAG, "User started typing: $userName")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle typing start: ${e.message}", e)
            }
        }
    }

    private val handleUserStoppedTyping = Emitter.Listener { args ->
        scope.launch {
            try {
                val data = args.getOrNull(0) as? org.json.JSONObject
                if (data == null) {
                    Log.e(TAG, "Invalid typing stop data")
                    return@launch
                }

                val userId = data.optString("userId")
                if (userId.isEmpty()) return@launch

                _typingUsers.value = _typingUsers.value.filter { it != userId }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle typing stop: ${e.message}", e)
            }
        }
    }

    private val handleMessageReactionUpdated = Emitter.Listener { args ->
        scope.launch {
            try {
                val data = args.getOrNull(0) as? org.json.JSONObject
                if (data == null) {
                    Log.e(TAG, "Invalid reaction update data")
                    return@launch
                }

                val messageId = data.optString("messageId")
                val reactionsObj = data.optJSONObject("reactions")

                if (messageId.isEmpty() || reactionsObj == null) return@launch

                val reactions = mutableMapOf<String, Int>()
                val keysIterator = reactionsObj.keys()
                while (keysIterator.hasNext()) {
                    val key = keysIterator.next() as String
                    reactions[key] = reactionsObj.optInt(key, 0)
                }

                // Update message reactions
                val currentMessages = _messages.value.toMutableList()
                val index = currentMessages.indexOfFirst { it.id == messageId }
                if (index != -1) {
                    val updatedMessage = currentMessages[index].copy(reactions = reactions)
                    currentMessages[index] = updatedMessage
                    _messages.value = currentMessages
                }

                Log.i(TAG, "Updated reactions for message: $messageId")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle reaction update: ${e.message}", e)
            }
        }
    }

    private val handleChatRoomUpdated = Emitter.Listener { args ->
        scope.launch {
            try {
                val data = args.getOrNull(0) as? org.json.JSONObject
                if (data == null) {
                    Log.e(TAG, "Invalid chat room update data")
                    return@launch
                }

                val jsonString = data.toString()
                val roomData = gson.fromJson(jsonString, ChatRoomUpdated::class.java)
                val newUserIds = roomData.onlineUsers.toSet()

                // Remove users who are no longer online
                val currentUsers = _onlineUsers.value.toMutableList()
                currentUsers.removeAll { !newUserIds.contains(it.id) }

                // Add new users with placeholder names
                for (userId in roomData.onlineUsers) {
                    if (currentUsers.none { it.id == userId }) {
                        currentUsers.add(ChatUserInApp(id = userId, name = "", isOnline = true))
                    }
                }

                _onlineUsers.value = currentUsers

                Log.i(TAG, "Chat room updated with ${newUserIds.size} online users")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle chat room update: ${e.message}", e)
            }
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        disconnectSocket()
        typingTimer?.cancel()
    }
}
