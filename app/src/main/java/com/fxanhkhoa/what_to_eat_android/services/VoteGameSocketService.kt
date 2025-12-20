package com.fxanhkhoa.what_to_eat_android.services

import android.content.Context
import android.util.Log
import com.fxanhkhoa.what_to_eat_android.model.DishVoteModel
import com.fxanhkhoa.what_to_eat_android.model.VoteDishDto
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

/**
 * Service for handling vote game real-time events via Socket.IO
 */
class VoteGameSocketService private constructor(private val context: Context) {

    companion object {
        private const val TAG = "VoteGameSocketService"

        @Volatile
        private var INSTANCE: VoteGameSocketService? = null

        fun getInstance(context: Context): VoteGameSocketService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VoteGameSocketService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // MARK: - Properties
    private val socketManager = SocketIOManager.getInstance(context)
    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Published Properties
    private val _activeVoteGames = MutableStateFlow<List<DishVoteModel>>(emptyList())
    val activeVoteGames: StateFlow<List<DishVoteModel>> = _activeVoteGames.asStateFlow()

    private val _voteUpdates = MutableStateFlow<List<VoteUpdate>>(emptyList())
    val voteUpdates: StateFlow<List<VoteUpdate>> = _voteUpdates.asStateFlow()

    private val _newVoteGame = MutableStateFlow<DishVoteModel?>(null)
    val newVoteGame: StateFlow<DishVoteModel?> = _newVoteGame.asStateFlow()

    private val _voteGameDeleted = MutableStateFlow<String?>(null)
    val voteGameDeleted: StateFlow<String?> = _voteGameDeleted.asStateFlow()

    private val _dishVoteUpdate = MutableStateFlow<DishVoteModel?>(null)
    val dishVoteUpdate: StateFlow<DishVoteModel?> = _dishVoteUpdate.asStateFlow()

    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError.asStateFlow()

    // Private Properties
    private val subscribedRooms = mutableSetOf<String>()
    private var isSubscriptionsSetup = false

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
     * Subscribe to a specific vote game room for real-time updates
     */
    fun subscribeToVoteGame(voteGameId: String) {
        val roomName = voteGameId

        if (subscribedRooms.contains(roomName)) {
            Log.i(TAG, "Already subscribed to vote game: $voteGameId")
            return
        }

        Log.i(TAG, "========================================")
        Log.i(TAG, "SUBSCRIBING TO VOTE GAME: $voteGameId")
        Log.i(TAG, "Room name: $roomName")
        Log.i(TAG, "Is connected: ${socketManager.isConnected.value}")
        Log.i(TAG, "Subscriptions setup: $isSubscriptionsSetup")
        Log.i(TAG, "========================================")

        // Ensure socket is connected and subscriptions are setup
        ensureConnectionAndSubscriptions()

        socketManager.joinRoom(roomName, mapOf("roomID" to voteGameId))
        subscribedRooms.add(roomName)

        Log.i(TAG, "Emitted join-room event for vote game: $voteGameId")
        Log.i(TAG, "Subscribed to vote game room: $roomName")
        Log.i(TAG, "Total subscribed rooms: ${subscribedRooms.size}")
    }

    /**
     * Unsubscribe from a specific vote game room
     */
    fun unsubscribeFromVoteGame(voteGameId: String) {
        val roomName = voteGameId

        if (!subscribedRooms.contains(roomName)) {
            Log.i(TAG, "Not subscribed to vote game: $voteGameId")
            return
        }

        socketManager.leaveRoom(roomName, mapOf("roomID" to voteGameId))
        subscribedRooms.remove(roomName)
        Log.i(TAG, "Unsubscribed from vote game room: $roomName")
    }

    /**
     * Subscribe to general vote game events (new games, deletions, etc.)
     */
    fun subscribeToGeneralVoteGameEvents() {
        val roomName = "vote_games_general"

        if (subscribedRooms.contains(roomName)) {
            Log.i(TAG, "Already subscribed to general vote game events")
            return
        }

        // Ensure socket is connected and subscriptions are setup
        ensureConnectionAndSubscriptions()

        socketManager.joinRoom(roomName)
        subscribedRooms.add(roomName)
        Log.i(TAG, "Subscribed to general vote game events")
    }

    /**
     * Unsubscribe from all vote game rooms
     */
    fun unsubscribeFromAll() {
        for (room in subscribedRooms) {
            socketManager.leaveRoom(room)
        }
        subscribedRooms.clear()
        Log.i(TAG, "Unsubscribed from all vote game rooms")
    }

    /**
     * Submit a vote for a dish
     */
    fun submitVote(voteData: VoteDishDto, options: VoteOptions) {
        Log.i(TAG, "Submitting vote for dish: ${voteData.slug} in vote game: ${options.roomID}")

        try {
            // Create the vote data JSON with the required structure
            val voteDataJson = JSONObject().apply {
                put("slug", voteData.slug)
                put("myName", voteData.userID)
                put("userID", voteData.userID)
                put("isVoting", voteData.isVoting)
            }

            val optionsJson = JSONObject(gson.toJson(options))

            socketManager.emit("dish-vote-update", voteDataJson, optionsJson)
            Log.i(TAG, "Successfully submitted vote for dish: ${voteData.slug} in vote game: ${options.roomID}")
            Log.i(TAG, "Vote data: $voteDataJson")
            Log.i(TAG, "Options data: $optionsJson")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert vote data to JSON: ${e.message}", e)
        }
    }

    /**
     * Create a new vote game and emit to all subscribers
     */
    fun createVoteGame(voteGame: DishVoteModel) {
        try {
            val voteGameJson = gson.toJson(voteGame)
            socketManager.emit("create_vote_game", voteGameJson)
            Log.i(TAG, "Created new vote game: ${voteGame.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert vote game to JSON: ${e.message}", e)
        }
    }

    /**
     * Delete a vote game and notify all subscribers
     */
    fun deleteVoteGame(voteGameId: String) {
        val deleteData = DeleteVoteGameData(
            voteGameId = voteGameId,
            timestamp = System.currentTimeMillis() / 1000.0
        )

        try {
            val deleteDataJson = gson.toJson(deleteData)
            socketManager.emit("delete_vote_game", deleteDataJson)
            Log.i(TAG, "Deleted vote game: $voteGameId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert delete data to JSON: ${e.message}", e)
        }
    }

    /**
     * Get current connection status for debugging
     */
    fun getDebugInfo(): Map<String, Any> {
        return mapOf(
            "isConnected" to socketManager.isConnected.value,
            "connectionStatus" to socketManager.connectionStatus.value.description,
            "subscribedRooms" to subscribedRooms.toList(),
            "lastError" to (socketManager.lastError.value ?: "None"),
            "voteUpdatesCount" to _voteUpdates.value.size,
            "activeVoteGamesCount" to _activeVoteGames.value.size
        )
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

        Log.i(TAG, "Setting up socket subscriptions for vote game events")

        // Subscribe to vote updates - this is the main event for real-time vote updates
        socketManager.subscribe("dish-vote-update-client", handleVoteUpdate)
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
                    rejoinRoomsAfterReconnection()
                } else {
                    _connectionError.value = "Socket disconnected"
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

    private fun rejoinRoomsAfterReconnection() {
        // Rejoin all previously subscribed rooms after reconnection
        val roomsToRejoin = subscribedRooms.toList()
        subscribedRooms.clear()

        for (room in roomsToRejoin) {
            if (room == "vote_games_general") {
                subscribeToGeneralVoteGameEvents()
            } else {
                // Assume it's a vote game ID
                subscribeToVoteGame(room)
            }
        }

        Log.i(TAG, "Rejoined ${roomsToRejoin.size} rooms after reconnection")
    }

    // MARK: - Event Handlers

    private val handleVoteUpdate = Emitter.Listener { args ->
        scope.launch {
            try {
                Log.i(TAG, "========================================")
                Log.i(TAG, "VOTE UPDATE EVENT RECEIVED!")
                Log.i(TAG, "Raw vote update event received with ${args.size} items")

                // Log all arguments for debugging
                args.forEachIndexed { index, arg ->
                    Log.i(TAG, "Arg[$index] type: ${arg?.javaClass?.simpleName}, value: $arg")
                }

                val data = args.getOrNull(0) as? org.json.JSONObject
                if (data == null) {
                    Log.e(TAG, "Invalid vote update data - first argument is not a JSONObject")
                    Log.e(TAG, "First arg type: ${args.getOrNull(0)?.javaClass?.simpleName}")
                    return@launch
                }

                Log.i(TAG, "Received vote update as JSONObject: $data")
                Log.i(TAG, "========================================")
                handleVoteUpdateFromDictionary(data)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to process vote update: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }

    private fun handleVoteUpdateFromDictionary(data: org.json.JSONObject) {
        try {
            val jsonString = data.toString()

            // Check which type of data we received by looking for key fields
            val hasDishVoteItems = data.has("dishVoteItems")
            val hasTitle = data.has("title")
            val hasVoteGameId = data.has("voteGameId")
            val hasDishSlug = data.has("dishSlug")

            Log.d(TAG, "Received data fields: hasDishVoteItems=$hasDishVoteItems, hasTitle=$hasTitle, hasVoteGameId=$hasVoteGameId, hasDishSlug=$hasDishSlug")

            // Try DishVoteModel first (most common - sent when votes change)
            if (hasDishVoteItems || hasTitle || data.has("_id")) {
                try {
                    val dishVote = gson.fromJson(jsonString, DishVoteModel::class.java)

                    // Validate we got valid data (check for non-empty required fields)
                    if (dishVote.id.isNotEmpty() && dishVote.title.isNotEmpty()) {
                        val currentGames = _activeVoteGames.value.toMutableList()
                        val index = currentGames.indexOfFirst { it.id == dishVote.id }

                        if (index != -1) {
                            currentGames[index] = dishVote
                            Log.i(TAG, "Successfully updated DishVoteModel: ${dishVote.id}")
                        } else {
                            currentGames.add(dishVote)
                            Log.i(TAG, "Added new DishVoteModel: ${dishVote.id}")
                        }

                        _activeVoteGames.value = currentGames
                        _newVoteGame.value = dishVote
                        _dishVoteUpdate.value = dishVote // Emit the update for real-time subscriptions

                        Log.i(TAG, "Vote game '${dishVote.title}' has ${dishVote.dishVoteItems.size} items")
                        dishVote.dishVoteItems.forEach { item ->
                            val totalVotes = item.voteUser.size + item.voteAnonymous.size
                            Log.d(TAG, "  - ${item.slug}: $totalVotes votes (${item.voteUser.size} user, ${item.voteAnonymous.size} anon)")
                        }
                        return
                    } else {
                        Log.w(TAG, "Decoded DishVoteModel but got empty id or title (id='${dishVote.id}', title='${dishVote.title}')")
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Not a DishVoteModel: ${e.message}")
                }
            }

            // Try VoteUpdate (less common - individual vote events)
            if (hasVoteGameId || hasDishSlug) {
                try {
                    val voteUpdate = gson.fromJson(jsonString, VoteUpdate::class.java)

                    // Validate we got valid data (check for non-empty required fields)
                    if (voteUpdate.voteGameId.isNotEmpty() && voteUpdate.dishSlug.isNotEmpty()) {
                        val currentUpdates = _voteUpdates.value.toMutableList()
                        currentUpdates.add(voteUpdate)
                        _voteUpdates.value = currentUpdates
                        Log.i(TAG, "Successfully decoded as VoteUpdate: $voteUpdate")
                        return
                    } else {
                        Log.w(TAG, "Decoded VoteUpdate but got empty fields (voteGameId='${voteUpdate.voteGameId}', dishSlug='${voteUpdate.dishSlug}')")
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Not a VoteUpdate: ${e.message}")
                }
            }

            // If both fail, create a manual VoteUpdate from the raw data
            val voteGameId = data.optString("voteGameId").ifEmpty {
                data.optString("id")
            }
            val dishSlug = data.optString("dishSlug").ifEmpty {
                data.optString("slug")
            }

            if (voteGameId.isNotEmpty() && dishSlug.isNotEmpty()) {
                val voteCount = data.optInt("voteCount", data.optInt("votes", 0))
                val manualUpdate = VoteUpdate(
                    voteGameId = voteGameId,
                    dishSlug = dishSlug,
                    voteCount = voteCount,
                    timestamp = System.currentTimeMillis() / 1000.0
                )

                val currentUpdates = _voteUpdates.value.toMutableList()
                currentUpdates.add(manualUpdate)
                _voteUpdates.value = currentUpdates
                Log.i(TAG, "Created manual VoteUpdate: $manualUpdate")
                return
            }

            // Last resort - just log the received data structure
            Log.w(TAG, "Could not decode vote update. Data: $data")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to process vote update dictionary: ${e.message}", e)
        }
    }

    private val handleNewVoteGame = Emitter.Listener { args ->
        scope.launch {
            try {
                val data = args.getOrNull(0) as? org.json.JSONObject
                if (data == null) {
                    Log.e(TAG, "Invalid new vote game data")
                    return@launch
                }

                val jsonString = data.toString()
                val voteGame = gson.fromJson(jsonString, DishVoteModel::class.java)

                _newVoteGame.value = voteGame
                val currentGames = _activeVoteGames.value.toMutableList()
                currentGames.add(voteGame)
                _activeVoteGames.value = currentGames

                Log.i(TAG, "Received new vote game: ${voteGame.id}")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode new vote game: ${e.message}", e)
            }
        }
    }

    private val handleVoteGameDeleted = Emitter.Listener { args ->
        scope.launch {
            try {
                val data = args.getOrNull(0) as? org.json.JSONObject
                if (data == null) {
                    Log.e(TAG, "Invalid vote game deletion data")
                    return@launch
                }

                val voteGameId = data.optString("voteGameId")
                if (voteGameId.isEmpty()) return@launch

                _voteGameDeleted.value = voteGameId
                _activeVoteGames.value = _activeVoteGames.value.filter { it.id != voteGameId }

                Log.i(TAG, "Vote game deleted: $voteGameId")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle vote game deletion: ${e.message}", e)
            }
        }
    }

    private val handleVoteGameUpdated = Emitter.Listener { args ->
        scope.launch {
            try {
                val data = args.getOrNull(0) as? org.json.JSONObject
                if (data == null) {
                    Log.e(TAG, "Invalid vote game update data")
                    return@launch
                }

                val jsonString = data.toString()
                val updatedVoteGame = gson.fromJson(jsonString, DishVoteModel::class.java)

                // Update the vote game in the active list
                val currentGames = _activeVoteGames.value.toMutableList()
                val index = currentGames.indexOfFirst { it.id == updatedVoteGame.id }

                if (index != -1) {
                    currentGames[index] = updatedVoteGame
                    _activeVoteGames.value = currentGames
                    Log.i(TAG, "Updated vote game: ${updatedVoteGame.id}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode updated vote game: ${e.message}", e)
            }
        }
    }

    private val handleUserJoinedVote = Emitter.Listener { args ->
        scope.launch {
            try {
                val data = args.getOrNull(0) as? org.json.JSONObject
                if (data == null) {
                    Log.e(TAG, "Invalid user joined data")
                    return@launch
                }

                val voteGameId = data.optString("voteGameId")
                val userName = data.optString("userName")

                if (voteGameId.isNotEmpty() && userName.isNotEmpty()) {
                    Log.i(TAG, "User $userName joined vote game: $voteGameId")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle user joined: ${e.message}", e)
            }
        }
    }

    private val handleUserLeftVote = Emitter.Listener { args ->
        scope.launch {
            try {
                val data = args.getOrNull(0) as? org.json.JSONObject
                if (data == null) {
                    Log.e(TAG, "Invalid user left data")
                    return@launch
                }

                val voteGameId = data.optString("voteGameId")
                val userName = data.optString("userName")

                if (voteGameId.isNotEmpty() && userName.isNotEmpty()) {
                    Log.i(TAG, "User $userName left vote game: $voteGameId")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle user left: ${e.message}", e)
            }
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        unsubscribeFromAll()
    }
}

// MARK: - Supporting Models

/**
 * Vote update event model
 */
data class VoteUpdate(
    val voteGameId: String,
    val dishSlug: String,
    val voteCount: Int,
    val userVote: Boolean? = null,
    val anonymousVote: Boolean? = null,
    val timestamp: Double,
    val userName: String? = null
)

/**
 * Delete vote game data model
 */
data class DeleteVoteGameData(
    val voteGameId: String,
    val timestamp: Double
)

/**
 * Vote options for submitting votes
 */
data class VoteOptions(
    val roomID: String,
    val timestamp: Double = System.currentTimeMillis() / 1000.0
)
