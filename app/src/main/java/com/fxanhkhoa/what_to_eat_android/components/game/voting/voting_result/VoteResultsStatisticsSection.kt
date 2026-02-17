package com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.model.DishVoteModel
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.utils.DateUtil

@Composable
fun VoteResultsStatisticsSection(
    dishVote: DishVoteModel,
    totalVotesCount: Int,
    language: Language,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(15.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(15.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = localizationManager.getString(R.string.vote_statistics, language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Statistics Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Total Votes Card
            StatisticCard(
                title = localizationManager.getString(R.string.total_votes, language),
                value = "$totalVotesCount",
                icon = Icons.Default.ThumbUp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )

            // Total Dishes Card
            StatisticCard(
                title = localizationManager.getString(R.string.total_dishes, language),
                value = "${dishVote.dishVoteItems.size}",
                icon = Icons.Default.RestaurantMenu,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )

            // Created Date Card
            StatisticCard(
                title = localizationManager.getString(R.string.created, language),
                value = DateUtil.formatDate(dishVote.createdAt, language),
                icon = Icons.Default.DateRange,
                color = Color.Gray,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
