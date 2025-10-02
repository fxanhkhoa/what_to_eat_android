package com.fxanhkhoa.what_to_eat_android.screens.ingredient

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fxanhkhoa.what_to_eat_android.components.ingredient.*
import com.fxanhkhoa.what_to_eat_android.shared.IngredientCategory
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.viewmodel.IngredientListViewModel
import kotlinx.coroutines.flow.first
import com.fxanhkhoa.what_to_eat_android.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientListScreen(
    modifier: Modifier = Modifier,
    viewModel: IngredientListViewModel = viewModel(),
    navController: NavController
) {
    val ingredients by viewModel.ingredients.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val hasMorePages by viewModel.hasMorePages.collectAsState()

    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showingFilter by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val bottomSheetState = rememberModalBottomSheetState()
    val localizationManager = remember { LocalizationManager(context) }
    var language by remember { mutableStateOf(Language.ENGLISH) }

    val loadingText = localizationManager.getString(R.string.loading, language)
    val noDataText = localizationManager.getString(R.string.no_ingredients_found, language)

    // Observe language changes
    LaunchedEffect(Unit) {
        language = localizationManager.currentLanguage.first()
    }

    val filterText = localizationManager.getString(R.string.filter, language)

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchBar(
                        text = searchText,
                        onTextChange = {
                            searchText = it;
                            viewModel.searchIngredients(searchText)
                        },
                        modifier = Modifier.weight(1f),
                        language = language
                    )
                    IconButton(onClick = { showingFilter = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = filterText)
                    }
                }
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    CategoryFilterView(
                        selectedCategory = selectedCategory,
                        selectedCategories = selectedCategories,
                        onCategoryChanged = {
                            selectedCategory = it
                            if (it == null) {
                                // Clear all filters when "All" is clicked
                                viewModel.clearAllFilters()
                            } else {
                                val categoryEnum =
                                    IngredientCategory.entries.find { entry -> entry.name == it }
                                viewModel.filterByCategory(categoryEnum?.name ?: "")
                            }
                        },
                        language = language
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = modifier.padding(innerPadding)) {
            when {
                isLoading && ingredients.isEmpty() -> LoadingView(loadingText)
                ingredients.isEmpty() -> EmptyStateView(noDataText)
                else -> IngredientListContent(
                    ingredients = ingredients,
                    isLoading = isLoading,
                    hasMorePages = hasMorePages,
                    onLoadMore = { viewModel.loadMoreIngredients() },
                    onIngredientClick = { ingredient ->
                        // Navigate to Ingredient Detail Screen
                        navController.navigate("ingredient_detail/${ingredient.id}")
                    },
                    language = language
                )
            }
            if (errorMessage != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearError() },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text(localizationManager.getString(R.string.ok, language))
                        }
                    },
                    title = { Text(localizationManager.getString(R.string.error, language)) },
                    text = { Text(errorMessage ?: "") }
                )
            }
            // Show filter as bottom sheet
            if (showingFilter) {
                ModalBottomSheet(
                    onDismissRequest = { showingFilter = false },
                    sheetState = bottomSheetState
                ) {
                    IngredientFilterScreen(
                        selectedCategories = selectedCategories,
                        onApplyFilter = { cats ->
                            viewModel.applyFilter(cats)
                            showingFilter = false
                        },
                        onCancel = { showingFilter = false },
                        language = language,
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ingredients.isEmpty()) {
            viewModel.loadIngredients()
        }
    }
}
