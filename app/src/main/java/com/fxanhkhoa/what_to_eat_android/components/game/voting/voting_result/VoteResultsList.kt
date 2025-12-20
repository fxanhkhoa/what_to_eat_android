package com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_result

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager

@Composable
fun VoteResultsList(
    enrichedVoteResults: List<EnrichedVoteResult>,
    maxVotes: Int,
    language: Language,
    onDishClick: (DishModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = localizationManager.getString(R.string.all_results, language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Results List - each result as a separate composable, not in a LazyColumn
        enrichedVoteResults.forEachIndexed { index, result ->
            EnrichedVoteResultCard(
                result = result,
                maxVotes = maxVotes,
                isWinner = result.totalVotes == maxVotes && maxVotes > 0,
                language = language,
                onDishClick = onDishClick
            )
            if (index < enrichedVoteResults.size - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
