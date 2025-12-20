package com.fxanhkhoa.what_to_eat_android.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.model.ChatUserInApp
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager

/**
 * Typing indicator component
 * Shows which users are currently typing
 */
@Composable
fun TypingIndicatorView(
    typingUsers: List<String>,
    onlineUsers: List<ChatUserInApp>,
    localizationManager: LocalizationManager,
    language: Language,
    modifier: Modifier = Modifier
) {
    val typingText = getTypingIndicatorText(typingUsers, onlineUsers, localizationManager, language)

    if (typingText.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = typingText,
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

private fun getTypingIndicatorText(
    typingUsers: List<String>,
    onlineUsers: List<ChatUserInApp>,
    localizationManager: LocalizationManager,
    language: Language
): String {
    if (typingUsers.isEmpty()) return ""

    val typingUserNames = typingUsers.mapNotNull { userId ->
        onlineUsers.find { it.id == userId }?.name?.takeIf { it.isNotEmpty() } ?: userId
    }

    return when (typingUserNames.size) {
        1 -> "${typingUserNames[0]} ${localizationManager.getString(R.string.chat_is_typing, language)}"
        2 -> "${typingUserNames[0]} ${localizationManager.getString(R.string.chat_and, language)} ${typingUserNames[1]} ${localizationManager.getString(R.string.chat_are_typing, language)}"
        else -> {
            val firstNames = typingUserNames.take(2).joinToString(", ")
            val othersCount = typingUserNames.size - 2
            "$firstNames ${localizationManager.getString(R.string.chat_and_others_are_typing, language).format(othersCount)}"
        }
    }
}

