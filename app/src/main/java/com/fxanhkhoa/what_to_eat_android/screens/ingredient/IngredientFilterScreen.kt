package com.fxanhkhoa.what_to_eat_android.screens.ingredient

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.shared.IngredientCategory
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientFilterScreen(
    selectedCategories: Set<String>,
    onApplyFilter: (Set<String>) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    language: Language = Language.ENGLISH
) {
    var tempSelectedCategories by remember { mutableStateOf(selectedCategories) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val localizationManager = remember { com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.filter_ingredients, language), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.Close, contentDescription = localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.cancel, language))
                    }
                },
                actions = {
                    TextButton(
                        onClick = { tempSelectedCategories = emptySet() },
                        enabled = tempSelectedCategories.isNotEmpty()
                    ) {
                        Text(localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.reset, language))
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (tempSelectedCategories.isNotEmpty()) {
                    Text(
                        localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.categories_selected, language).format(tempSelectedCategories.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { tempSelectedCategories = emptySet() },
                        enabled = tempSelectedCategories.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.clear_all, language))
                    }
                    Button(
                        onClick = {
                            onApplyFilter(tempSelectedCategories)
                            onCancel()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.apply_filter, language))
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                localizationManager.getString(com.fxanhkhoa.what_to_eat_android.R.string.select_categories_to_filter_ingredients, language),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(IngredientCategory.allCases) { category ->
                    FilterCategoryRow(
                        category = category,
                        isSelected = tempSelectedCategories.contains(category.name),
                        onToggle = { isSelected ->
                            tempSelectedCategories = if (isSelected) {
                                tempSelectedCategories + category.name
                            } else {
                                tempSelectedCategories - category.name
                            }
                        },
                        language = language
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterCategoryRow(
    category: IngredientCategory,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit,
    language: Language = Language.ENGLISH,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val localizationManager = remember { com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager(context) }

    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = if (isSelected) 2.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onToggle(!isSelected) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(category.color, shape = MaterialTheme.shapes.small)
            ) {
                Image(
                    painter = painterResource(id = category.iconName),
                    contentDescription = category.displayName,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    localizationManager.getString(category.localizationKey, language),
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
