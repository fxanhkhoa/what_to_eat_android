package com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_game

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.model.DishVoteItem
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager

/**
 * Vote result row component
 * Displays a single dish's voting results with animated progress bar and expandable voter list
 */
@Composable
fun VoteResultRow(
    item: DishVoteItem,
    dish: DishModel?,
    totalVotes: Int,
    isSelected: Boolean,
    language: Language = Language.ENGLISH,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    var showVoters by remember { mutableStateOf(false) }


    // Calculate vote count and percentage
    val voteCount = item.voteUser.size + item.voteAnonymous.size
    val percentage = if (totalVotes > 0) {
        voteCount.toFloat() / totalVotes.toFloat()
    } else {
        0f
    }

    // Animate progress bar
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 500),
        label = "progress_animation"
    )

    // Get dish title
    val dishTitle = when {
        item.isCustom -> item.customTitle ?: localizationManager.getString(R.string.custom_dish, language)
        dish != null -> dish.getTitle(language.code) ?: localizationManager.getString(R.string.untitled, language)
        else -> localizationManager.getString(R.string.unknown_dish, language)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
    ) {
        // Vote result bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // Progress bar background
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedPercentage)
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            )

            // Content row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dishTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$voteCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Toggle button to show/hide voters
                    if (voteCount > 0) {
                        IconButton(
                            onClick = { showVoters = !showVoters },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (showVoters) {
                                    Icons.Default.KeyboardArrowUp
                                } else {
                                    Icons.Default.KeyboardArrowDown
                                },
                                contentDescription = localizationManager.getString(
                                    if (showVoters) R.string.hide_voters else R.string.show_voters,
                                    language
                                ),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Voter list (expandable)
        AnimatedVisibility(
            visible = showVoters && voteCount > 0,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            VoterResultRowListView(
                userIds = item.voteUser,
                anonymousCount = item.voteAnonymous.size,
                language = language,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * Voter list view component
 * Shows the list of users who voted and anonymous vote count
 */
@Composable
fun VoterResultRowListView(
    userIds: List<String>,
    anonymousCount: Int,
    language: Language = Language.ENGLISH,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // ...existing code...

        // Anonymous votes
        if (anonymousCount > 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = context.resources.getQuantityString(
                        R.plurals.anonymous_votes,
                        anonymousCount,
                        anonymousCount
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VoteResultRowPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Regular dish with votes
            VoteResultRow(
                item = DishVoteItem(
                    slug = "pho-bo",
                    customTitle = null,
                    voteUser = listOf("user1", "user2"),
                    voteAnonymous = listOf("anon1"),
                    isCustom = false
                ),
                dish = null,
                totalVotes = 10,
                isSelected = false
            )

            // Custom dish, selected
            VoteResultRow(
                item = DishVoteItem(
                    slug = "custom-dish",
                    customTitle = "My Special Dish",
                    voteUser = listOf("user3"),
                    voteAnonymous = emptyList(),
                    isCustom = true
                ),
                dish = null,
                totalVotes = 10,
                isSelected = true
            )

            // No votes
            VoteResultRow(
                item = DishVoteItem(
                    slug = "com-tam",
                    customTitle = null,
                    voteUser = emptyList(),
                    voteAnonymous = emptyList(),
                    isCustom = false
                ),
                dish = null,
                totalVotes = 10,
                isSelected = false
            )
        }
    }
}

@Preview(showBackground = true, name = "Voter List")
@Composable
private fun VoterListViewPreview() {
    MaterialTheme {
        VoterListView(
            userIds = listOf("user1", "user2", "user3"),
            anonymousCount = 2,
            modifier = Modifier.padding(8.dp)
        )
    }
}
