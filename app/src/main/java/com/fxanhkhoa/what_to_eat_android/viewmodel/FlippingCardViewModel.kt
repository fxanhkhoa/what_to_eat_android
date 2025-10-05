package com.fxanhkhoa.what_to_eat_android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider
import com.fxanhkhoa.what_to_eat_android.services.DishService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

// Game state enum
enum class GameState {
    NOT_STARTED,
    PLAYING,
    COMPLETED
}

// Game card data class
data class GameCard(
    val id: String = UUID.randomUUID().toString(),
    val dish: DishModel,
    var isFlipped: Boolean = false
)

class FlippingCardViewModel : ViewModel() {

    private val dishService = RetrofitProvider.createService<DishService>()

    // Published state properties
    private val _dishes = MutableStateFlow<List<DishModel>>(emptyList())
    val dishes: StateFlow<List<DishModel>> = _dishes.asStateFlow()

    private val _selectedDishes = MutableStateFlow<List<DishModel>>(emptyList())
    val selectedDishes: StateFlow<List<DishModel>> = _selectedDishes.asStateFlow()

    private val _cards = MutableStateFlow<List<GameCard>>(emptyList())
    val cards: StateFlow<List<GameCard>> = _cards.asStateFlow()

    private val _gameState = MutableStateFlow(GameState.NOT_STARTED)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedDish = MutableStateFlow<DishModel?>(null)
    val selectedDish: StateFlow<DishModel?> = _selectedDish.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Game configuration
    private val _numberOfCards = MutableStateFlow(12) // Default: 3x4 grid
    val numberOfCards: StateFlow<Int> = _numberOfCards.asStateFlow()

    // MARK: - Public Methods

    fun updateNumberOfCards(count: Int) {
        _numberOfCards.value = count
        // Reset game when card count changes
        if (_dishes.value.isNotEmpty()) {
            resetGame()
            setupGame()
        }
    }

    fun updateSelectedDish(dish: DishModel?) {
        _selectedDish.value = dish
    }

    fun updateGameState(state: GameState) {
        _gameState.value = state
    }

    fun loadDishes() {
        if (_isLoading.value) return

        if (_selectedDishes.value.isEmpty()) {
            // Load random dishes if no dishes are selected
            _isLoading.value = true

            viewModelScope.launch {
                try {
                    val randomDishes = dishService.findRandom(limit = _numberOfCards.value)
                    _dishes.value = randomDishes
                    _selectedDishes.value = randomDishes // Sync with selected dishes
                    setupGame()
                } catch (e: Exception) {
                    handleError(e)
                } finally {
                    _isLoading.value = false
                }
            }
        } else {
            // Use selected dishes
            _dishes.value = _selectedDishes.value
            setupGame()
        }
    }

    fun updateSelectedDishes(newDishes: List<DishModel>) {
        _selectedDishes.value = newDishes
        _dishes.value = newDishes
        resetGame()
        if (newDishes.isNotEmpty()) {
            setupGame()
        }
    }

    fun removeDishFromGame(dish: DishModel) {
        val updatedDishes = _selectedDishes.value.filter { it.id != dish.id }
        updateSelectedDishes(updatedDishes)
    }

    fun cardTapped(card: GameCard) {
        if (_gameState.value != GameState.PLAYING || card.isFlipped) {
            return
        }

        // Flip the selected card
        val cardIndex = _cards.value.indexOfFirst { it.id == card.id }
        if (cardIndex != -1) {
            val updatedCards = _cards.value.toMutableList()
            updatedCards[cardIndex] = updatedCards[cardIndex].copy(isFlipped = true)
            _cards.value = updatedCards
            _selectedDish.value = card.dish
            _gameState.value = GameState.COMPLETED
        }
    }

    fun startNewGame() {
        resetGame()
        loadDishes()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // MARK: - Private Methods

    private fun setupGame() {
        if (_dishes.value.isEmpty()) {
            _errorMessage.value = null // Don't show error for empty selection
            return
        }

        // Create cards for available dishes (up to numberOfCards)
        val gameCards = _dishes.value.take(_numberOfCards.value).map { dish ->
            GameCard(dish = dish, isFlipped = false)
        }

        // Shuffle the cards
        _cards.value = gameCards.shuffled()

        _gameState.value = GameState.PLAYING
    }

    private fun resetGame() {
        _selectedDish.value = null
        _gameState.value = GameState.NOT_STARTED
        _cards.value = emptyList()
    }

    private fun handleError(error: Throwable) {
        _errorMessage.value = when (error) {
            is java.net.UnknownHostException -> "No internet connection"
            is java.net.SocketTimeoutException -> "Request timed out"
            is java.io.IOException -> "Network error: ${error.message}"
            else -> "Failed to load dishes: ${error.localizedMessage}"
        }
        android.util.Log.e("FlippingCardViewModel", "Error: ${error.message}", error)
    }
}
