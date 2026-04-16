package com.fxanhkhoa.what_to_eat_android.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.launch

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String,
    val gradientColors: List<Color>,
    val accentColor: Color,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    val language by localizationManager.currentLanguage.collectAsStateWithLifecycle(initialValue = Language.ENGLISH)

    // Localized strings
    val nextText         = remember(language) { localizationManager.getString(R.string.onboarding_next, language) }
    val getStartedText   = remember(language) { localizationManager.getString(R.string.onboarding_get_started, language) }
    val skipText         = remember(language) { localizationManager.getString(R.string.onboarding_skip, language) }

    val onboardingPages = remember(language) {
        listOf(
            OnboardingPage(
                emoji = "🍜",
                title = localizationManager.getString(R.string.onboarding_page1_title, language),
                description = localizationManager.getString(R.string.onboarding_page1_desc, language),
                gradientColors = listOf(Color(0xFFFFF3E0), Color(0xFFFFCCBC)),
                accentColor = Color(0xFFAA6804),
            ),
            OnboardingPage(
                emoji = "🎰",
                title = localizationManager.getString(R.string.onboarding_page2_title, language),
                description = localizationManager.getString(R.string.onboarding_page2_desc, language),
                gradientColors = listOf(Color(0xFFFCE4EC), Color(0xFFFFD0B5)),
                accentColor = Color(0xFFA7683A),
            ),
            OnboardingPage(
                emoji = "📋",
                title = localizationManager.getString(R.string.onboarding_page3_title, language),
                description = localizationManager.getString(R.string.onboarding_page3_desc, language),
                gradientColors = listOf(Color(0xFFE8F5E9), Color(0xFFFFF9C4)),
                accentColor = Color(0xFF4CAF50),
            ),
        )
    }

    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingPages.lastIndex

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            OnboardingPageContent(page = onboardingPages[pageIndex])
        }

        // Bottom controls overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Page indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(onboardingPages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 8.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "dot_width"
                    )
                    val color = onboardingPages[pagerState.currentPage].accentColor
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(if (isSelected) color else color.copy(alpha = 0.3f))
                    )
                }
            }

            // CTA button
            Button(
                onClick = {
                    if (isLastPage) {
                        onFinish()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = onboardingPages[pagerState.currentPage].accentColor
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = if (isLastPage) getStartedText else nextText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Skip button (hidden on last page)
            if (!isLastPage) {
                TextButton(onClick = onFinish) {
                    Text(
                        text = skipText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = onboardingPages[pagerState.currentPage].accentColor.copy(alpha = 0.7f)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Floating animation for emoji
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emoji_float"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emoji_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(page.gradientColors))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Emoji card
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -80 }
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .offset(y = offsetY.dp)
                        .scale(scale)
                        .clip(RoundedCornerShape(40.dp))
                        .background(Color.White.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = page.emoji, fontSize = 80.sp)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Title
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600, delayMillis = 200)) + slideInVertically(tween(600, delayMillis = 200)) { 60 }
            ) {
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = page.accentColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Description
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600, delayMillis = 400)) + slideInVertically(tween(600, delayMillis = 400)) { 60 }
            ) {
                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF5A4A3A),
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )
            }
        }
    }
}





