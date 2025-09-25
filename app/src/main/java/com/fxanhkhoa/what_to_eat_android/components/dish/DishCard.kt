package com.fxanhkhoa.what_to_eat_android.components.dish

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.model.MultiLanguage
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishCard(
    dish: DishModel,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    language: Language = Language.ENGLISH
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Thumbnail Image
            DishImage(
                thumbnailUrl = dish.thumbnail,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = dish.getTitle(language.code) ?: "No Title",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description
            Text(
                text = dish.getShortDescription(language.code) ?: "No Description",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight
            )

            // Time information
            if (dish.cookingTime != null || dish.preparationTime != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = "Time",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = formatTotalTime(dish.preparationTime, dish.cookingTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DishImage(
    thumbnailUrl: String?,
    modifier: Modifier = Modifier
) {
    if (!thumbnailUrl.isNullOrEmpty()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(thumbnailUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Dish thumbnail",
            modifier = modifier,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
            error = painterResource(id = android.R.drawable.ic_menu_gallery),
            fallback = painterResource(id = android.R.drawable.ic_menu_gallery)
        )
    } else {
        DishPlaceholder(modifier = modifier)
    }
}

@Composable
private fun DishPlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = "No image",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// Helper function to format total cooking and preparation time
private fun formatTotalTime(preparationTime: Int?, cookingTime: Int?): String {
    val prepTime = preparationTime ?: 0
    val cookTime = cookingTime ?: 0
    val totalMinutes = prepTime + cookTime

    return when {
        totalMinutes == 0 -> "Time N/A"
        totalMinutes < 60 -> "$totalMinutes min"
        else -> {
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            "${hours}h ${minutes}m"
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DishCardPreview() {
    // Create sample data for preview
    val sampleTitle = listOf(MultiLanguage(lang = "en", data = "Sample Dish"))
    val sampleDescription = listOf(
        MultiLanguage(
            lang = "en",
            data = "This is a sample dish description for preview purposes."
        )
    )

    val sampleDish = DishModel(
        id = "1",
        deleted = false,
        createdAt = "",
        updatedAt = "",
        createdBy = null,
        updatedBy = null,
        deletedBy = null,
        deletedAt = null,
        slug = "sample-dish",
        title = sampleTitle,
        shortDescription = sampleDescription,
        content = sampleDescription,
        tags = listOf("sample"),
        preparationTime = 15,
        cookingTime = 25,
        difficultLevel = "easy",
        mealCategories = listOf("lunch"),
        ingredientCategories = emptyList(),
        thumbnail = null,
        videos = emptyList(),
        ingredients = emptyList(),
        relatedDishes = emptyList(),
        labels = null
    )

    MaterialTheme {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card with image placeholder
                DishCard(
                    dish = sampleDish,
                    onClick = { /* Handle click */ }
                )

                // Card with longer text to test overflow
                DishCard(
                    dish = sampleDish.copy(
                        title = listOf(
                            MultiLanguage(
                                lang = "en",
                                data = "Very Long Dish Name That Should Be Truncated"
                            )
                        ),
                        shortDescription = listOf(
                            MultiLanguage(
                                lang = "en",
                                data = "This is a very long description that should be truncated after two lines to test the overflow behavior of the text component in the dish card."
                            )
                        )
                    ),
                    onClick = { /* Handle click */ }
                )
            }
        }
    }
}
