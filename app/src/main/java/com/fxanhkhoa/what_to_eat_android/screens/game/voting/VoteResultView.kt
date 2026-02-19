package com.fxanhkhoa.what_to_eat_android.screens.game.voting

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_result.EnrichedVoteResult
import com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_result.VoteResultsHeader
import com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_result.VoteResultsList
import com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_result.VoteResultsStatisticsSection
import com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_result.VoteResultsWinner
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.model.DishVoteModel
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.viewmodel.VoteResultsViewModel
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteResultsView(
    dishVote: DishVoteModel,
    onDismiss: () -> Unit,
    onDishClick: (DishModel) -> Unit = {},
    viewModel: VoteResultsViewModel = viewModel()
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    var language by remember { mutableStateOf(Language.ENGLISH) }

    // Observe language changes
    LaunchedEffect(Unit) {
        language = localizationManager.currentLanguage.first()
    }

    // Initialize ViewModel with dish vote data
    LaunchedEffect(dishVote) {
        viewModel.loadDishesData(dishVote)
    }

    val enrichedVoteResults by viewModel.enrichedVoteResults.collectAsState()
    val isLoadingDishes by viewModel.isLoadingDishes.collectAsState()

    // Calculate statistics
    val winner = enrichedVoteResults.firstOrNull()
    val totalVotesCount = enrichedVoteResults.sumOf { it.totalVotes }
    val maxVotes = enrichedVoteResults.maxOfOrNull { it.totalVotes } ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = localizationManager.getString(R.string.view_results, language),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localizationManager.getString(R.string.back, language)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            shareResults(
                                context = context,
                                dishVote = dishVote,
                                enrichedVoteResults = enrichedVoteResults,
                                winner = winner,
                                totalVotesCount = totalVotesCount,
                                localizationManager = localizationManager,
                                language = language
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = localizationManager.getString(R.string.share, language),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    // Header Section
                    VoteResultsHeader(
                        dishVote = dishVote
                    )
                }

                item {
                    if (isLoadingDishes) {
                        // Loading state
                        LoadingView(
                            localizationManager = localizationManager,
                            language = language
                        )
                    } else {
                        // Winner Section (Optional - commented out in Swift version)
                        if (winner != null && winner.totalVotes > 0) {
                            VoteResultsWinner(
                                winner = winner,
                                maxVotes = maxVotes,
                                language = language,
                                onDishClick = onDishClick
                            )
                        }
                    }
                }

                item {
                    // All Results Section
                    VoteResultsList(
                        enrichedVoteResults = enrichedVoteResults,
                        maxVotes = maxVotes,
                        language = language,
                        onDishClick = onDishClick
                    )
                }

                item {
                    // Vote Statistics
                    VoteResultsStatisticsSection(
                        dishVote = dishVote,
                        totalVotesCount = totalVotesCount,
                        language = language
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingView(
    localizationManager: LocalizationManager,
    language: Language,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = localizationManager.getString(R.string.loading, language),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun shareResults(
    context: android.content.Context,
    dishVote: DishVoteModel,
    enrichedVoteResults: List<EnrichedVoteResult>,
    winner: EnrichedVoteResult?,
    totalVotesCount: Int,
    localizationManager: LocalizationManager,
    language: Language
) {
    val shareText = generateShareText(
        dishVote = dishVote,
        enrichedVoteResults = enrichedVoteResults,
        winner = winner,
        totalVotesCount = totalVotesCount,
        localizationManager = localizationManager,
        language = language
    )

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(
        sendIntent,
        localizationManager.getString(R.string.share_vote_results, language)
    )

    context.startActivity(shareIntent)
}

private fun generateShareText(
    dishVote: DishVoteModel,
    enrichedVoteResults: List<EnrichedVoteResult>,
    winner: EnrichedVoteResult?,
    totalVotesCount: Int,
    localizationManager: LocalizationManager,
    language: Language
): String {
    val sb = StringBuilder()

    // Title
    sb.append(localizationManager.getString(R.string.share_voting_results, language))
    sb.append(": ")
    sb.append(dishVote.title)
    sb.append("\n\n")

    // Winner (if exists)
    if (winner != null && winner.totalVotes > 0) {
        val dishName = winner.displayName
        sb.append(localizationManager.getString(R.string.share_winner, language))
        sb.append(": ")
        sb.append(dishName)
        sb.append(" ")
        sb.append(localizationManager.getString(R.string.share_with, language))
        sb.append(" ")
        sb.append(winner.totalVotes)
        sb.append(" ")
        sb.append(localizationManager.getString(R.string.votes, language))
        sb.append("!\n\n")
    }

    // All Results
    sb.append(localizationManager.getString(R.string.share_all_results, language))
    sb.append(":\n")
    enrichedVoteResults.forEachIndexed { index, result ->
        val dishName = result.displayName
        sb.append("${index + 1}. ")
        sb.append(dishName)
        sb.append(": ")
        sb.append(result.totalVotes)
        sb.append(" ")
        sb.append(localizationManager.getString(R.string.votes, language))
        sb.append("\n")
    }

    // Total votes
    sb.append("\n")
    sb.append(localizationManager.getString(R.string.total_votes, language))
    sb.append(": ")
    sb.append(totalVotesCount)

    return sb.toString()
}
