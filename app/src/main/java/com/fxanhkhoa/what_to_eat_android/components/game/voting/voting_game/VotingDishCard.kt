package com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.model.DishVoteItem
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager

/**
 * VotingDishCard - A card component for displaying a dish option in a voting game
 *
 * Matches the SwiftUI VotingDishCard component with:
 * - Dish thumbnail or icon display
 * - Selection state with animation
 * - Gradient overlays and shadows
 * - Localized dish title
 */
@Composable
fun VotingDishCard(
    item: DishVoteItem,
    dish: DishModel?,
    isSelected: Boolean,
    onTap: () -> Unit,
    language: Language = Language.ENGLISH,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    // Animated scale effect for selection
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )

    // Colors
    val primaryColor = MaterialTheme.colorScheme.primary

    val backgroundColor = if (isSelected) primaryColor else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
    val iconColor = if (isSelected) Color.White else primaryColor

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .scale(scale)
            .shadow(
                elevation = if (isSelected) 8.dp else 4.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = if (isSelected) primaryColor.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onTap)
    ) {
        // Gradient overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Image or Icon
            if (!dish?.thumbnail.isNullOrEmpty()) {
                SubcomposeAsyncImage(
                    model = dish.thumbnail,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shadow(2.dp, RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    error = { _ ->
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = iconColor
                            )
                        }
                    },
                    loading = {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = iconColor
                            )
                        }
                    }
                )
            } else {
                Icon(
                    imageVector = if (item.isCustom) Icons.Default.Person else Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            val title = if (item.isCustom) {
                item.customTitle ?: localizationManager.getString(R.string.custom_dish, language)
            } else {
                dish?.getTitle(language.code) ?: localizationManager.getString(R.string.unknown_dish, language)
            }

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VotingDishCardPreview() {
    MaterialTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Unselected card
            VotingDishCard(
                item = DishVoteItem(
                    slug = "pho-bo",
                    customTitle = null,
                    isCustom = false
                ),
                dish = null,
                isSelected = false,
                onTap = {},
                modifier = Modifier.weight(1f)
            )

            // Selected custom card
            VotingDishCard(
                item = DishVoteItem(
                    slug = "custom-dish",
                    customTitle = "My Special Dish",
                    isCustom = true
                ),
                dish = null,
                isSelected = true,
                onTap = {},
                modifier = Modifier.weight(1f)
            )
        }
    }
}
