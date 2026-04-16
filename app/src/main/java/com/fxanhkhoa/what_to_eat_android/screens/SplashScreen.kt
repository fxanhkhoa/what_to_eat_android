package com.fxanhkhoa.what_to_eat_android.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager

@Composable
fun SplashScreen() {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    val language by localizationManager.currentLanguage.collectAsStateWithLifecycle(initialValue = Language.ENGLISH)

    val appName = remember(language) { context.getString(R.string.app_name) }
    val tagline = remember(language) { localizationManager.getString(R.string.splash_tagline, language) }

    // Master entrance animation trigger
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // ── Infinite animations ────────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    // Slow background pulse (two blobs)
    val blob1Scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "blob1"
    )
    val blob2Scale by infiniteTransition.animateFloat(
        initialValue = 1.1f, targetValue = 0.92f,
        animationSpec = infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "blob2"
    )

    // Emoji float
    val emojiOffsetY by infiniteTransition.animateFloat(
        initialValue = -10f, targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "emojiFloat"
    )

    // Loading dots fade cycle
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 0), RepeatMode.Reverse),
        label = "dot1"
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 200), RepeatMode.Reverse),
        label = "dot2"
    )
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 400), RepeatMode.Reverse),
        label = "dot3"
    )

    // ── Entrance animations ────────────────────────────────────────────────
    val logoScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "logoScale"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800),
        label = "contentAlpha"
    )

    // ── Colors ─────────────────────────────────────────────────────────────
    val primaryOrange = Color(0xFFAA6804)
    val softOrange    = Color(0xFFFFB86A)

    // ── Layout ─────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFFFFF8F0), Color(0xFFFFEDD5), Color(0xFFFFCCA0)),
                    radius = 1600f
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // ── Decorative background blobs ────────────────────────────────────
        Box(
            modifier = Modifier
                .size(320.dp)
                .scale(blob1Scale)
                .offset(x = (-60).dp, y = (-80).dp)
                .clip(CircleShape)
                .background(softOrange.copy(alpha = 0.18f))
                .blur(40.dp)
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .scale(blob2Scale)
                .offset(x = 80.dp, y = 100.dp)
                .clip(CircleShape)
                .background(primaryOrange.copy(alpha = 0.12f))
                .blur(40.dp)
        )

        // ── Centred content ────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(contentAlpha)
        ) {

            // Logo card
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale)
                    .offset(y = emojiOffsetY.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFFFFF3E0), Color(0xFFFFDCBC)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🍽️", fontSize = 60.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App name
            Text(
                text = appName,
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = primaryOrange,
                textAlign = TextAlign.Center,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Tagline
            Text(
                text = tagline,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFA07040),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(dot1Alpha, dot2Alpha, dot3Alpha).forEach { alpha ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .alpha(alpha)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(listOf(softOrange, primaryOrange))
                            )
                    )
                }
            }
        }

        // ── Bottom decoration strip ────────────────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(contentAlpha * 0.5f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("🍕", "🍣", "🥗", "🍜", "🍩").forEachIndexed { i, emoji ->
                val itemScale by infiniteTransition.animateFloat(
                    initialValue = 0.85f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        tween(900 + i * 120, easing = FastOutSlowInEasing),
                        RepeatMode.Reverse
                    ),
                    label = "foodScale$i"
                )
                Text(
                    text = emoji,
                    fontSize = 22.sp,
                    modifier = Modifier.scale(itemScale)
                )
            }
        }
    }
}





