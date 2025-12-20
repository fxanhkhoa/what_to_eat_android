package com.fxanhkhoa.what_to_eat_android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fxanhkhoa.what_to_eat_android.model.DishVoteFilter
import com.fxanhkhoa.what_to_eat_android.model.DishVoteModel
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider
import com.fxanhkhoa.what_to_eat_android.services.DishVoteService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VoteGameListViewModel : ViewModel() {

    private val dishVoteService = RetrofitProvider.createService<DishVoteService>()

    // Published state properties
    private val _voteGames = MutableStateFlow<List<DishVoteModel>>(emptyList())
    val voteGames: StateFlow<List<DishVoteModel>> = _voteGames.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _hasMorePages = MutableStateFlow(false)
    val hasMorePages: StateFlow<Boolean> = _hasMorePages.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    // Filter properties
    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword: StateFlow<String> = _searchKeyword.asStateFlow()

    private val _sortBy = MutableStateFlow("createdAt")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    private val _sortOrder = MutableStateFlow("desc")
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    private val _showingVoteGame = MutableStateFlow(false)
    val showingVoteGame: StateFlow<Boolean> = _showingVoteGame.asStateFlow()

    private val pageSize = 20
    private var searchDebounceJob: Job? = null

    // MARK: - Computed Properties

    val hasActiveFilters: Boolean
        get() = _searchKeyword.value.isNotEmpty() ||
                _sortBy.value != "createdAt" ||
                _sortOrder.value != "desc"

    // MARK: - Public Methods

    fun loadVoteGames() {
        if (_isLoading.value) return

        _isLoading.value = true
        _currentPage.value = 1
        _errorMessage.value = null

        val filter = DishVoteFilter(
            keyword = if (_searchKeyword.value.isEmpty()) null else _searchKeyword.value,
            page = _currentPage.value,
            limit = pageSize,
            sortBy = _sortBy.value,
            sortOrder = _sortOrder.value
        )

        viewModelScope.launch {
            try {
                val response = dishVoteService.findAll(
                    page = filter.page,
                    limit = filter.limit,
                    sortBy = filter.sortBy,
                    sortOrder = filter.sortOrder,
                    keyword = filter.keyword
                )

                _voteGames.value = response.data
                _hasMorePages.value = response.count >= pageSize
            } catch (e: Exception) {
                _errorMessage.value = when (e) {
                    is java.net.UnknownHostException -> "No internet connection"
                    is java.net.SocketTimeoutException -> "Request timed out"
                    is java.io.IOException -> "Network error: ${e.message}"
                    else -> e.localizedMessage ?: "Failed to load vote games"
                }
                android.util.Log.e("VoteGameListViewModel", "Error loading vote games", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun refreshVoteGames() {
        _currentPage.value = 1
        loadVoteGames()
    }

    fun loadMoreVoteGames() {
        if (_isLoading.value || !_hasMorePages.value) return

        _isLoading.value = true
        val nextPage = _currentPage.value + 1

        val filter = DishVoteFilter(
            keyword = if (_searchKeyword.value.isEmpty()) null else _searchKeyword.value,
            page = nextPage,
            limit = pageSize,
            sortBy = _sortBy.value,
            sortOrder = _sortOrder.value
        )

        viewModelScope.launch {
            try {
                val response = dishVoteService.findAll(
                    page = filter.page,
                    limit = filter.limit,
                    sortBy = filter.sortBy,
                    sortOrder = filter.sortOrder,
                    keyword = filter.keyword
                )

                _voteGames.value = _voteGames.value + response.data
                _hasMorePages.value = response.count >= pageSize
                _currentPage.value = nextPage
            } catch (e: Exception) {
                _errorMessage.value = when (e) {
                    is java.net.UnknownHostException -> "No internet connection"
                    is java.net.SocketTimeoutException -> "Request timed out"
                    is java.io.IOException -> "Network error: ${e.message}"
                    else -> e.localizedMessage ?: "Failed to load more vote games"
                }
                android.util.Log.e("VoteGameListViewModel", "Error loading more vote games", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchKeyword(keyword: String) {
        _searchKeyword.value = keyword

        // Debounce search to avoid too many API calls
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(500) // 500ms debounce
            loadVoteGames()
        }
    }

    fun applySorting(sortBy: String, sortOrder: String) {
        _sortBy.value = sortBy
        _sortOrder.value = sortOrder
        loadVoteGames()
    }

    fun clearFilters() {
        _searchKeyword.value = ""
        _sortBy.value = "createdAt"
        _sortOrder.value = "desc"
        loadVoteGames()
    }

    fun setShowingVoteGame(showing: Boolean) {
        _showingVoteGame.value = showing
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // Clean up when ViewModel is destroyed
    override fun onCleared() {
        super.onCleared()
        searchDebounceJob?.cancel()
    }
}

