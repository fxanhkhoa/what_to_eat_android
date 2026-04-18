package com.fxanhkhoa.what_to_eat_android.components.game.flipping_card

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.viewmodel.GameState

@Composable
fun GameActionsView(
    gameState: GameState,
    onNewGame: () -> Unit,
    onShuffle: () -> Unit = {},
    language: Language,
    localizationManager: LocalizationManager
) {
    var shuffleTrigger by remember { mutableIntStateOf(0) }
    val shuffleRotation by animateFloatAsState(
        targetValue = (shuffleTrigger * 360).toFloat(),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "shuffle_rotation"
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (gameState == GameState.PLAYING) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = localizationManager.getString(R.string.choose_your_destiny, language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )

                OutlinedButton(
                    onClick = {
                        shuffleTrigger++
                        onShuffle()
                    },
                    shape = RoundedCornerShape(25.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer { rotationZ = shuffleRotation }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = localizationManager.getString(R.string.shuffle, language),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            Button(
                onClick = onNewGame,
                shape = RoundedCornerShape(25.dp),
                contentPadding = PaddingValues(horizontal = 30.dp, vertical = 15.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = localizationManager.getString(R.string.new_game, language),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
