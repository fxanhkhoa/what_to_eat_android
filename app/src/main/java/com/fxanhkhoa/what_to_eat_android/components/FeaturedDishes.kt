package com.fxanhkhoa.what_to_eat_android.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

data class FeaturedDish(
    val name: String,
    val description: String,
    val imageRes: Int = android.R.drawable.ic_menu_gallery // Placeholder image
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeaturedDishes(
    modifier: Modifier = Modifier
) {
    // Sample data for featured dishes
    val dishes = remember {
        listOf(
            FeaturedDish(
                name = "Spicy Thai Curry",
                description = "Authentic Thai red curry with coconut milk and fresh herbs"
            ),
            FeaturedDish(
                name = "Italian Pasta",
                description = "Classic spaghetti carbonara with crispy pancetta"
            ),
            FeaturedDish(
                name = "Japanese Ramen",
                description = "Rich tonkotsu ramen with tender chashu pork"
            ),
            FeaturedDish(
                name = "Mexican Tacos",
                description = "Street-style tacos with authentic Mexican flavors"
            ),
            FeaturedDish(
                name = "French Croissant",
                description = "Buttery, flaky croissants baked fresh daily"
            )
        )
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { dishes.size }
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 3D Carousel
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentPadding = PaddingValues(horizontal = 0.dp),
            pageSpacing = 16.dp
        ) { page ->
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

            // 3D transformation calculations
            val scale by animateFloatAsState(
                targetValue = if (pageOffset.absoluteValue < 0.5f) 1.0f else 0.85f,
                animationSpec = tween(300),
                label = "scale"
            )

            val alpha by animateFloatAsState(
                targetValue = if (pageOffset.absoluteValue < 0.5f) 1.0f else 0.7f,
                animationSpec = tween(300),
                label = "alpha"
            )

            val rotationY = pageOffset * 30f
            val translationX = pageOffset * 50f

            FeaturedDishCard(
                dish = dishes[page],
                modifier = Modifier
                    .scale(scale)
                    .graphicsLayer {
                        this.alpha = alpha
                        this.rotationY = rotationY
                        this.translationX = translationX
                        this.cameraDistance = 12f * density
                    }
            )
        }

        // Page indicators
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(dishes.size) { index ->
                val isSelected = pagerState.currentPage == index
                val animatedSize by animateFloatAsState(
                    targetValue = if (isSelected) 12f else 8f,
                    animationSpec = tween(300),
                    label = "indicatorSize"
                )

                Box(
                    modifier = Modifier
                        .size(animatedSize.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                )

                if (index < dishes.size - 1) {
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
        }
    }
}

@Composable
private fun FeaturedDishCard(
    dish: FeaturedDish,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background image
            Image(
                painter = painterResource(id = dish.imageRes),
                contentDescription = dish.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop,
                alpha = 0.3f
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.surface
                            ),
                            startY = 0f,
                            endY = 600f
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = dish.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = dish.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Start,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
