package com.fxanhkhoa.what_to_eat_android.components.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager

/**
 * Load more button component
 * Button to load older messages with loading indicator
 */
@Composable
fun LoadMoreButton(
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    localizationManager: LocalizationManager,
    language: Language,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onLoadMore,
        enabled = !isLoadingMore,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        if (isLoadingMore) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = localizationManager.getString(R.string.chat_loading, language),
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            Text(
                text = localizationManager.getString(R.string.chat_load_more_messages, language),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

