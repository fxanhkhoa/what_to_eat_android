package com.fxanhkhoa.what_to_eat_android.components.ingredient

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.shared.IngredientCategory
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.R

@Composable
fun CategoryFilterView(
    selectedCategory: String?,
    selectedCategories: Set<String>,
    onCategoryChanged: (String?) -> Unit,
    language: Language = Language.ENGLISH,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    val allText = localizationManager.getString(R.string.all, language)

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        CategoryButton(
            title = allText,
            isSelected = selectedCategories.isEmpty(),
            onClick = { onCategoryChanged(null) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        IngredientCategory.allCases.forEach { category ->
            val categoryName = localizationManager.getString(category.localizationKey, language)
            CategoryButton(
                title = categoryName,
                isSelected = selectedCategories.contains(category.name),
                onClick = { onCategoryChanged(category.name) }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun CategoryButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .height(32.dp)
            .defaultMinSize(minWidth = 64.dp)
    ) {
        Text(title, style = MaterialTheme.typography.labelSmall)
    }
}
