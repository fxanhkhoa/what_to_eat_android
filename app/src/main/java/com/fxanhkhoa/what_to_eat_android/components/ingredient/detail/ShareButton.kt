package com.fxanhkhoa.what_to_eat_android.components.ingredient.detail

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.model.Ingredient
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager

@Composable
fun ShareButton(
    ingredient: Ingredient,
    localizationManager: LocalizationManager,
    language: Language
) {
    val context = LocalContext.current

    IconButton(
        onClick = {
            val title = ingredient.getTitle(language.code)
                ?: localizationManager.getString(R.string.ingredient, language)

            val calories = String.format("%.0f", ingredient.calories ?: 0.0)
            val protein = String.format("%.1f", ingredient.protein ?: 0.0)
            val carbs = String.format("%.1f", ingredient.carbohydrate ?: 0.0)
            val fat = String.format("%.1f", ingredient.fat ?: 0.0)

            val shareText = """
                $title
                
                ${localizationManager.getString(R.string.nutrition_per_measure, language)} ${ingredient.measure}:
                • ${localizationManager.getString(R.string.calories, language)}: $calories ${localizationManager.getString(R.string.kcal, language)}
                • ${localizationManager.getString(R.string.protein, language)}: $protein${localizationManager.getString(R.string.g, language)}
                • ${localizationManager.getString(R.string.carbohydrates, language)}: $carbs${localizationManager.getString(R.string.g, language)}
                • ${localizationManager.getString(R.string.fat, language)}: $fat${localizationManager.getString(R.string.g, language)}
            """.trimIndent()

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            val chooser = Intent.createChooser(
                shareIntent,
                localizationManager.getString(R.string.share_ingredient, language)
            )
            context.startActivity(chooser)
        }
    ) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = localizationManager.getString(R.string.share, language)
        )
    }
}
