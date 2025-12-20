package com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_result

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.model.DishVoteItem
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager

/**
 * Data class representing enriched vote result with dish information
 */
data class EnrichedVoteResult(
    val dishVoteItem: DishVoteItem,
    val dish: DishModel?,
    val totalVotes: Int,
    val userVotes: Int,
    val anonymousVotes: Int
) {
    val displayName: String
        get() = if (dishVoteItem.isCustom) {
            dishVoteItem.customTitle ?: dishVoteItem.slug
        } else {
            dish?.title?.firstOrNull()?.data ?: dishVoteItem.slug
        }

    val thumbnailURL: String?
        get() = dish?.thumbnail
}

@Composable
fun EnrichedVoteResultCard(
    result: EnrichedVoteResult,
    maxVotes: Int,
    isWinner: Boolean,
    language: Language,
    onDishClick: (DishModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    val percentage = if (maxVotes > 0) {
        result.totalVotes.toFloat() / maxVotes.toFloat()
    } else {
        0f
    }

    // Animated progress
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 800),
        label = "progressAnimation"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isWinner) 4.dp else 2.dp
        ),
        onClick = {
            result.dish?.let { dish ->
                onDishClick(dish)
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Image + Title + Winner Badge
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dish Image
                DishImageSection(
                    thumbnailURL = result.thumbnailURL,
                    isCustom = result.dishVoteItem.isCustom
                )

                // Dish Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Title and Winner Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = result.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (isWinner) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = localizationManager.getString(R.string.winner, language),
                                tint = Color(0xFFFFD700), // Gold color
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Vote Counts Summary
                    VoteCountsRow(
                        anonymousVotes = result.anonymousVotes,
                        totalVotes = result.totalVotes,
                        language = language,
                        localizationManager = localizationManager
                    )
                }
            }

            // Progress Bar Section
            ProgressBarSection(
                percentage = animatedPercentage,
                isWinner = isWinner
            )
        }
    }
}

@Composable
private fun DishImageSection(
    thumbnailURL: String?,
    isCustom: Boolean
) {
    if (!thumbnailURL.isNullOrEmpty()) {
        AsyncImage(
            model = thumbnailURL,
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        PlaceholderImage(isCustom = isCustom)
    }
}

@Composable
private fun PlaceholderImage(isCustom: Boolean) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isCustom) {
                    Color(0xFFFF9800).copy(alpha = 0.2f)
                } else {
                    Color.Gray.copy(alpha = 0.3f)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isCustom) Icons.Default.Restaurant else Icons.Default.RestaurantMenu,
            contentDescription = null,
            tint = if (isCustom) Color(0xFFFF9800) else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun VoteCountsRow(
    anonymousVotes: Int,
    totalVotes: Int,
    language: Language,
    localizationManager: LocalizationManager
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Anonymous Votes
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Color.Gray
            )
            Text(
                text = "$anonymousVotes",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Total Votes
        Text(
            text = "$totalVotes ${localizationManager.getString(R.string.votes, language)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ProgressBarSection(
    percentage: Float,
    isWinner: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isWinner) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFFD700), // Gold
                                    Color(0xFFFF9800)  // Orange
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            )
                        }
                    )
            )
        }

        // Percentage Text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "%.1f%%".format(percentage * 100),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
