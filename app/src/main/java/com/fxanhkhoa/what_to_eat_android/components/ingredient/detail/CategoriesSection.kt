package com.fxanhkhoa.what_to_eat_android.components.ingredient.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.model.Ingredient
import com.fxanhkhoa.what_to_eat_android.shared.IngredientCategory
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager

@Composable
fun CategoriesSection(
    ingredient: Ingredient,
    localizationManager: LocalizationManager,
    language: Language,
    modifier: Modifier = Modifier
) {
    if (ingredient.ingredientCategory.isNotEmpty()) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = localizationManager.getString(R.string.categories, language),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            // Use regular Column and Row instead of LazyVerticalGrid to avoid scrolling conflicts
            val categories = ingredient.ingredientCategory.map { categoryString ->
                IngredientCategory.fromString(categoryString)
            }

            // Group categories into pairs for 2-column layout
            categories.chunked(2).forEach { categoryPair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip(
                        category = categoryPair[0],
                        localizationManager = localizationManager,
                        language = language,
                        modifier = Modifier
                            .weight(1f)
                            .height(70.dp)
                    )

                    if (categoryPair.size > 1) {
                        CategoryChip(
                            category = categoryPair[1],
                            localizationManager = localizationManager,
                            language = language,
                            modifier = Modifier
                                .weight(1f)
                                .height(70.dp)
                        )
                    } else {
                        // Empty space for odd number of categories
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryChip(
    category: IngredientCategory,
    localizationManager: LocalizationManager,
    language: Language,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = category.color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Image(
            painter = painterResource(id = category.iconName),
            contentDescription = category.getDisplayName(localizationManager, language),
            modifier = Modifier
                .size(32.dp)
        )

        Text(
            text = category.getDisplayName(localizationManager, language),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = category.color
        )
    }
}
