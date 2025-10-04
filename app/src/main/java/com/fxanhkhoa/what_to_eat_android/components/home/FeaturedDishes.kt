package com.fxanhkhoa.what_to_eat_android.components.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.services.DishService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue
import androidx.compose.ui.platform.LocalContext
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import kotlinx.coroutines.flow.first
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider.createService
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Photo

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeaturedDishes(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    refreshTrigger: Int = 0 // Add refresh trigger parameter
) {
    // State for dishes, loading, and error
    var dishes by remember { mutableStateOf<List<DishModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Fetch dishes when refreshTrigger changes
    LaunchedEffect(refreshTrigger) {
        isLoading = true
        error = null
        try {
            val service = createService<DishService>()
            val result = withContext(Dispatchers.IO) {
                service.findRandom(7)
            }
            dishes = result
        } catch (e: Exception) {
            error = e.localizedMessage ?: "Unknown error"
        } finally {
            isLoading = false
        }
    }

    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    var currentLanguage by remember { mutableStateOf(Language.ENGLISH) }

    // Observe language changes
    LaunchedEffect(Unit) {
        currentLanguage = localizationManager.currentLanguage.first()
    }

    // Pager state for carousel
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { dishes.size }
    )

    // Auto-scroll carousel every 5 seconds and loop
    LaunchedEffect(dishes.size) {
        if (dishes.isNotEmpty()) {
            while (true) {
                delay(5000)
                val nextPage = (pagerState.currentPage + 1) % dishes.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            isLoading -> {
                Box(modifier = Modifier.height(280.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Box(modifier = Modifier.height(280.dp), contentAlignment = Alignment.Center) {
                    Text(text = error ?: "Error", color = MaterialTheme.colorScheme.error)
                }
            }

            dishes.isEmpty() -> {
                Box(modifier = Modifier.height(280.dp), contentAlignment = Alignment.Center) {
                    Text(text = "No featured dishes found.")
                }
            }

            else -> {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    pageSpacing = 16.dp
                ) { page ->
                    val pageOffset =
                        (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

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
                        language = currentLanguage.code,
                        navController = navController,
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
    }
}

@Composable
private fun FeaturedDishCard(
    modifier: Modifier = Modifier,
    dish: DishModel,
    language: String = "en",
    navController: NavController? = null,
) {
    Card(
        modifier = modifier
            .then(
                if (navController != null) Modifier.clickable { navController.navigate("dish/${dish.slug}") } else Modifier
            )
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
            // Background image (use thumbnail if available, else fallback)
            if (dish.thumbnail != null && dish.thumbnail.isNotBlank()) {
                AsyncImage(
                    model = dish.thumbnail,
                    contentDescription = dish.getTitle(language) ?: "Dish image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop,
                    alpha = 0.8f
                )
            } else {
                Image(
                    imageVector = Icons.Filled.Photo,
                    contentDescription = dish.getTitle(language) ?: "Dish image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop,
                    alpha = 0.8f
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                            ),
                            startY = 0f,
                            endY = 600f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = dish.getTitle(language) ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(8.dp))

                val description = dish.getShortDescription(language)
                Text(
                    text = if (description != null) {
                        if (description.length > 20) {
                            description.substring(0, 20) + "..."
                        } else {
                            description}
                    } else {
                        "" // Or some placeholder if description is null
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Start,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
