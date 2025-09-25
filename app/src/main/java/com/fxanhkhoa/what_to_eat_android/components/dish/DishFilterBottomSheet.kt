package com.fxanhkhoa.what_to_eat_android.components.dish

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.shared.DifficultyLevel
import com.fxanhkhoa.what_to_eat_android.shared.MealCategory
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.viewmodel.DishFilters
import com.fxanhkhoa.what_to_eat_android.viewmodel.DishListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishFilterBottomSheet(
    viewModel: DishListViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    language: Language = Language.ENGLISH
) {
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    var tempFilters by remember { mutableStateOf(filters) }
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    val filterDishesText = localizationManager.getString(R.string.filter, language)
    val preparationText = localizationManager.getString(R.string.preparation, language)
    val cookingText = localizationManager.getString(R.string.cooking, language)
    val minutesText = localizationManager.getString(R.string.mins, language)
    val mealCategoriesText = localizationManager.getString(R.string.meal_categories, language)
    val difficultyLevelText = localizationManager.getString(R.string.difficulty_level, language)
    val tagsText = localizationManager.getString(R.string.tags, language)
    val clearAllText = localizationManager.getString(R.string.clear_all, language)
    val applyFiltersText = localizationManager.getString(R.string.apply_filters, language)
    val addTagText = localizationManager.getString(R.string.add_tag, language)
    val addTagPlaceholderText = localizationManager.getString(R.string.add_tag_placeholder, language)
    val noMinimumText = localizationManager.getString(R.string.no_minimum, language)
    val noMaximumText = localizationManager.getString(R.string.no_maximum, language)
    val resetRangeText = localizationManager.getString(R.string.reset_range, language)
    val closeText = localizationManager.getString(R.string.close, language)
    val removeTagText = localizationManager.getString(R.string.remove_tag, language)

    // Update temp filters when viewModel filters change
    LaunchedEffect(filters) {
        tempFilters = filters
    }
    
    BottomSheetDefaults.ExpandedShape
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = filterDishesText,
                style = MaterialTheme.typography.headlineSmall
            )
            
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = closeText)
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Meal Categories
            item {
                MealCategoryFilterSection(
                    title = mealCategoriesText,
                    selectedOptions = tempFilters.mealCategories,
                    onSelectionChange = { selected ->
                        tempFilters = tempFilters.copy(mealCategories = selected)
                    },
                    language = language
                )
            }
            
            // Difficulty Levels
            item {
                DifficultyLevelFilter(
                    title = difficultyLevelText,
                    selectedLevels = tempFilters.difficultLevels,
                    onSelectionChange = { selected ->
                        tempFilters = tempFilters.copy(difficultLevels = selected)
                    }
                )
            }
            
            // Time Ranges
            item {
                TimeRangeFilter(
                    title = "$preparationText ($minutesText)",
                    fromValue = tempFilters.preparationTimeFrom,
                    toValue = tempFilters.preparationTimeTo,
                    onRangeChange = { from, to ->
                        tempFilters = tempFilters.copy(
                            preparationTimeFrom = from,
                            preparationTimeTo = to
                        )
                    },
                    noMinimumText = noMinimumText,
                    noMaximumText = noMaximumText,
                    resetRangeText = resetRangeText
                )
            }
            
            item {
                TimeRangeFilter(
                    title = "$cookingText ($minutesText)",
                    fromValue = tempFilters.cookingTimeFrom,
                    toValue = tempFilters.cookingTimeTo,
                    onRangeChange = { from, to ->
                        tempFilters = tempFilters.copy(
                            cookingTimeFrom = from,
                            cookingTimeTo = to
                        )
                    },
                    noMinimumText = noMinimumText,
                    noMaximumText = noMaximumText,
                    resetRangeText = resetRangeText
                )
            }
            
            // Tags
            item {
                TagsFilter(
                    title = tagsText,
                    selectedTags = tempFilters.tags,
                    onTagsChange = { tags ->
                        tempFilters = tempFilters.copy(tags = tags)
                    },
                    addTagText = addTagText,
                    addTagPlaceholderText = addTagPlaceholderText,
                    closeText = closeText,
                    removeTagText = removeTagText
                )
            }
            
            // Action buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            tempFilters = DishFilters()
                            viewModel.clearAllFilters()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(clearAllText)
                    }
                    
                    Button(
                        onClick = {
                            viewModel.updateFilters { tempFilters }
                            viewModel.applyFilters()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(applyFiltersText)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    title: String,
    options: List<String>,
    selectedOptions: List<String>,
    onSelectionChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(options) { option ->
                val isSelected = selectedOptions.contains(option)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newSelection = if (isSelected) {
                            selectedOptions - option
                        } else {
                            selectedOptions + option
                        }
                        onSelectionChange(newSelection)
                    },
                    label = { Text(option.replaceFirstChar { it.uppercase() }) }
                )
            }
        }
    }
}

