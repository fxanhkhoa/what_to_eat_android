package com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_result

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
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
    fun displayName(language: Language): String =
        if (dishVoteItem.isCustom) {
            dishVoteItem.customTitle ?: dishVoteItem.slug
        } else {
            dish?.title?.firstOrNull { it.lang == language.code }?.data
                ?: dish?.title?.firstOrNull()?.data
                ?: dishVoteItem.slug
        }

    val thumbnailURL: String?
        get() = dish?.thumbnail
}

private val WinnerGold = Color(0xFFFFD700)
private val WinnerOrange = Color(0xFFFF9800)
private val CustomOrange = Color(0xFFFF9800)

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

    val percentage = if (maxVotes > 0) result.totalVotes.toFloat() / maxVotes.toFloat() else 0f

    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 900),
        label = "progressAnimation"
    )

    val cardContainerColor = if (isWinner)
        WinnerGold.copy(alpha = 0.06f)
    else
        MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isWinner) 6.dp else 2.dp),
        onClick = { result.dish?.let { onDishClick(it) } }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // ── Top Row ───────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Thumbnail
                Box(contentAlignment = Alignment.TopEnd) {
                    DishImageSection(
                        thumbnailURL = result.thumbnailURL,
                        isCustom = result.dishVoteItem.isCustom
                    )
                    if (isWinner) {
                        Box(
                            modifier = Modifier
                                .offset(x = 4.dp, y = (-4).dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(WinnerGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = localizationManager.getString(R.string.winner, language),
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }

                // Title + badge
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = result.displayName(language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (result.dishVoteItem.isCustom) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CustomOrange.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = localizationManager.getString(R.string.custom, language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CustomOrange,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Vote pill row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VotePill(
                            icon = { Icon(Icons.Default.HowToVote, null, Modifier.size(11.dp), tint = MaterialTheme.colorScheme.primary) },
                            label = "${result.totalVotes} ${localizationManager.getString(R.string.votes, language)}",
                            labelColor = MaterialTheme.colorScheme.primary
                        )
                        VotePill(
                            icon = { Icon(Icons.Default.Person, null, Modifier.size(11.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            label = "${result.anonymousVotes}",
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Percentage badge on the right
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isWinner) WinnerGold.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "%.0f%%".format(animatedPercentage * 100),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = if (isWinner) WinnerOrange
                        else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Progress Bar ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedPercentage)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isWinner)
                                Brush.horizontalGradient(listOf(WinnerGold, WinnerOrange))
                            else
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                        )
                )
            }
        }
    }
}

@Composable
private fun VotePill(
    icon: @Composable () -> Unit,
    label: String,
    labelColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        icon()
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor
        )
    }
}

@Composable
private fun DishImageSection(thumbnailURL: String?, isCustom: Boolean) {
    if (!thumbnailURL.isNullOrEmpty()) {
        AsyncImage(
            model = thumbnailURL,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isCustom) CustomOrange.copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCustom) Icons.Default.Restaurant else Icons.Default.RestaurantMenu,
                contentDescription = null,
                tint = if (isCustom) CustomOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
