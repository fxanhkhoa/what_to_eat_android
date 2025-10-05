package com.fxanhkhoa.what_to_eat_android.components.game.wheel_of_fortune

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.model.MultiLanguage
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language

@Composable
fun SelectedDishChip(
    dish: DishModel,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    language: Language = Language.ENGLISH
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dish thumbnail
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(dish.thumbnail)
                    .crossfade(true)
                    .build(),
                contentDescription = "Selected dish",
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.dp
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "No image",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            )

            // Dish title
            Text(
                text = getLocalizedTitle(dish, language),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color.Red,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun getLocalizedTitle(dish: DishModel, language: Language): String {
    return dish.getTitle(language.code) ?: dish.slug
}

@Preview(showBackground = true)
@Composable
fun SelectedDishChipPreview() {
    MaterialTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SelectedDishChip(
                dish = DishModel(
                    id = "1",
                    deleted = false,
                    createdAt = null,
                    updatedAt = null,
                    createdBy = null,
                    updatedBy = null,
                    deletedBy = null,
                    deletedAt = null,
                    slug = "pho-bo",
                    title = listOf(
                        MultiLanguage(lang = "en", data = "Pho Bo"),
                        MultiLanguage(lang = "vi", data = "Phở Bò")
                    ),
                    shortDescription = listOf(
                        MultiLanguage(lang = "en", data = "Vietnamese beef noodle soup"),
                        MultiLanguage(lang = "vi", data = "Món phở bò truyền thống")
                    ),
                    content = listOf(
                        MultiLanguage(lang = "en", data = "Content"),
                        MultiLanguage(lang = "vi", data = "Nội dung")
                    ),
                    preparationTime = 30,
                    cookingTime = 60,
                    difficultLevel = "medium",
                    thumbnail = null
                ),
                onRemove = {}
            )

            SelectedDishChip(
                dish = DishModel(
                    id = "2",
                    deleted = false,
                    createdAt = null,
                    updatedAt = null,
                    createdBy = null,
                    updatedBy = null,
                    deletedBy = null,
                    deletedAt = null,
                    slug = "banh-mi",
                    title = listOf(
                        MultiLanguage(lang = "en", data = "Banh Mi"),
                        MultiLanguage(lang = "vi", data = "Bánh Mì")
                    ),
                    shortDescription = listOf(
                        MultiLanguage(lang = "en", data = "Vietnamese sandwich"),
                        MultiLanguage(lang = "vi", data = "Bánh mì Việt Nam")
                    ),
                    content = listOf(
                        MultiLanguage(lang = "en", data = "Content"),
                        MultiLanguage(lang = "vi", data = "Nội dung")
                    ),
                    preparationTime = 15,
                    cookingTime = 10,
                    difficultLevel = "easy",
                    thumbnail = "https://example.com/banh-mi.jpg"
                ),
                onRemove = {}
            )
        }
    }
}
