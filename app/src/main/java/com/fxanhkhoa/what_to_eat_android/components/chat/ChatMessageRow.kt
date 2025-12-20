package com.fxanhkhoa.what_to_eat_android.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.model.ChatMessage
import com.fxanhkhoa.what_to_eat_android.model.ChatMessageType
import java.text.SimpleDateFormat
import java.util.*

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
        modifier = modifier,
        horizontalAlignment = alignment
    ) {
        // Sender name (not shown for system messages)
        if (!isSystemMessage) {
            Text(
                text = message.senderName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )
        }

        // Message bubble
        Row(
            modifier = Modifier.fillMaxWidth(if (isSystemMessage) 1f else 0.75f),
            horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
        ) {
            Column(
                modifier = Modifier
                    .background(
                        color = bubbleColor,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )

                // Message reactions
                if (message.reactions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    MessageReactionsView(reactions = message.reactions)
                }
            }
        }

        // Timestamp
        Text(
            text = formatChatTime(message.timestamp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun MessageReactionsView(
    reactions: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        reactions.entries.sortedBy { it.key }.forEach { (reaction, count) ->
            if (count > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = reaction,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
