package com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.model.DishVoteItem
import com.fxanhkhoa.what_to_eat_android.model.DishVoteModel
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import kotlinx.coroutines.flow.first

/**
 * Real-time vote results component
 * Displays live voting results with animated progress bars
 */
@Composable
fun RealTimeVoteResults(
    voteGame: DishVoteModel,
    selectedDish: String?,
    dishes: List<DishModel>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    var language by remember { mutableStateOf(Language.ENGLISH) }

    // Observe language changes
    LaunchedEffect(Unit) {
        language = localizationManager.currentLanguage.first()
    }

    // Helper function to find dish by slug
    val dishForSlug: (String) -> DishModel? = { slug ->
        dishes.firstOrNull { it.slug == slug }
    }

    // Calculate total votes
    val totalVotes = remember(voteGame.dishVoteItems) {
        voteGame.dishVoteItems.sumOf { item ->
            item.voteUser.size + item.voteAnonymous.size
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Live Results", // TODO: Add localization
            style = MaterialTheme.typography.titleMedium
        )

        voteGame.dishVoteItems.forEach { item ->
            val dish = dishForSlug(item.slug)

            VoteResultRow(
                item = item,
                dish = dish,
                totalVotes = totalVotes,
                isSelected = selectedDish == item.slug
            )
        }
    }
}

@Composable
fun VoteResultRow(
    item: DishVoteItem,
    dish: DishModel?,
    totalVotes: Int,
    isSelected: Boolean
) {
    val animatedProgress = animateFloatAsState(
        targetValue = if (totalVotes > 0) {
            (item.voteUser.size + item.voteAnonymous.size).toFloat() / totalVotes
        } else {
            0f
        },
        animationSpec = tween(durationMillis = 500)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .then(if (isSelected) Modifier.padding(4.dp) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        (dish?.getTitle(Language.ENGLISH.code) ?: item.customTitle)?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(end = 8.dp),
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(24.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                LinearProgressIndicator(
                    progress = { animatedProgress.value },
                    modifier = Modifier.fillMaxSize(),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${((animatedProgress.value) * 100).toInt()}%", // TODO: Add localization
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(40.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RealTimeVoteResultsPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            RealTimeVoteResults(
                voteGame = DishVoteModel(
                    id = "test-id",
                    deleted = false,
                    createdAt = "2025-08-30T00:00:00Z",
                    updatedAt = "2025-08-30T00:00:00Z",
                    createdBy = null,
                    updatedBy = null,
                    deletedBy = null,
                    deletedAt = null,
                    title = "What should we eat for lunch?",
                    description = "Vote for your favorite dish from the menu",
                    dishVoteItems = listOf(
                        DishVoteItem(
                            slug = "pho-bo",
                            customTitle = null,
                            voteUser = listOf("user1", "user2"),
                            voteAnonymous = listOf("anon1"),
                            isCustom = false
                        ),
                        DishVoteItem(
                            slug = "banh-mi",
                            customTitle = null,
                            voteUser = listOf("user3"),
                            voteAnonymous = emptyList(),
                            isCustom = false
                        ),
                        DishVoteItem(
                            slug = "com-tam",
                            customTitle = null,
                            voteUser = emptyList(),
                            voteAnonymous = emptyList(),
                            isCustom = false
                        )
                    )
                ),
                selectedDish = "pho-bo",
                dishes = emptyList() // Using empty list for preview
            )
        }
    }
}
