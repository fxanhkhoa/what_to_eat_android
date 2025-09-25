package com.fxanhkhoa.what_to_eat_android.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fxanhkhoa.what_to_eat_android.data.model.QueryDishDto
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.services.DishService
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider
import com.fxanhkhoa.what_to_eat_android.shared.DifficultyLevel
import com.fxanhkhoa.what_to_eat_android.shared.MealCategory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DishListViewModel : ViewModel() {

    private val dishService = RetrofitProvider.createService<DishService>()

    // Published state properties
    private val _dishes = MutableStateFlow<List<DishModel>>(emptyList())
    val dishes: StateFlow<List<DishModel>> = _dishes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _filters = MutableStateFlow(DishFilters())
    val filters: StateFlow<DishFilters> = _filters.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _hasMorePages = MutableStateFlow(true)
    val hasMorePages: StateFlow<Boolean> = _hasMorePages.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Private properties
    private val pageSize = 20
    private var searchDebounceJob: Job? = null

    // Computed properties as StateFlows for reactivity
    val hasActiveFilters: StateFlow<Boolean> = _filters.map { !it.isEmpty() }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val activeFilterChips: StateFlow<List<FilterChipData>> = _filters.map { currentFilters ->
        val chips = mutableListOf<FilterChipData>()

        // Meal categories - convert display names back to enum values for removal
        currentFilters.mealCategories.forEach { categoryDisplayName ->
            chips.add(FilterChipData(
                title = categoryDisplayName,
                onRemove = {
                    updateFilters { filters ->
                        filters.copy(mealCategories = filters.mealCategories - categoryDisplayName)
                    }
                    applyFilters()
                }
            ))
        }

        // Difficulty levels - convert display names back to enum values for removal
        currentFilters.difficultLevels.forEach { levelDisplayName ->
            chips.add(FilterChipData(
                title = levelDisplayName,
                onRemove = {
                    updateFilters { filters ->
                        filters.copy(difficultLevels = filters.difficultLevels - levelDisplayName)
                    }
                    applyFilters()
                }
            ))
        }

        // Tags
        currentFilters.tags.forEach { tag ->
            chips.add(FilterChipData(
                title = "#$tag",
                onRemove = {
                    updateFilters { filters ->
                        filters.copy(tags = filters.tags - tag)
                    }
                    applyFilters()
                }
            ))
        }

        // Time ranges
        if (currentFilters.preparationTimeFrom != null || currentFilters.preparationTimeTo != null) {
            val fromTime = currentFilters.preparationTimeFrom ?: 0
            val toTime = currentFilters.preparationTimeTo ?: 999
            chips.add(FilterChipData(
                title = "Prep: ${fromTime}-${toTime}min",
                onRemove = {
                    updateFilters { filters ->
                        filters.copy(
                            preparationTimeFrom = null,
                            preparationTimeTo = null
                        )
                    }
                    applyFilters()
                }
            ))
        }

        if (currentFilters.cookingTimeFrom != null || currentFilters.cookingTimeTo != null) {
            val fromTime = currentFilters.cookingTimeFrom ?: 0
            val toTime = currentFilters.cookingTimeTo ?: 999
            chips.add(FilterChipData(
                title = "Cook: ${fromTime}-${toTime}min",
                onRemove = {
                    updateFilters { filters ->
                        filters.copy(
                            cookingTimeFrom = null,
                            cookingTimeTo = null
                        )
                    }
                    applyFilters()
                }
            ))
        }

        chips
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun loadDishes(query: QueryDishDto) {
        if (_isLoading.value) return

        _isLoading.value = true
        _currentPage.value = 1

        viewModelScope.launch {
            try {
                // Use the new URL-based approach for search/fuzzy endpoint
                val url = query.buildSearchFuzzyUrl()
                Log.d("DishListViewModel", "Loading dishes with URL: $url")

                val response = dishService.searchFuzzy(url)
                Log.d("DishListViewModel", "Raw API Response: $response")
                Log.d("DishListViewModel", "Response data field: ${response.data}")
                Log.d("DishListViewModel", "Response count field: ${response.count}")

                _dishes.value = response.data ?: emptyList()
                _hasMorePages.value = (response.data?.size ?: 0) >= pageSize
                _currentPage.value = 1
                _errorMessage.value = null
            } catch (e: Exception) {
                _dishes.value = emptyList()
                _errorMessage.value = e.localizedMessage
                Log.e("DishListViewModel", "Error loading dishes", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMoreDishes() {
        if (_isLoading.value || !_hasMorePages.value) return

        _isLoading.value = true
        val nextPage = _currentPage.value + 1

        val query = createQueryDto(page = nextPage)

        viewModelScope.launch {
            try {
                // Use the new URL-based approach
                val url = query.buildDishUrl()
                val response = dishService.findAll(url)
                val newDishes = response.data ?: emptyList()
                _dishes.value = _dishes.value + newDishes
                _hasMorePages.value = newDishes.size >= pageSize
                _currentPage.value = nextPage
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun refreshDishes() {
        _currentPage.value = 1
        _hasMorePages.value = true

        try {
            val query = createQueryDto()
            // Use the new URL-based approach
            val url = query.buildDishUrl()
            val response = dishService.findAll(url)

            _dishes.value = response.data ?: emptyList()
            _hasMorePages.value = (response.data?.size ?: 0) >= pageSize
            _currentPage.value = 1
            _errorMessage.value = null
        } catch (e: Exception) {
            _errorMessage.value = e.localizedMessage
        }
    }

    fun applyFilters() {
        _currentPage.value = 1
        _hasMorePages.value = true
        val query = createQueryDto()
        Log.d("DishListViewModel", "Applying filters with query: $query")
        loadDishes(query)
    }

    fun clearAllFilters() {
        _filters.value = DishFilters()
        applyFilters()
    }

    fun updateSearchKeyword(keyword: String) {
        // Cancel previous debounce job
        searchDebounceJob?.cancel()

        // Start new debounce job
        searchDebounceJob = viewModelScope.launch {
            delay(500) // 500ms debounce
            updateFilters { filters ->
                filters.copy(keyword = keyword.takeIf { it.isNotEmpty() })
            }
            applyFilters()
        }
    }

    fun updateFilters(update: (DishFilters) -> DishFilters) {
        _filters.value = update(_filters.value)
    }

    // Helper methods for working with enums
    fun addMealCategoryFilter(category: MealCategory) {
        updateFilters { filters ->
            val updatedCategories = filters.mealCategories + category.displayName
            filters.copy(mealCategories = updatedCategories.distinct())
        }
        applyFilters()
    }

    fun removeMealCategoryFilter(category: MealCategory) {
        updateFilters { filters ->
            filters.copy(mealCategories = filters.mealCategories - category.displayName)
        }
        applyFilters()
    }

    fun addDifficultyLevelFilter(level: DifficultyLevel) {
        updateFilters { filters ->
            val updatedLevels = filters.difficultLevels + level.displayName
            filters.copy(difficultLevels = updatedLevels.distinct())
        }
        applyFilters()
    }

    fun removeDifficultyLevelFilter(level: DifficultyLevel) {
        updateFilters { filters ->
            filters.copy(difficultLevels = filters.difficultLevels - level.displayName)
        }
        applyFilters()
    }

    // Convert display names back to raw values for API calls
    private fun createQueryDto(page: Int? = null): QueryDishDto {
        val currentFilters = _filters.value

        // Convert display names back to raw values for API
        val convertedDifficultLevels = currentFilters.difficultLevels.mapNotNull { displayName ->
            val found = DifficultyLevel.entries.find { it.displayName == displayName }
            Log.d("DishListViewModel", "Converting difficulty '$displayName' -> '${found?.rawValue}'")
            found?.rawValue
        }.takeIf { it.isNotEmpty() }

        val convertedMealCategories = currentFilters.mealCategories.mapNotNull { displayName ->
            val found = MealCategory.entries.find { it.displayName == displayName }
            Log.d("DishListViewModel", "Converting meal category '$displayName' -> '${found?.rawValue}'")
            found?.rawValue
        }.takeIf { it.isNotEmpty() }

        val query = QueryDishDto(
            page = page ?: _currentPage.value,
            limit = pageSize,
            keyword = currentFilters.keyword,
            tags = currentFilters.tags.takeIf { it.isNotEmpty() },
            preparationTimeFrom = currentFilters.preparationTimeFrom,
            preparationTimeTo = currentFilters.preparationTimeTo,
            cookingTimeFrom = currentFilters.cookingTimeFrom,
            cookingTimeTo = currentFilters.cookingTimeTo,
            difficultLevels = convertedDifficultLevels,
            mealCategories = convertedMealCategories,
            ingredientCategories = currentFilters.ingredientCategories.takeIf { it.isNotEmpty() },
            ingredients = currentFilters.ingredients.takeIf { it.isNotEmpty() },
            labels = currentFilters.labels.takeIf { it.isNotEmpty() }
        )

        Log.d("DishListViewModel", "Created QueryDto: $query")
        Log.d("DishListViewModel", "Converted difficulty levels: $convertedDifficultLevels")
        Log.d("DishListViewModel", "Converted meal categories: $convertedMealCategories")

        return query
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun searchDishes() {
        _isLoading.value = true
        _currentPage.value = 1

        viewModelScope.launch {
            try {
                val query = createQueryDto()
                // Use the new URL-based approach
                val url = query.buildDishUrl()
                val response = dishService.findAll(url)

                Log.d("DishListViewModel", "Data is null: ${response.data == null}")
                Log.d("DishListViewModel", "Data size: ${response.data?.size ?: "null"}")

                _dishes.value = response.data ?: emptyList()
                _hasMorePages.value = (response.data?.size ?: 0) >= pageSize
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e("DishListViewModel", "Error searching dishes", e)
                _errorMessage.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// MARK: - Supporting Types
data class DishFilters(
    val keyword: String? = null,
    val tags: List<String> = emptyList(),
    val preparationTimeFrom: Int? = null,
    val preparationTimeTo: Int? = null,
    val cookingTimeFrom: Int? = null,
    val cookingTimeTo: Int? = null,
    val difficultLevels: List<String> = emptyList(),
    val mealCategories: List<String> = emptyList(),
    val ingredientCategories: List<String> = emptyList(),
    val ingredients: List<String> = emptyList(),
    val labels: List<String> = emptyList()
) {
    fun isEmpty(): Boolean {
        return keyword == null &&
                tags.isEmpty() &&
                preparationTimeFrom == null &&
                preparationTimeTo == null &&
                cookingTimeFrom == null &&
                cookingTimeTo == null &&
                difficultLevels.isEmpty() &&
                mealCategories.isEmpty() &&
                ingredientCategories.isEmpty() &&
                ingredients.isEmpty() &&
                labels.isEmpty()
    }
}

data class FilterChipData(
    val title: String,
    val onRemove: () -> Unit
)
