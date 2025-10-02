package com.fxanhkhoa.what_to_eat_android.screens.dish

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.components.dish.DishCard
import com.fxanhkhoa.what_to_eat_android.components.dish.DishFilterBottomSheet
import com.fxanhkhoa.what_to_eat_android.data.dto.QueryDishDto
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.viewmodel.DishListViewModel
import com.fxanhkhoa.what_to_eat_android.viewmodel.FilterChipData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishListScreen(
    modifier: Modifier = Modifier,
    viewModel: DishListViewModel = viewModel(),
    navController: NavController,
) {
    val dishes by viewModel.dishes.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val hasMorePages by viewModel.hasMorePages.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val hasActiveFilters by viewModel.hasActiveFilters.collectAsStateWithLifecycle()
    val activeFilterChips by viewModel.activeFilterChips.collectAsStateWithLifecycle()

    var searchText by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    // Bottom sheet state
    val bottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    var language by remember { mutableStateOf(Language.ENGLISH) }

    // Get localized strings
    val searchDishesText = localizationManager.getString(R.string.search_dishes, language)
    val clearSearchText = localizationManager.getString(R.string.clear_search, language)
    val filterDishesText = localizationManager.getString(R.string.filter_dishes, language)
    val clearAllFiltersText = localizationManager.getString(R.string.clear_all_filters, language)
    val noDishesFoundText = localizationManager.getString(R.string.no_dishes_found, language)

    // Observe language changes
    LaunchedEffect(Unit) {
        language = localizationManager.currentLanguage.first()
    }

    // Load dishes on first composition
    LaunchedEffect(Unit) {
        viewModel.loadDishes(QueryDishDto())
    }

    // Infinite scroll effect
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null &&
                    lastVisibleIndex >= (dishes?.size ?: 0) - 3 &&
                    hasMorePages &&
                    !isLoading &&
                    (dishes?.isNotEmpty() == true)
                ) {
                    viewModel.loadMoreDishes()
                }
            }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search bar with filter button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    viewModel.updateSearchKeyword(it)
                },
                modifier = Modifier.weight(1f),
                placeholder = { Text(searchDishesText) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = searchDishesText) },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                searchText = ""
                                viewModel.updateSearchKeyword("")
                            }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = clearSearchText)
                        }
                    }
                },
                singleLine = true
            )

            // Filter button
            IconButton(
                onClick = { showBottomSheet = true },
                modifier = Modifier
            ) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = filterDishesText,
                    tint = if (hasActiveFilters) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        // Filter chips
        if (hasActiveFilters) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activeFilterChips) { chip ->
                    FilterChip(chip = chip)
                }

                item {
                    AssistChip(
                        onClick = { viewModel.clearAllFilters() },
                        label = { Text(clearAllFiltersText) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Error message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Dishes list
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                scope.launch {
                    try {
                        viewModel.refreshDishes()
                    } finally {
                        isRefreshing = false
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {

            if ((dishes?.isEmpty() == true || dishes == null) && !isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(noDishesFoundText)
                }
                return@PullToRefreshBox
            }

            LazyVerticalGrid(
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Fixed(2)
            ) {
                dishes?.let { dishList ->
                    items(dishList) { dish ->
                        DishCard(dish = dish, modifier = Modifier, onClick = {
                            navController.navigate("dish/${dish.slug}")
                        }, language = language)
                    }
                }
            }
        }
    }

    // Bottom Sheet
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = bottomSheetState
        ) {
            DishFilterBottomSheet(
                viewModel = viewModel,
                onDismiss = { showBottomSheet = false },
                language = language
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChip(
    chip: FilterChipData,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    var language by remember { mutableStateOf(Language.ENGLISH) }
    val removeFilterText = localizationManager.getString(R.string.remove_filter, language)

    // Observe language changes
    LaunchedEffect(Unit) {
        language = localizationManager.currentLanguage.first()
    }

    AssistChip(
        onClick = { chip.onRemove() },
        label = { Text(chip.title) },
        trailingIcon = {
            Icon(
                Icons.Default.Clear,
                contentDescription = removeFilterText,
                modifier = Modifier.size(16.dp)
            )
        },
        modifier = modifier
    )
}