@Composable
private fun DifficultyLevelFilter(
    title: String,
    selectedLevels: List<String>,
    onSelectionChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(DifficultyLevel.entries) { level ->
                val isSelected = selectedLevels.contains(level.displayName)

                // Get colors from the enum
                val levelColor = Color(context.getColor(level.colorRes))

                Button(
                    onClick = {
                        val newSelection = if (isSelected) {
                            selectedLevels - level.displayName
                        } else {
                            selectedLevels + level.displayName
                        }
                        onSelectionChange(newSelection)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) levelColor else Color.Transparent,
                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.dp, levelColor),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Icon based on difficulty level
                        val iconRes = when (level) {
                            DifficultyLevel.EASY -> R.drawable.easy
                            DifficultyLevel.MEDIUM -> R.drawable.medium
                            DifficultyLevel.HARD -> R.drawable.hard
                        }

                        Image(
                            painter = painterResource(id= iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )

                        Text(
                            text = level.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeRangeFilter(
    title: String,
    fromValue: Int?,
    toValue: Int?,
    onRangeChange: (Int?, Int?) -> Unit,
    noMinimumText: String,
    noMaximumText: String,
    resetRangeText: String,
    modifier: Modifier = Modifier
) {
    val minTime = 0f
    val maxTime = 480f // 8 hours max

    // Convert nullable Int to Float for slider, with defaults
    val currentFromValue = fromValue?.toFloat() ?: minTime
    val currentToValue = toValue?.toFloat() ?: maxTime

    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Display current range values
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (fromValue != null) "${fromValue}min" else noMinimumText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (toValue != null) "${toValue}min" else noMaximumText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Range Slider
        RangeSlider(
            value = currentFromValue..currentToValue,
            onValueChange = { range ->
                val newFrom = if (range.start == minTime) null else range.start.toInt()
                val newTo = if (range.endInclusive == maxTime) null else range.endInclusive.toInt()
                onRangeChange(newFrom, newTo)
            },
            valueRange = minTime..maxTime,
            steps = 47, // 240 minutes / 5 minute steps = 48 steps
            modifier = Modifier.padding(vertical = 8.dp),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onSurface,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = Color.Transparent,
                inactiveTickColor = MaterialTheme.colorScheme.onSurface
            )
        )

        // Min/Max labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${minTime.toInt()}min",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${maxTime.toInt()}min",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Reset button
        if (fromValue != null || toValue != null) {
            TextButton(
                onClick = { onRangeChange(null, null) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(resetRangeText)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagsFilter(
    title: String,
    selectedTags: List<String>,
    onTagsChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    addTagText: String,
    addTagPlaceholderText: String,
    closeText: String,
    removeTagText: String
) {
    var newTag by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Add new tag input
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTag,
                onValueChange = { newTag = it },
                placeholder = { Text(addTagPlaceholderText) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            Button(
                onClick = {
                    if (newTag.isNotBlank() && !selectedTags.contains(newTag.trim())) {
                        onTagsChange(selectedTags + newTag.trim())
                        newTag = ""
                    }
                },
                enabled = newTag.isNotBlank()
            ) {
                Text(addTagText)
            }
        }

        // Selected tags
        if (selectedTags.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedTags) { tag ->
                    AssistChip(
                        onClick = {
                            onTagsChange(selectedTags - tag)
                        },
                        label = { Text("#$tag") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = removeTagText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealCategoryFilterSection(
    title: String,
    selectedOptions: List<String>,
    onSelectionChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    language: Language
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(MealCategory.entries) { category ->
                val localizedName = category.getDisplayName(language, localizationManager)
                val isSelected = selectedOptions.contains(category.displayName)

                // Get colors from the enum
                val categoryColor = Color(context.getColor(category.colorRes))

                Button(
                    onClick = {
                        val newSelection = if (isSelected) {
                            selectedOptions - category.displayName
                        } else {
                            selectedOptions + category.displayName
                        }
                        onSelectionChange(newSelection)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) categoryColor else Color.Transparent,
                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.dp, categoryColor),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(
                        text = localizedName,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
