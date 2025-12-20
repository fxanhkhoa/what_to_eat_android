package com.fxanhkhoa.what_to_eat_android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_result.EnrichedVoteResult
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.model.DishVoteItem
import com.fxanhkhoa.what_to_eat_android.model.DishVoteModel
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider
import com.fxanhkhoa.what_to_eat_android.services.DishService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

/**
 * ViewModel for Vote Results Screen
 * Handles loading dish data and creating enriched vote results
 */
class VoteResultsViewModel : ViewModel() {

    private val dishService = RetrofitProvider.createService<DishService>()

    // State properties
    private val _enrichedVoteResults = MutableStateFlow<List<EnrichedVoteResult>>(emptyList())
    val enrichedVoteResults: StateFlow<List<EnrichedVoteResult>> = _enrichedVoteResults.asStateFlow()

    private val _isLoadingDishes = MutableStateFlow(true)
    val isLoadingDishes: StateFlow<Boolean> = _isLoadingDishes.asStateFlow()

    /**
     * Load dishes data and create enriched vote results
     */
    fun loadDishesData(dishVote: DishVoteModel) {
        viewModelScope.launch {
            _isLoadingDishes.value = true

            try {
                // Calculate basic vote results
                val voteResults = calculateVoteResults(dishVote)

                // Get unique non-custom dish slugs
                val dishSlugs = dishVote.dishVoteItems
                    .filter { !it.isCustom }
                    .map { it.slug }

                // Load dishes in parallel
                val dishes = if (dishSlugs.isNotEmpty()) {
                    loadDishesInParallel(dishSlugs)
                } else {
                    emptyList()
                }

                // Create enriched results
                val enrichedResults = createEnrichedResults(voteResults, dishes)

                _enrichedVoteResults.value = enrichedResults
            } catch (_: Exception) {
                // Handle error - create enriched results without dish data
                val voteResults = calculateVoteResults(dishVote)
                _enrichedVoteResults.value = createEnrichedResults(voteResults, emptyList())
            } finally {
                _isLoadingDishes.value = false
            }
        }
    }

    /**
     * Calculate vote results from dish vote items
     */
    private fun calculateVoteResults(dishVote: DishVoteModel): List<VoteResult> {
        val results = dishVote.dishVoteItems.map { item ->
            val totalVotes = item.voteUser.size + item.voteAnonymous.size
            VoteResult(
                dishVoteItem = item,
                totalVotes = totalVotes,
                userVotes = item.voteUser.size,
                anonymousVotes = item.voteAnonymous.size
            )
        }
        return results.sortedByDescending { it.totalVotes }
    }

    /**
     * Load multiple dishes in parallel
     */
    private suspend fun loadDishesInParallel(slugs: List<String>): List<DishModel> {
        return viewModelScope.async {
            slugs.map { slug ->
                async {
                    try {
                        dishService.findBySlug(slug)
                    } catch (_: Exception) {
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }.await()
    }

    /**
     * Create enriched results by combining vote results with dish data
     */
    private fun createEnrichedResults(
        voteResults: List<VoteResult>,
        dishes: List<DishModel>
    ): List<EnrichedVoteResult> {
        val dishMap = dishes.associateBy { it.slug }

        return voteResults.map { result ->
            val dish = if (result.dishVoteItem.isCustom) {
                null
            } else {
                dishMap[result.dishVoteItem.slug]
            }

            EnrichedVoteResult(
                dishVoteItem = result.dishVoteItem,
                dish = dish,
                totalVotes = result.totalVotes,
                userVotes = result.userVotes,
                anonymousVotes = result.anonymousVotes
            )
        }
    }

    /**
     * Data class to hold basic vote result information
     */
    private data class VoteResult(
        val dishVoteItem: DishVoteItem,
        val totalVotes: Int,
        val userVotes: Int,
        val anonymousVotes: Int
    )
}
