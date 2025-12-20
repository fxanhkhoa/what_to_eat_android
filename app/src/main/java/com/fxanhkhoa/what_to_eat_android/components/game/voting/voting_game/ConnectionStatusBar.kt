package com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import kotlinx.coroutines.flow.first

@Composable
fun ConnectionStatusBar(
    isConnected: Boolean,
    onReconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    var language by remember { mutableStateOf(Language.ENGLISH) }

    // Observe language changes
    LaunchedEffect(Unit) {
        language = localizationManager.currentLanguage.first()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status indicator circle
        Surface(
            shape = CircleShape,
            color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336),
            modifier = Modifier.size(8.dp)
        ) {}

        Spacer(modifier = Modifier.width(8.dp))

        // Status text
        Text(
            text = if (isConnected) {
                localizationManager.getString(R.string.live, language)
            } else {
                localizationManager.getString(R.string.disconnected, language)
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.weight(1f))

        // Reconnect button (only shown when disconnected)
        if (!isConnected) {
            TextButton(
                onClick = onReconnect,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = localizationManager.getString(R.string.reconnect, language),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectionStatusBarPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            ConnectionStatusBar(
                isConnected = true,
                onReconnect = {}
            )

            ConnectionStatusBar(
                isConnected = false,
                onReconnect = {}
            )
        }
    }
}
