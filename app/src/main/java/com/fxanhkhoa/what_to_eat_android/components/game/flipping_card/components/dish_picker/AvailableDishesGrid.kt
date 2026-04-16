package com.fxanhkhoa.what_to_eat_android.components.game.flipping_card.components.dish_picker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager

@Composable
internal fun AvailableDishesGrid(
    dishes: List<DishModel>,
    selectedDishes: List<DishModel>,
    isLoading: Boolean,
    maxDishes: Int,
    onDishTap: (DishModel) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: suspend () -> Unit,
    language: Language,
    localizationManager: LocalizationManager
) {
    when {
        isLoading && dishes.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        dishes.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = localizationManager.getString(R.string.no_dishes_found, language),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(dishes, key = { it.id }) { dish ->
                    val isSelected = selectedDishes.any { it.id == dish.id }
                    val canSelect = selectedDishes.size < maxDishes

                    FlippingSelectableDishCard(
                        dish = dish,
                        isSelected = isSelected,
                        canSelect = canSelect,
                        onTap = { onDishTap(dish) },
                        language = language,
                        localizationManager = localizationManager
                    )

                    if (dish == dishes.lastOrNull()) {
                        LaunchedEffect(dish) { onLoadMore() }
                    }
                }

                if (isLoading && dishes.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                }
            }
        }
    }
}

