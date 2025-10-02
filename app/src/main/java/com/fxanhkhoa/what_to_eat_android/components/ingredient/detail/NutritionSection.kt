package com.fxanhkhoa.what_to_eat_android.components.ingredient.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.model.Ingredient
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager

@Composable
fun NutritionSection(
    ingredient: Ingredient,
    localizationManager: LocalizationManager,
    language: Language,
    modifier: Modifier = Modifier
) {
    val nutritionData = listOf(
        NutritionInfo(
            title = localizationManager.getString(R.string.calories, language),
            value = String.format("%.0f", ingredient.calories ?: 0.0),
            unit = localizationManager.getString(R.string.kcal, language),
            color = Color(0xFFFF9500) // Orange
        ),
        NutritionInfo(
            title = localizationManager.getString(R.string.protein, language),
            value = String.format("%.1f", ingredient.protein ?: 0.0),
            unit = localizationManager.getString(R.string.g, language),
            color = Color(0xFFFF3B30) // Red
        ),
        NutritionInfo(
            title = localizationManager.getString(R.string.carbohydrates, language),
            value = String.format("%.1f", ingredient.carbohydrate ?: 0.0),
            unit = localizationManager.getString(R.string.g, language),
            color = Color(0xFF007AFF) // Blue
        ),
        NutritionInfo(
            title = localizationManager.getString(R.string.fat, language),
            value = String.format("%.1f", ingredient.fat ?: 0.0),
            unit = localizationManager.getString(R.string.g, language),
            color = Color(0xFFFFCC00) // Yellow
        ),
        NutritionInfo(
            title = localizationManager.getString(R.string.cholesterol, language),
            value = String.format("%.1f", ingredient.cholesterol ?: 0.0),
            unit = localizationManager.getString(R.string.mg, language),
            color = Color(0xFFAF52DE) // Purple
        ),
        NutritionInfo(
            title = localizationManager.getString(R.string.sodium, language),
            value = String.format("%.1f", ingredient.sodium ?: 0.0),
            unit = localizationManager.getString(R.string.mg, language),
            color = Color(0xFF34C759) // Green
        )
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = localizationManager.getString(R.string.nutrition_information, language),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Group nutrition data into pairs for 2-column layout
                nutritionData.chunked(2).forEach { nutritionPair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NutritionCard(
                            nutrition = nutritionPair[0],
                            modifier = Modifier.weight(1f)
                        )

                        if (nutritionPair.size > 1) {
                            NutritionCard(
                                nutrition = nutritionPair[1],
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            // Empty space for odd number of nutrition items
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionCard(
    nutrition: NutritionInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = nutrition.title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = nutrition.value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = nutrition.color
                )
                Text(
                    text = nutrition.unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class NutritionInfo(
    val title: String,
    val value: String,
    val unit: String,
    val color: Color
)
