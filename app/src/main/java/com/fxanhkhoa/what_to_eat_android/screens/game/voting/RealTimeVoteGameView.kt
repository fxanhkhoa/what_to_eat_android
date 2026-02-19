package com.fxanhkhoa.what_to_eat_android.screens.game.voting

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.components.SharedLoadingView
import com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_game.*
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.model.DishVoteModel
import com.fxanhkhoa.what_to_eat_android.model.VoteDishDto
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider
import com.fxanhkhoa.what_to_eat_android.services.*
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.utils.rememberSharedAuthViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

/**
 * Real-time Vote Game View
 * Displays a live voting game with real-time updates via Socket.IO
 *
 * Matches the SwiftUI RealTimeVoteGameView with:
 * - Real-time vote updates via Socket.IO
 * - Live chat integration
 * - Connection status monitoring
 * - Voting functionality
 * - Dynamic vote results
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealTimeVoteGameView(
    voteGameId: String,
    onDismiss: () -> Unit = {},
    language: Language = Language.ENGLISH
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Services
    val socketManager = remember { SocketIOManager.getInstance(context) }
    val voteGameSocketService = remember { VoteGameSocketService.getInstance(context) }
    val chatSocketService = remember { ChatSocketService.getInstance(context) }
    val localizationManager = remember { LocalizationManager(context) }

    // State
    var voteGame by remember { mutableStateOf<DishVoteModel?>(null) }
    var voteDishes by remember { mutableStateOf<List<DishModel>>(emptyList()) }
    var selectedDish by remember { mutableStateOf<String?>(null) }
    var showingChat by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Socket states
    val isConnected by socketManager.isConnected.collectAsState()
    val newVoteGame by voteGameSocketService.newVoteGame.collectAsState()
    val dishVoteUpdate by voteGameSocketService.dishVoteUpdate.collectAsState()

    val authViewModel = rememberSharedAuthViewModel()
    val user by authViewModel.user.collectAsState()

    // User info - these will update when user state changes
    val senderId = user?.id ?: ""
    val senderName = user?.name ?: ""

    // Debug logging
    LaunchedEffect(user) {
        Log.d("RealTimeVoteGameView", "User state changed: id=${user?.id}, name=${user?.name}, email=${user?.email}")
        Log.d("RealTimeVoteGameView", "senderId=$senderId, senderName=$senderName")
        // Refresh chat service user info when user changes
        chatSocketService.refreshUserInfo()
    }

    // Setup real-time connection - only depends on voteGameId
    LaunchedEffect(voteGameId) {
        Log.d("RealTimeVoteGameView", "LaunchedEffect(voteGameId) RUNNING - voteGameId=$voteGameId")
        setupRealTimeConnection(
            voteGameId = voteGameId,
            socketManager = socketManager,
            voteGameSocketService = voteGameSocketService,
            context = context,
            onVoteGameLoaded = { loadedVoteGame, dishes ->
                voteGame = loadedVoteGame
                voteDishes = dishes
                isLoading = false
                Log.d("RealTimeVoteGameView", "Vote game loaded successfully")
            },
            onError = {
                isLoading = false
                Log.e("RealTimeVoteGameView", "Error loading vote game")
            }
        )
    }

    // Listen for vote game updates
    LaunchedEffect(newVoteGame) {
        newVoteGame?.let { updatedGame ->
            if (updatedGame.id == voteGameId) {
                voteGame = updatedGame
            }
        }
    }

    // Listen for dish vote updates from the dish-vote-update-client event
    LaunchedEffect(dishVoteUpdate) {
        dishVoteUpdate?.let { updatedGame ->
            if (updatedGame.id == voteGameId) {
                Log.d("RealTimeVoteGameView", "Received dish vote update: ${updatedGame.title}")
                Log.d("RealTimeVoteGameView", "Vote items: ${updatedGame.dishVoteItems.map { it.slug to (it.voteUser.size + it.voteAnonymous.size) }}")
                voteGame = updatedGame
            }
        }
    }

    // Cleanup on dispose - only key on voteGameId to prevent premature disposal
    DisposableEffect(voteGameId) {
        Log.d("RealTimeVoteGameView", "DisposableEffect SETUP - voteGameId=$voteGameId")

        onDispose {
            Log.d("RealTimeVoteGameView", "DisposableEffect DISPOSE - voteGameId=$voteGameId, senderId=$senderId")

            // Disconnect from real-time services when leaving the screen
            // Use the current senderId value at disposal time
            if (senderId.isNotEmpty()) {
                disconnectFromRealTime(
                    voteGameId = voteGameId,
                    senderId = senderId,
                    socketManager = socketManager,
                    voteGameSocketService = voteGameSocketService,
                    chatSocketService = chatSocketService
                )
            } else {
                Log.d("RealTimeVoteGameView", "Skipping disconnect - senderId is empty (user not loaded yet)")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localizationManager.getString(R.string.live_vote_game, language)) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localizationManager.getString(R.string.back, language)
                        )
                    }
                },
                actions = {
                    // Connection status button
                    IconButton(
                        onClick = {
                            if (isConnected) {
                                socketManager.disconnect()
                            } else {
                                socketManager.connect()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = localizationManager.getString(
                                if (isConnected) R.string.connected else R.string.disconnected,
                                language
                            ),
                            tint = if (isConnected) Color.Green else Color.Red
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                SharedLoadingView(
                    localizationManager = localizationManager,
                    language = language,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                voteGame?.let { game ->
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Connection Status Bar
                        ConnectionStatusBar(
                            isConnected = isConnected,
                            onReconnect = { socketManager.reconnect() }
                        )

                        // Main Content
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                // Player Info Header
                                PlayerInfoHeader(
                                    playerName = senderName,
                                    language = language
                                )
                            }

                            item {
                                // Vote Game Header
                                VoteGameHeader(
                                    voteGame = game,
                                    language = language
                                )
                            }

                            item {
                                // Real-time Vote Results
                                RealTimeVoteResults(
                                    voteGame = game,
                                    selectedDish = selectedDish,
                                    dishes = voteDishes,
                                    language = language
                                )
                            }

                            item {
                                // Voting Section
                                VotingSection(
                                    voteGame = game,
                                    selectedDish = selectedDish,
                                    dishes = voteDishes,
                                    onDishSelect = { dishSlug ->
                                        selectedDish = dishSlug
                                    },
                                    onSubmitVote = {
                                        scope.launch {
                                            submitVote(
                                                voteGame = game,
                                                selectedDish = selectedDish,
                                                senderId = senderId,
                                                senderName = senderName,
                                                voteGameId = voteGameId,
                                                voteGameSocketService = voteGameSocketService,
                                                onVoteSubmitted = {
                                                    selectedDish = null
                                                }
                                            )
                                        }
                                    },
                                    language = language
                                )
                            }

                            item {
                                // Live Chat Section
                                LiveChatSection(
                                    voteGameId = voteGameId,
                                    showingChat = showingChat,
                                    onToggleChat = { showingChat = !showingChat },
                                    chatSocketService = chatSocketService,
                                    language = language
                                )
                            }
                        }
                    }
                } ?: run {
                    Text(
                        text = localizationManager.getString(R.string.vote_game_not_found, language),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

// MARK: - Helper Functions

/**
 * Setup real-time connection and load initial data
 */
