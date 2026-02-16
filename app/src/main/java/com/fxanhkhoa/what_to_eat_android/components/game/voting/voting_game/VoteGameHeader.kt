package com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_game

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.model.DishVoteItem
import com.fxanhkhoa.what_to_eat_android.model.DishVoteModel
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager

/**
 * Vote game header component
 * Displays vote game title, description, live indicator, and total vote count
 */
@Composable
fun VoteGameHeader(
    voteGame: DishVoteModel,
    language: Language = Language.ENGLISH,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    // Calculate total votes
    val totalVotes = remember(voteGame.dishVoteItems) {
        voteGame.dishVoteItems.sumOf { item ->
            item.voteUser.size + item.voteAnonymous.size
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Title
        Text(
            text = voteGame.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Description (only show if not empty)
        if (voteGame.description.isNotEmpty()) {
            Text(
                text = voteGame.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Live indicators row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live updates indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = localizationManager.getString(R.string.live, language),
                    tint = Color(0xFF4CAF50), // Green color for live indicator
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = localizationManager.getString(R.string.live_updates, language),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50)
                )
            }

            // Vote count
            Text(
                text = context.resources.getQuantityString(
                    R.plurals.vote_count,
                    totalVotes,
                    totalVotes
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VoteGameHeaderPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preview with description
            VoteGameHeader(
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
                        )
                    )
                )
            )

            Divider()

            // Preview without description
            VoteGameHeader(
                voteGame = DishVoteModel(
                    id = "test-id-2",
                    deleted = false,
                    createdAt = "2025-08-30T00:00:00Z",
                    updatedAt = "2025-08-30T00:00:00Z",
                    createdBy = null,
                    updatedBy = null,
                    deletedBy = null,
                    deletedAt = null,
                    title = "Quick lunch vote!",
                    description = "",
                    dishVoteItems = listOf(
                        DishVoteItem(
                            slug = "pho-bo",
                            customTitle = null,
                            voteUser = listOf("user1"),
                            voteAnonymous = emptyList(),
                            isCustom = false
                        )
                    )
                )
            )
        }
    }
}
