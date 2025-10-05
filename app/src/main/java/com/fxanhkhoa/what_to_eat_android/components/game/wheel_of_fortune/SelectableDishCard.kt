package com.fxanhkhoa.what_to_eat_android.components.game.wheel_of_fortune

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.model.MultiLanguage
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language

@Composable
fun SelectableDishCard(
    dish: DishModel,
    isSelected: Boolean,
    canSelect: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    language: Language = Language.ENGLISH
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.95f else 1f,
        label = "card_scale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clickable(enabled = canSelect || isSelected) { onTap() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(12.dp)
        ) {
            // Image with overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                // Dish image
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(dish.thumbnail)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Dish image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp
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
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                )

                // Selection overlay
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                } else if (!canSelect) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveCircle,
                            contentDescription = "Cannot select",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = getLocalizedTitle(dish, language),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            )

            // Description
            if (getLocalizedDescription(dish, language) != null) {
                Text(
                    text = getLocalizedDescription(dish, language) ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(28.dp))
            }

            // Bottom info row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prep time
                if (dish.preparationTime != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = "Time",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${dish.preparationTime} ${stringResource(R.string.mins)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Difficulty
                if (dish.difficultLevel != null) {
                    val difficultyText = when (dish.difficultLevel.lowercase()) {
                        "easy" -> stringResource(R.string.difficulty_easy)
                        "medium" -> stringResource(R.string.difficulty_medium)
                        "hard" -> stringResource(R.string.difficulty_hard)
                        else -> dish.difficultLevel
                    }

                    Text(
                        text = difficultyText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

private fun getLocalizedTitle(dish: DishModel, language: Language): String {
    return dish.getTitle(language.code) ?: dish.slug
}

private fun getLocalizedDescription(dish: DishModel, language: Language): String? {
    return dish.getShortDescription(language.code)
}

@Preview(showBackground = true)
@Composable
fun SelectableDishCardPreview() {
    MaterialTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SelectableDishCard(
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
                        MultiLanguage(lang = "en", data = "A delicious Vietnamese beef noodle soup with rich broth"),
                        MultiLanguage(lang = "vi", data = "Món phở bò truyền thống với nước dùng đậm đà")
                    ),
                    content = listOf(),
                    preparationTime = 30,
                    cookingTime = 60,
                    difficultLevel = "medium",
                    thumbnail = "https://example.com/pho.jpg"
                ),
                isSelected = false,
                canSelect = true,
                onTap = {},
                modifier = Modifier.width(180.dp)
            )

            SelectableDishCard(
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
                        MultiLanguage(lang = "en", data = "Vietnamese sandwich with various fillings"),
                        MultiLanguage(lang = "vi", data = "Bánh mì Việt Nam với nhiều loại nhân")
                    ),
                    content = listOf(),
                    preparationTime = 15,
                    cookingTime = 10,
                    difficultLevel = "easy",
                    thumbnail = null
                ),
                isSelected = true,
                canSelect = true,
                onTap = {},
                modifier = Modifier.width(180.dp)
            )
        }
    }
}