private suspend fun setupRealTimeConnection(
    voteGameId: String,
    socketManager: SocketIOManager,
    voteGameSocketService: VoteGameSocketService,
    context: Context,
    onVoteGameLoaded: (DishVoteModel, List<DishModel>) -> Unit,
    onError: () -> Unit
) {
    // Connect to socket if not already connected
    if (!socketManager.isConnected.value) {
        socketManager.connect()
    }

    // Subscribe to vote game updates
    voteGameSocketService.subscribeToVoteGame(voteGameId)

    // Load initial vote game data
    try {
        val dishVoteService = RetrofitProvider.createService<DishVoteService>()
        val dishService = RetrofitProvider.createService<DishService>()

        val voteGame = dishVoteService.findById(voteGameId)

        // Fetch dishes for all slugs in parallel
        val slugs = voteGame.dishVoteItems.map { it.slug }
        val dishes = kotlinx.coroutines.coroutineScope {
            slugs.map { slug ->
                async(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        dishService.findBySlug(slug)
                    } catch (_: Exception) {
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }

        onVoteGameLoaded(voteGame, dishes)
    } catch (e: Exception) {
        Log.e("RealTimeVoteGameView", "Error loading vote game: ${e.message}", e)
        onError()
    }
}

/**
 * Disconnect from real-time services
 */
private fun disconnectFromRealTime(
    voteGameId: String,
    senderId: String,
    socketManager: SocketIOManager,
    voteGameSocketService: VoteGameSocketService,
    chatSocketService: ChatSocketService
) {
    // Emit user left event
    val leaveData = org.json.JSONObject().apply {
        put("voteGameId", voteGameId)
        put("senderId", senderId)
        put("timestamp", System.currentTimeMillis() / 1000.0)
    }
    socketManager.emit("user_left_vote", leaveData)

    // Unsubscribe from vote game
    voteGameSocketService.unsubscribeFromVoteGame(voteGameId)

    // Leave chat room
    chatSocketService.leaveRoom()
}

/**
 * Submit a vote for a dish
 */
private fun submitVote(
    voteGame: DishVoteModel,
    selectedDish: String?,
    senderId: String,
    senderName: String,
    voteGameId: String,
    voteGameSocketService: VoteGameSocketService,
    onVoteSubmitted: () -> Unit
) {
    if (selectedDish == null) return
    Log.d("RealTimeVoteGameView", "Submitting vote for dish: $selectedDish")

    // Check if user has already voted for this dish
    var isCurrentlyVoted = false
    voteGame.dishVoteItems.forEach { item ->
        if (item.slug == selectedDish) {
            Log.d("RealTimeVoteGameView", "Checking vote status for dish: ${item.slug}, $item")
            if (item.voteAnonymous.contains(senderId) || item.voteUser.contains(senderId)) {
                // User has already voted, we'll toggle it off
                isCurrentlyVoted = true
            }
        }
    }

    // Submit vote via socket
    val voteData = VoteDishDto(
        slug = selectedDish,
        myName = senderId,
        userID = senderId,
        isVoting = !isCurrentlyVoted // Toggle vote state
    )

    val options = VoteOptions(roomID = voteGameId)
    voteGameSocketService.submitVote(voteData, options)

    // Clear selection
    onVoteSubmitted()
}
