package com.fxanhkhoa.what_to_eat_android.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fxanhkhoa.what_to_eat_android.data.dto.QueryIngredientDto
import com.fxanhkhoa.what_to_eat_android.model.APIPagination
import com.fxanhkhoa.what_to_eat_android.model.Ingredient
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider
import com.fxanhkhoa.what_to_eat_android.services.IngredientService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class IngredientListViewModel : ViewModel() {
    private val ingredientService = RetrofitProvider.createService<IngredientService>()
    private var searchDebounceJob: Job? = null

    private val itemsPerPage = 20
    private var currentPage = 1
    private var currentKeyword: String? = null

    private val _ingredients = MutableStateFlow<List<Ingredient>>(emptyList())
    val ingredients: StateFlow<List<Ingredient>> = _ingredients

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories: StateFlow<Set<String>> = _selectedCategories

    private val _hasMorePages = MutableStateFlow(true)
    val hasMorePages: StateFlow<Boolean> = _hasMorePages

    // Load initial ingredients
    fun loadIngredients() {
        if (_isLoading.value) return
        currentPage = 1
        currentKeyword = null
        _ingredients.value = emptyList()
        fetchIngredients()
    }

    // Search ingredients by keyword
    fun searchIngredients(keyword: String) {
        searchDebounceJob?.cancel()

        // Start new debounce job
        searchDebounceJob = viewModelScope.launch {
            delay(500) // 500ms debounce
            if (keyword.trim().isEmpty()) {
                loadIngredients()
            } else {
                currentPage = 1
                currentKeyword = keyword.trim()
                _ingredients.value = emptyList()
                fetchIngredients()
            }
        }
    }

    // Filter ingredients by category
    fun filterByCategory(category: String?) {
        currentPage = 1
        currentKeyword = null
        _ingredients.value = emptyList()
        if (category != null) {
            _selectedCategories.value = setOf(category)
        } else {
            _selectedCategories.value = emptySet()
        }
        fetchIngredients()
    }

    // Apply filter with multiple categories
    fun applyFilter(categories: Set<String>) {
        currentPage = 1
        currentKeyword = null
        _ingredients.value = emptyList()
        _selectedCategories.value = categories
        fetchIngredients()
    }

    // Load more ingredients for pagination
    fun loadMoreIngredients() {
        if (_isLoading.value || !_hasMorePages.value) return
        currentPage += 1
        fetchIngredients(append = true)
    }

    // Clear error message
    fun clearError() {
        _errorMessage.value = null
    }

    // Refresh ingredients
    fun refreshIngredients() {
        currentPage = 1
        _ingredients.value = emptyList()
        fetchIngredients()
    }

    // Private Methods
    private fun fetchIngredients(append: Boolean = false) {
        _isLoading.value = true
        val queryDto = QueryIngredientDto(
            page = currentPage,
            limit = itemsPerPage,
            keyword = currentKeyword,
            ingredientCategories = if (_selectedCategories.value.isNotEmpty()) _selectedCategories.value.toList() else null
        )
        viewModelScope.launch {
            try {
                val fullUrl = queryDto.buildIngredientUrl()
                val response = ingredientService.findAllWithUrl(fullUrl)
                handleIngredientsResponse(response, append)
            } catch (e: Exception) {
                _isLoading.value = false
                handleError(e, append)
            }
        }
    }

    private fun handleIngredientsResponse(response: APIPagination<Ingredient>?, append: Boolean) {
        if (response == null || response.data == null) {
            _isLoading.value = false
            _ingredients.value = emptyList()
            _hasMorePages.value = false
            return
        }
        if (append) {
            _ingredients.value = _ingredients.value + response.data
        } else {
            _ingredients.value = response.data
        }
        _hasMorePages.value = currentPage * itemsPerPage < response.count
        _isLoading.value = false
    }

    private fun handleError(error: Exception, append: Boolean = false) {
        if (currentKeyword == null && _selectedCategories.value.isEmpty() && currentPage == 1 && !append) {
            _ingredients.value = emptyList()
            return
        }
        _errorMessage.value = getErrorMessage(error)
    }

    private fun getErrorMessage(error: Exception): String {
        return when (error) {
            is IOException -> "Network error: ${error.localizedMessage ?: "Unknown"}"
            is HttpException -> "Server error: ${error.code()}"
            else -> "An unexpected error occurred"
        }
    }

    // Convenience Methods
    fun loadRandomIngredients(limit: Int = 10, categories: List<String>? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val randomIngredients = ingredientService.findRandom(limit, categories)
                _ingredients.value = randomIngredients
                _hasMorePages.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                handleError(e)
            }
        }
    }

    val hasActiveFilters: Boolean
        get() = _selectedCategories.value.isNotEmpty() || currentKeyword != null

    val activeFilterDescription: String
        get() {
            val descriptions = mutableListOf<String>()
            currentKeyword?.let { descriptions.add("Keyword: $it") }
            if (_selectedCategories.value.isNotEmpty()) {
                descriptions.add("Categories: ${_selectedCategories.value.size}")
            }
            return descriptions.joinToString(", ")
        }

    fun clearAllFilters() {
        currentKeyword = null
        _selectedCategories.value = emptySet()
        loadIngredients()
    }
}