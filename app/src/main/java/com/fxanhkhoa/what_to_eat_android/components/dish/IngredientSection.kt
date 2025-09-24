package com.fxanhkhoa.what_to_eat_android.components.dish

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.model.Ingredient
import com.fxanhkhoa.what_to_eat_android.model.IngredientsInDish
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import java.util.Locale
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.ExperimentalFoundationApi

@Composable
fun IngredientPlaceholderRow(
    dishIngredient: IngredientsInDish,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit,
    isDarkTheme: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(
                color = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onToggle,
            modifier = Modifier.padding(end = 4.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ingredient),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = dishIngredient.slug,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
            }
            Text(
                text = String.format(Locale.US, "%.1f", dishIngredient.quantity),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (dishIngredient.note.isNotEmpty()) {
                Text(
                    text = dishIngredient.note,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun IngredientRow(
    dishIngredient: IngredientsInDish,
    fullIngredient: Ingredient,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit,
    isDarkTheme: Boolean,
    language: String
) {
    val localizedTitle = fullIngredient.getTitle(language) ?: dishIngredient.slug
    val imageUrl = fullIngredient.images.firstOrNull()?.takeIf { it.isNotEmpty() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(
                color = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onToggle,
            modifier = Modifier.padding(end = 4.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ingredient),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = localizedTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
            }
            Text(
                text = String.format(Locale.US, "%.1f", dishIngredient.quantity) + (fullIngredient.measure?.let { " $it" } ?: ""),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (dishIngredient.note.isNotEmpty()) {
                Text(
                    text = dishIngredient.note,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun CheckboxToggleStyle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: @Composable () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (checked) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
            contentDescription = null,
            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .clickable(role = Role.Checkbox) { onCheckedChange(!checked) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        label()
    }
}

@Composable
fun IngredientCategoriesSection(
    dishIngredientCategories: List<String>,
    modifier: Modifier = Modifier,
    onCategoryClick: (String) -> Unit = {},
    language: Language
) {
    // Use LocalizationManager to get localized heading
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    // Use the compile-time string resource (no reflection)
    val title = localizationManager.getString(R.string.ingredient_categories, language)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (dishIngredientCategories.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    dishIngredientCategories.forEachIndexed { index, category ->
                        androidx.compose.material3.Button(
                            onClick = { onCategoryClick(category) },
                            modifier = Modifier
                                .padding(horizontal = 0.dp)
                                .height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = when (index % 2) {
                                    0 -> MaterialTheme.colorScheme.primary
                                    1 -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.secondary
                                }
                            )
                        ) {
                            Text(
                                text = category,
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IngredientSection(
    modifier: Modifier = Modifier,
    dish: DishModel,
    fullIngredients: Map<String, Ingredient>,
    checkedIngredients: Set<String>,
    onToggleIngredient: (ingredientId: String, isChecked: Boolean) -> Unit,
    isLoadingIngredients: Boolean,
    onLoadIngredients: () -> Unit = {},
    language: Language
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    val title = try {
        localizationManager.getString(R.string.ingredients, language)
    } catch (_: Exception) {
        "Ingredients"
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        // Debug logging
        Log.d("IngredientSection", "isLoadingIngredients: $isLoadingIngredients")
        Log.d("IngredientSection", "dish.ingredients.size: ${dish.ingredients.size}")
        Log.d("IngredientSection", "fullIngredients.size: ${fullIngredients.size}")
        Log.d("IngredientSection", "checkedIngredients.size: ${checkedIngredients.size}")

        if (isLoadingIngredients) {
            // Centered loading indicator with a small text
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Loading ingredients...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else if (dish.ingredients.isEmpty()) {
            // Show empty state
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "No ingredients found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val gridState = rememberLazyGridState()

            // Snapshot collections to avoid concurrent-modification or identity issues
            val ingredientsListSnapshot = remember(dish.ingredients) { dish.ingredients.toList() }
            val fullIngredientsSnapshot = remember(fullIngredients) { fullIngredients.toMap() }
            val checkedIngredientsSnapshot = remember(checkedIngredients) { checkedIngredients.toSet() }

            Log.d("IngredientSection", "Rendering ${ingredientsListSnapshot.size} ingredients, ${fullIngredientsSnapshot.size} full ingredients, ${checkedIngredientsSnapshot.size} checked")

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 400.dp),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = {
                    // Use indexed items with a composite key (id + index) to guarantee uniqueness
                    itemsIndexed(
                        items = ingredientsListSnapshot,
                        key = { index, item -> "${item.ingredientId}-$index" }
                    ) { index, dishIngredient: IngredientsInDish ->
                        Log.d("IngredientSection", "Rendering item $index: ${dishIngredient.ingredientId}")

                        val ingredientId = dishIngredient.ingredientId

                        // Defensive: ensure ingredientId is present; if it's blank, render placeholder
                        if (ingredientId.isBlank()) {
                            IngredientPlaceholderRow(
                                dishIngredient = dishIngredient,
                                isChecked = false,
                                onToggle = { /* ignore invalid id */ },
                                isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
                            )
                            return@itemsIndexed
                        }

                        val fullIngredient = fullIngredientsSnapshot[ingredientId]
                        val isChecked = checkedIngredientsSnapshot.contains(ingredientId)

                        if (fullIngredient != null) {
                            IngredientRow(
                                dishIngredient = dishIngredient,
                                fullIngredient = fullIngredient,
                                isChecked = isChecked,
                                onToggle = { checked ->
                                    try {
                                        onToggleIngredient(ingredientId, checked)
                                    } catch (t: Throwable) {
                                        Log.e("IngredientSection", "onToggleIngredient threw", t)
                                    }
                                },
                                isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme(),
                                language = language.code
                            )
                        } else {
                            IngredientPlaceholderRow(
                                dishIngredient = dishIngredient,
                                isChecked = isChecked,
                                onToggle = { checked ->
                                    try {
                                        onToggleIngredient(ingredientId, checked)
                                    } catch (t: Throwable) {
                                        Log.e("IngredientSection", "onToggleIngredient threw", t)
                                    }
                                },
                                isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
                            )
                        }
                    }
                }
            )
        }
    }

    // Trigger load on first composition
    LaunchedEffect(Unit) {
        onLoadIngredients()
    }
}
