package com.fxanhkhoa.what_to_eat_android.components.ingredient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.components.ImagePlaceholder
import com.fxanhkhoa.what_to_eat_android.model.Ingredient
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager

@Composable
fun IngredientListContent(
    ingredients: List<Ingredient>,
    isLoading: Boolean,
    hasMorePages: Boolean,
    onLoadMore: () -> Unit,
    onIngredientClick: (Ingredient) -> Unit,
    modifier: Modifier = Modifier,
    language: Language = Language.ENGLISH
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(ingredients) { ingredient ->
            IngredientRowView(
                ingredient = ingredient,
                language = language,
                onClick = { onIngredientClick(ingredient) }
            )
            if (ingredient.id == ingredients.lastOrNull()?.id && hasMorePages && !isLoading) {
                onLoadMore()
            }
        }
        if (isLoading && ingredients.isNotEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
fun IngredientRowView(
    ingredient: Ingredient,
    modifier: Modifier = Modifier,
    language: Language = Language.ENGLISH,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterVertically)
                    .background(Color.Gray.copy(alpha = 0.2f), shape = MaterialTheme.shapes.small)
            ) {
                IngredientImage(
                    thumbnailUrl = ingredient.images.firstOrNull(),
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    language = language
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    ingredient.getTitle(language.code)
                        ?: localizationManager.getString(R.string.unknown, language),
                    style = MaterialTheme.typography.titleMedium
                )
                if (ingredient.ingredientCategory.isNotEmpty()) {
                    val localizedCategories = ingredient.ingredientCategory.map { category ->
                        localizationManager.getString(
                            try {
                                val resId = R.string::class.java.getField(category.lowercase().replace(" ", "_")).getInt(null)
                                resId
                            } catch (e: Exception) {
                                R.string.unknown
                            },
                            language
                        )
                    }
                    Text(
                        localizedCategories.joinToString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Row {
                    NutritionBadge(
                        title = localizationManager.getString(R.string.calories, language),
                        value = ingredient.calories?.toInt()?.toString() ?: "-"
                    )
                    NutritionBadge(
                        title = localizationManager.getString(R.string.protein, language),
                        value = ingredient.protein?.let { String.format("%.1fg", it) } ?: "-")
                    NutritionBadge(
                        title = localizationManager.getString(R.string.carbs, language),
                        value = ingredient.carbohydrate?.let { String.format("%.1fg", it) } ?: "-")
                }
            }
        }
    }
}

@Composable
private fun IngredientImage(
    thumbnailUrl: String?,
    modifier: Modifier = Modifier,
    language: Language = Language.ENGLISH
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    if (!thumbnailUrl.isNullOrEmpty()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(thumbnailUrl)
                .crossfade(true)
                .build(),
            contentDescription = localizationManager.getString(R.string.ingredient_thumbnail, language),
            modifier = modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
            error = painterResource(id = android.R.drawable.ic_menu_gallery),
            fallback = painterResource(id = android.R.drawable.ic_menu_gallery)
        )
    } else {
        ImagePlaceholder(modifier = modifier)
    }
}

@Composable
fun NutritionBadge(title: String, value: String) {
    Column(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .background(Color.Gray.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
    }
}
