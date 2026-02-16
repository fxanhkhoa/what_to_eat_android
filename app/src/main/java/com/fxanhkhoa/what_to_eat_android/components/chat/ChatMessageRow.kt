package com.fxanhkhoa.what_to_eat_android.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fxanhkhoa.what_to_eat_android.model.ChatMessage
import com.fxanhkhoa.what_to_eat_android.model.ChatMessageType
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * Chat message row component
 * Displays a single chat message with sender info, bubble, and timestamp
 */
@Composable
fun ChatMessageRow(
    message: ChatMessage,
    isMyMessage: Boolean,
    modifier: Modifier = Modifier
) {
    val isSystemMessage = message.type == ChatMessageType.SYSTEM

    val alignment: Alignment.Horizontal = when {
        isSystemMessage -> Alignment.CenterHorizontally
        isMyMessage -> Alignment.End
        else -> Alignment.Start
    }

    val bubbleColor = when {
        isSystemMessage -> MaterialTheme.colorScheme.surfaceVariant
        isMyMessage -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    val textColor = when {
        isSystemMessage -> MaterialTheme.colorScheme.onSurfaceVariant
        isMyMessage -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        if (isSystemMessage) {
            // System message - centered
            Surface(
                color = bubbleColor,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        } else {
            // Regular message with avatar
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
            ) {
                if (!isMyMessage) {
                    // Avatar on the left for received messages
                    AvatarWithInitials(
                        name = message.senderName,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                Column(
                    horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Sender name above bubble (only for received messages)
                    if (!isMyMessage) {
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
                        )
                    }

                    // Message bubble
                    Surface(
                        color = bubbleColor,
                        shape = RoundedCornerShape(
                            topStart = if (isMyMessage) 16.dp else 4.dp,
                            topEnd = if (isMyMessage) 4.dp else 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        ),
                        shadowElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            // Message content
                            Text(
                                text = message.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor
                            )

                            // Timestamp
                            Text(
                                text = formatChatTime(message.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 4.dp)
                            )
                        }
                    }

                    // Message reactions (outside bubble)
                    if (message.reactions.isNotEmpty()) {
                        MessageReactionsView(
                            reactions = message.reactions,
                            modifier = Modifier.padding(
                                start = if (isMyMessage) 0.dp else 4.dp,
                                end = if (isMyMessage) 4.dp else 0.dp,
                                top = 4.dp
                            )
                        )
                    }
                }

                if (isMyMessage) {
                    // Avatar on the right for sent messages
                    AvatarWithInitials(
                        name = message.senderName,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Avatar component showing initials of the name
 */
@Composable
private fun AvatarWithInitials(
    name: String,
    modifier: Modifier = Modifier
) {
    val initials = getInitials(name)
    val backgroundColor = getColorForName(name)

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

/**
 * Get first 2 characters (or initials) from name
 */
private fun getInitials(name: String): String {
    if (name.isEmpty()) return "?"

    val words = name.trim().split(" ")
    return if (words.size >= 2) {
        // First letter of first two words
        "${words[0].firstOrNull()?.uppercaseChar() ?: ""}${words[1].firstOrNull()?.uppercaseChar() ?: ""}"
    } else {
        // First two characters of the name
        name.take(2).uppercase()
    }
}

/**
 * Generate a consistent color based on the name
 */
private fun getColorForName(name: String): Color {
    val colors = listOf(
        Color(0xFF1976D2), // Blue
        Color(0xFF388E3C), // Green
        Color(0xFFD32F2F), // Red
        Color(0xFF7B1FA2), // Purple
        Color(0xFFF57C00), // Orange
        Color(0xFF0097A7), // Cyan
        Color(0xFFC2185B), // Pink
        Color(0xFF5D4037), // Brown
        Color(0xFF303F9F), // Indigo
        Color(0xFF00796B), // Teal
    )

    val hash = abs(name.hashCode())
    return colors[hash % colors.size]
}

@Composable
private fun MessageReactionsView(
    reactions: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        reactions.entries.sortedBy { it.key }.forEach { (reaction, count) ->
            if (count > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 2.dp,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = reaction,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (count > 1) {
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatChatTime(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    return dateFormat.format(Date(timestamp * 1000))
}
