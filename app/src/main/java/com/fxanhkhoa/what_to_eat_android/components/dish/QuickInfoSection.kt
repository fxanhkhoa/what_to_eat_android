package com.fxanhkhoa.what_to_eat_android.components.dish

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow

@Composable
private fun QuickInfoCard(
    @DrawableRes iconRes: Int,
    titleContent: @Composable () -> Unit,
    value: String,
    modifier: Modifier = Modifier
) {
    // Use a neutral background similar to systemGray6 in both themes (adjust with Material colors)
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
            .background(color = backgroundColor, shape = RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
        )

        // Title slot (allows passing LocalizedText composable directly)
        titleContent()

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun QuickInfoSection(
    dish: com.fxanhkhoa.what_to_eat_android.model.DishModel,
    modifier: Modifier = Modifier,
    language: com.fxanhkhoa.what_to_eat_android.ui.localization.Language = com.fxanhkhoa.what_to_eat_android.ui.localization.Language.ENGLISH
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val localizationManager =
        remember { com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager(context) }
    val minsText = remember(language) {
        localizationManager.getString(
            com.fxanhkhoa.what_to_eat_android.R.string.mins,
            language
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        dish.preparationTime?.let { prepTime ->
            QuickInfoCard(
                iconRes = com.fxanhkhoa.what_to_eat_android.R.drawable.preparation_time,
                titleContent = {
                    Text(
                        text = localizationManager.getString(
                            com.fxanhkhoa.what_to_eat_android.R.string.preparation,
                            language
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                value = "$prepTime $minsText",
                modifier = Modifier
                    .weight(1f)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
            )
        }

        dish.cookingTime?.let { cookTime ->
            QuickInfoCard(
                iconRes = com.fxanhkhoa.what_to_eat_android.R.drawable.cooking_time,
                titleContent = {
                    Text(
                        text = localizationManager.getString(
                            com.fxanhkhoa.what_to_eat_android.R.string.cooking,
                            language
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                value = "$cookTime $minsText",
                modifier = Modifier.weight(1f)
            )
        }

        if (dish.ingredients.isNotEmpty()) {
            QuickInfoCard(
                iconRes = com.fxanhkhoa.what_to_eat_android.R.drawable.ingredient,
                titleContent = {
                    Text(
                        text = localizationManager.getString(
                            com.fxanhkhoa.what_to_eat_android.R.string.ingredients,
                            language
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                value = "${dish.ingredients.size}",
                modifier = Modifier.weight(1f)
            )
        }
    }
}
