package com.fxanhkhoa.what_to_eat_android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fxanhkhoa.what_to_eat_android.model.CreateDishVoteDto
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.model.DishVoteItem
import com.fxanhkhoa.what_to_eat_android.model.DishVoteModel
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider
import com.fxanhkhoa.what_to_eat_android.services.DishVoteService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class VotingGameCreateViewModel : ViewModel() {

    private val dishVoteService = RetrofitProvider.createService<DishVoteService>()

    // Published state properties
    private val _voteTitle = MutableStateFlow("")
    val voteTitle: StateFlow<String> = _voteTitle.asStateFlow()

    private val _voteDescription = MutableStateFlow("")
    val voteDescription: StateFlow<String> = _voteDescription.asStateFlow()

    private val _selectedDishes = MutableStateFlow<List<DishVoteItem>>(emptyList())
    val selectedDishes: StateFlow<List<DishVoteItem>> = _selectedDishes.asStateFlow()

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showSuccess = MutableStateFlow(false)
    val showSuccess: StateFlow<Boolean> = _showSuccess.asStateFlow()

    private val _createdVote = MutableStateFlow<DishVoteModel?>(null)
    val createdVote: StateFlow<DishVoteModel?> = _createdVote.asStateFlow()

    // MARK: - Computed Properties

    val canCreateVote: StateFlow<Boolean> = MutableStateFlow(false).apply {
        viewModelScope.launch {
            _voteTitle.collect { title ->
                value = title.trim().isNotEmpty() && _selectedDishes.value.size >= 2
            }
        }
    }

    // MARK: - Methods

    fun updateVoteTitle(title: String) {
        _voteTitle.value = title
    }

    fun updateVoteDescription(description: String) {
        _voteDescription.value = description
    }

    fun addDish(dish: DishModel) {
        // Check if dish is already added
        val isDuplicate = _selectedDishes.value.any {
            !it.isCustom && it.slug == dish.slug
        }

        if (isDuplicate) {
            return
        }

        val dishVoteItem = DishVoteItem(
            slug = dish.slug,
            customTitle = null,
            voteUser = emptyList(),
            voteAnonymous = emptyList(),
            isCustom = false
        )

        _selectedDishes.value = _selectedDishes.value + dishVoteItem
    }

    fun addCustomDish(title: String, url: String? = null) {
        // Check if custom dish with same title is already added
        val isDuplicate = _selectedDishes.value.any {
            it.isCustom && it.customTitle == title
        }

        if (isDuplicate) {
            return
        }

        val customDishVoteItem = DishVoteItem(
            slug = url ?: title, // Use URL as slug if provided, otherwise use title
            customTitle = title,
            voteUser = emptyList(),
            voteAnonymous = emptyList(),
            isCustom = true
        )

        _selectedDishes.value = _selectedDishes.value + customDishVoteItem
    }

    fun removeDish(index: Int) {
        if (index < _selectedDishes.value.size) {
            _selectedDishes.value = _selectedDishes.value.toMutableList().apply {
                removeAt(index)
            }
        }
    }

    fun removeDish(dishVoteItem: DishVoteItem) {
        _selectedDishes.value = _selectedDishes.value.filter { it != dishVoteItem }
    }

    fun clearAllDishes() {
        _selectedDishes.value = emptyList()
    }

    fun createVote() {
        val trimmedTitle = _voteTitle.value.trim()
        val trimmedDescription = _voteDescription.value.trim()

        if (trimmedTitle.isEmpty() || _selectedDishes.value.size < 2) {
            return
        }

        _isCreating.value = true
        _errorMessage.value = null

        val createDto = CreateDishVoteDto(
            title = trimmedTitle,
            description = trimmedDescription.ifEmpty { null },
            dishVoteItems = _selectedDishes.value
        )

        viewModelScope.launch {
            try {
                val dishVote = dishVoteService.create(createDto)
                _createdVote.value = dishVote
                _showSuccess.value = true

                // Reset form after successful creation
                delay(1500)
                resetForm()
            } catch (e: Exception) {
                _errorMessage.value = when (e) {
                    is java.net.UnknownHostException -> "No internet connection"
                    is java.net.SocketTimeoutException -> "Request timed out"
                    is java.io.IOException -> "Network error: ${e.message}"
                    else -> e.localizedMessage ?: "Failed to create vote"
                }
                android.util.Log.e("VotingGameCreateViewModel", "Error creating vote", e)
            } finally {
                _isCreating.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _showSuccess.value = false
    }

    private fun resetForm() {
        _voteTitle.value = ""
        _voteDescription.value = ""
        _selectedDishes.value = emptyList()
        _showSuccess.value = false
        _createdVote.value = null
    }
}
