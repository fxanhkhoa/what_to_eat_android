package com.fxanhkhoa.what_to_eat_android.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fxanhkhoa.what_to_eat_android.model.CreateUserDishCollectionDto
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.model.UpdateUserDishCollectionDto
import com.fxanhkhoa.what_to_eat_android.model.UserDishCollectionModel
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider
import com.fxanhkhoa.what_to_eat_android.services.DishService
import com.fxanhkhoa.what_to_eat_android.services.UserDishCollectionService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserDishCollectionFormViewModel : ViewModel() {

    private val service = RetrofitProvider.createService<UserDishCollectionService>()
    private val dishService = RetrofitProvider.createService<DishService>()

    private val _collection = MutableStateFlow<UserDishCollectionModel?>(null)
    val collection: StateFlow<UserDishCollectionModel?> = _collection.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _searchResults = MutableStateFlow<List<DishModel>>(emptyList())
    val searchResults: StateFlow<List<DishModel>> = _searchResults.asStateFlow()

    private val _selectedDishes = MutableStateFlow<List<DishModel>>(emptyList())
    val selectedDishes: StateFlow<List<DishModel>> = _selectedDishes.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private var searchJob: Job? = null

    fun loadCollection(id: String) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val col = service.findById(id)
                _collection.value = col
                // Load DishModel for each slug
                val dishes = mutableListOf<DishModel>()
                val colSlugs = col.dishSlugs ?: emptyList()
                for (slug in colSlugs) {
                    try {
                        dishes.add(dishService.findBySlug(slug))
                    } catch (e: Exception) {
                        Log.w("UserDishCollectionFormVM", "Could not load dish $slug", e)
                    }
                }
                _selectedDishes.value = dishes
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
                Log.e("UserDishCollectionFormVM", "loadCollection error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _searchResults.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(500)
            try {
                val url = "https://api.eatwhat.io.vn/dish/search/fuzzy?keyword=${query.trim()}&page=1&limit=12"
                val response = dishService.searchFuzzy(url)
                _searchResults.value = response.data ?: emptyList()
            } catch (e: Exception) {
                Log.e("UserDishCollectionFormVM", "searchDishes error", e)
                _searchResults.value = emptyList()
            }
        }
    }

    fun addDish(dish: DishModel) {
        if (_selectedDishes.value.none { it.slug == dish.slug }) {
            _selectedDishes.value = _selectedDishes.value + dish
        }
    }

    fun removeDish(slug: String) {
        _selectedDishes.value = _selectedDishes.value.filter { it.slug != slug }
    }

    fun save(
        name: String,
        description: String,
        occasion: String,
        icon: String,
        color: String,
        isPublic: Boolean,
        userId: String,
        collectionId: String?,
        sortOrder: Int = 0
    ) {
        if (name.isBlank()) {
            _errorMessage.value = "Name is required"
            return
        }
        _isSaving.value = true
        _errorMessage.value = null
        val slugs = _selectedDishes.value.map { it.slug }
        viewModelScope.launch {
            try {
                if (collectionId == null) {
                    service.create(
                        CreateUserDishCollectionDto(
                            userId = userId,
                            name = name,
                            description = description.takeIf { it.isNotBlank() },
                            occasion = occasion.takeIf { it.isNotBlank() },
                            dishSlugs = slugs,
                            isPublic = isPublic,
                            icon = icon,
                            color = color,
                            sortOrder = sortOrder
                        )
                    )
                } else {
                    service.update(
                        collectionId,
                        UpdateUserDishCollectionDto(
                            _id = collectionId,
                            userId = userId,
                            name = name,
                            description = description.takeIf { it.isNotBlank() },
                            occasion = occasion.takeIf { it.isNotBlank() },
                            dishSlugs = slugs,
                            isPublic = isPublic,
                            icon = icon,
                            color = color,
                            sortOrder = sortOrder
                        )
                    )
                }
                _saveSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
                Log.e("UserDishCollectionFormVM", "save error", e)
            } finally {
                _isSaving.value = false
            }
        }
    }
}
