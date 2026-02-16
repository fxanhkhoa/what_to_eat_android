package com.fxanhkhoa.what_to_eat_android.components.chat

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.model.ChatRoomType
import com.fxanhkhoa.what_to_eat_android.services.ChatSocketService
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.utils.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * Reusable real-time chat component
 */
@Composable
fun RealTimeChatView(
    roomId: String,
    roomType: ChatRoomType,
    chatService: ChatSocketService,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    val tokenManager = remember { TokenManager.getInstance(context) }

    var language by remember { mutableStateOf(Language.ENGLISH) }
    var messageText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    var anchorMessageId by remember { mutableStateOf<String?>(null) }
    var isAtBottom by remember { mutableStateOf(true) }

    val messages by chatService.messages.collectAsState()
    val typingUsers by chatService.typingUsers.collectAsState()
    val onlineUsers by chatService.onlineUsers.collectAsState()
    val hasMoreMessages by chatService.hasMoreMessages.collectAsState()
    val isLoadingMore by chatService.isLoadingMore.collectAsState()

    // Get current user ID from TokenManager
    var currentUserId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        currentUserId = tokenManager.getUserInfo()?.id
        // Refresh chat service user info to ensure it has latest data
        chatService.refreshUserInfo()
    }

    val listState = rememberLazyListState()

    // Observe language changes
    LaunchedEffect(Unit) {
        language = localizationManager.currentLanguage.first()
    }

    // Setup chat connection
    LaunchedEffect(roomId) {
        Log.d("RealTimeChatView", "Setting up chat for roomId: $roomId")
        setupChatConnection(chatService, roomId, roomType)
    }

    // Auto-scroll to bottom for new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && !isLoadingMore) {
            val lastMessage = messages.last()
            val isMyMessage = lastMessage.senderId == currentUserId

            // Handle anchor scroll or new message scroll
            if (anchorMessageId != null && !isLoadingMore) {
                // Scroll to anchor after loading more
                val anchorIndex = messages.indexOfFirst { it.id == anchorMessageId }
                if (anchorIndex != -1) {
                    delay(100)
                    listState.animateScrollToItem(anchorIndex)
                    anchorMessageId = null
                }
            } else if (isAtBottom || isMyMessage) {
                // Scroll to bottom for new messages
                delay(100)
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    // Handle typing indicator
    LaunchedEffect(messageText) {
        if (messageText.isNotEmpty() && !isTyping) {
            isTyping = true
            chatService.startTyping()
            Log.d("RealTimeChatView", "Start typing")
        } else if (messageText.isEmpty() && isTyping) {
            isTyping = false
            chatService.stopTyping()
            Log.d("RealTimeChatView", "Stop typing")
        }
    }

    // Detect if user is at bottom
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        isAtBottom = lastVisibleIndex >= messages.size - 1
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        // Chat Messages
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Load more button at the top
                if (hasMoreMessages) {
                    item(key = "loadMoreButton") {
                        LoadMoreButton(
                            isLoadingMore = isLoadingMore,
                            onLoadMore = {
                                anchorMessageId = messages.firstOrNull()?.id
                                chatService.loadMoreMessages()
                            },
                            localizationManager = localizationManager,
                            language = language
                        )
                    }
                }

                // Messages
                items(messages, key = { it.id }) { message ->
                    val isMyMessage = message.senderId == currentUserId
                    ChatMessageRow(
                        message = message,
                        isMyMessage = isMyMessage,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Typing Indicators
        AnimatedVisibility(visible = typingUsers.isNotEmpty()) {
            TypingIndicatorView(
                typingUsers = typingUsers,
                onlineUsers = onlineUsers,
                localizationManager = localizationManager,
                language = language
            )
        }

        // Message Input
        MessageInputView(
            messageText = messageText,
            onMessageTextChange = { messageText = it },
            onSendMessage = {
                val trimmedMessage = messageText.trim()
                if (trimmedMessage.isNotEmpty()) {
                    chatService.sendMessage(trimmedMessage)
                    messageText = ""
                    chatService.stopTyping()
                }
            },
            localizationManager = localizationManager,
            language = language
        )
    }
}

// MARK: - Helper Functions

private fun setupChatConnection(
    chatService: ChatSocketService,
    roomId: String,
    roomType: ChatRoomType
) {
    // Ensure socket is connected before joining the chat room
    chatService.connectSocket()
    chatService.joinChatRoom(roomId, roomType)
    chatService.loadMessageHistory(limit = 25)
}

@Preview(showBackground = true)
@Composable
private fun RealTimeChatViewPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Chat Preview",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // RealTimeChatView requires a real ChatSocketService instance
            // For preview, you'd need to create a mock service
        }
    }
}
