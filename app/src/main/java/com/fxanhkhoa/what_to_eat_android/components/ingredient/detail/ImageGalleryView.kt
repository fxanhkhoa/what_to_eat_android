package com.fxanhkhoa.what_to_eat_android.components.ingredient.detail

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager

@Composable
fun ImageGalleryView(
    images: List<String>,
    selectedIndex: Int,
    onImageChanged: (Int) -> Unit,
    localizationManager: LocalizationManager,
    language: Language,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(
        initialPage = selectedIndex,
        pageCount = { images.size }
    )

    LaunchedEffect(pagerState.currentPage) {
        onImageChanged(pagerState.currentPage)
    }

    Column(
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        // Main Image Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(16.dp))
        ) { page ->
            IngredientImage(
                imageUrl = images[page],
                localizationManager = localizationManager,
                language = language,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Page Indicator
        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(images.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (selectedIndex == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                    )
                    if (index < images.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun IngredientImage(
    imageUrl: String,
    localizationManager: LocalizationManager,
    language: Language,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = localizationManager.getString(R.string.ingredient_image, language),
        modifier = modifier,
        contentScale = ContentScale.Crop,
        placeholder = painterResource(id = R.drawable.ic_leaf_placeholder),
        error = painterResource(id = R.drawable.ic_leaf_placeholder),
        fallback = painterResource(id = R.drawable.ic_leaf_placeholder)
    )
}
