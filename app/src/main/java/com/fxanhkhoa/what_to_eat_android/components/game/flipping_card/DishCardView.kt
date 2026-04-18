package com.fxanhkhoa.what_to_eat_android.components.game.flipping_card

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.viewmodel.GameCard
import kotlinx.coroutines.delay

@Composable
fun DishCardView(
    card: GameCard,
    onTapped: () -> Unit,
    language: Language,
    localizationManager: LocalizationManager,
    cardIndex: Int = 0,
    shuffleVersion: Int = 0
) {
    val colorScheme = MaterialTheme.colorScheme
    var isPressed by remember { mutableStateOf(false) }

    // Staggered entrance animation
    var visible by remember { mutableStateOf(false) }
    val entranceScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 350,
            delayMillis = cardIndex * 60,
            easing = FastOutSlowInEasing
        ),
        label = "entrance_scale"
    )
    val entranceAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = cardIndex * 60,
            easing = LinearOutSlowInEasing
        ),
        label = "entrance_alpha"
    )
    LaunchedEffect(card.id) {
        visible = true
    }

    // Shuffle animation: each card scales down then back up with staggered delay
    var shuffleScale by remember { mutableStateOf(1f) }
    val animatedShuffleScale by animateFloatAsState(
        targetValue = shuffleScale,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "shuffle_scale"
    )
    LaunchedEffect(shuffleVersion) {
        if (shuffleVersion > 0) {
            delay((cardIndex * 40).toLong())
            shuffleScale = 0.75f
            delay(200)
            shuffleScale = 1f
        }
    }

    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped) 0f else 180f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "card_rotation"
    )

    val dishTitle = card.dish.getTitle(language.code)
        ?: localizationManager.getString(R.string.unknown_dish, language)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .graphicsLayer {
                val s = entranceScale * animatedShuffleScale * (if (isPressed) 0.95f else 1f)
                scaleX = s
                scaleY = s
                alpha = entranceAlpha
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = !card.isFlipped) {
                onTapped()
            },
        shape = RoundedCornerShape(12.dp),
        shadowElevation = if (card.isFlipped) 8.dp else 4.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (card.isFlipped) {
                // Front side - show dish
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .graphicsLayer { rotationY = 0f },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        model = card.dish.thumbnail,
                        contentDescription = dishTitle,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.ic_launcher_foreground),
                        placeholder = painterResource(R.drawable.ic_launcher_foreground)
                    )

                    Text(
                        text = dishTitle,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Back side - show card back with gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    colorScheme.primary.copy(alpha = 0.8f),
                                    colorScheme.primary.copy(alpha = 0.6f)
                                )
                            )
                        )
                        .graphicsLayer { rotationY = 180f },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.graphicsLayer {
                            rotationY = 180f
                            rotationZ = 0f
                        },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.what_to_eat_high_resolution_logo_transparent),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).graphicsLayer { rotationY = 180f },
                            tint = Color.White
                        )

                        Text(
                            text = localizationManager.getString(R.string.mystery_dish, language),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.graphicsLayer { rotationY = 180f }
                        )
                    }
                }
            }
        }
    }
}
