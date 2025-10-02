package com.fxanhkhoa.what_to_eat_android.components.ingredient.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.model.Ingredient
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager

@Composable
fun TitleSection(
    ingredient: Ingredient,
    localizationManager: LocalizationManager,
    language: Language,
    modifier: Modifier = Modifier
) {
    val ingredientTitle = ingredient.getTitle(language.code)
        ?: localizationManager.getString(R.string.unknown_ingredient, language)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = ingredientTitle,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        ingredient.measure?.let { measure ->
            Text(
                text = "${localizationManager.getString(R.string.measure, language)}: ${ingredient.measure}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ingredient.weight?.let { weight ->
            Text(
                text = "${localizationManager.getString(R.string.weight, language)}: ${String.format("%.1f", weight)}g",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
