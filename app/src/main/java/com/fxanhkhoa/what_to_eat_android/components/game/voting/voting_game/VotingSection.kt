package com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.model.DishVoteItem
import com.fxanhkhoa.what_to_eat_android.model.DishVoteModel
import com.fxanhkhoa.what_to_eat_android.model.MultiLanguage
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager

/**
 * VotingSection - Main voting interface component
 *
 * Matches the SwiftUI VotingSection component with:
 * - Grid layout of voting dish cards
 * - Selection state management
 * - Animated submit button
 * - Localized strings
 */
@Composable
fun VotingSection(
    voteGame: DishVoteModel,
    selectedDish: String?,
    dishes: List<DishModel>,
    onDishSelect: (String) -> Unit,
    onSubmitVote: () -> Unit,
    modifier: Modifier = Modifier,
    language: Language = Language.ENGLISH
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    // Create a map for quick dish lookup by slug
    val dishMap = remember(dishes) {
        dishes.associateBy { it.slug }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title
        Text(
            text = localizationManager.getString(R.string.cast_your_vote, language),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        // Grid of dish cards
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            voteGame.dishVoteItems.chunked(2).forEach { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowItems.forEach { item ->
                        VotingDishCard(
                            item = item,
                            dish = dishMap[item.slug],
                            isSelected = selectedDish == item.slug,
                            onTap = { onDishSelect(item.slug) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Submit button - animated visibility
        AnimatedVisibility(
            visible = selectedDish != null,
            enter = scaleIn(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
            exit = scaleOut(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary

            Button(
                onClick = onSubmitVote,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color.White
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HowToVote,
                        contentDescription = null
                    )
                    Text(
                        text = localizationManager.getString(R.string.submit_vote, language),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VotingSectionPreview() {
    val sampleDishes = listOf(
        DishModel(
            id = "1",
            deleted = false,
            createdAt = "2025-01-01T00:00:00Z",
            updatedAt = "2025-01-01T00:00:00Z",
            createdBy = null,
            updatedBy = null,
            deletedBy = null,
            deletedAt = null,
            slug = "pho-bo",
            title = listOf(
                MultiLanguage(lang = "en", data = "Pho Bo"),
                MultiLanguage(lang = "vi", data = "Phở Bò")
            ),
            shortDescription = listOf(
                MultiLanguage(lang = "en", data = "Vietnamese beef noodle soup"),
                MultiLanguage(lang = "vi", data = "Món phở bò truyền thống")
            ),
            content = emptyList(),
            tags = emptyList(),
            preparationTime = 30,
            cookingTime = 60,
            difficultLevel = "Medium",
            mealCategories = listOf("Breakfast", "Lunch"),
            ingredientCategories = emptyList(),
            thumbnail = null,
            videos = emptyList(),
            ingredients = emptyList(),
            relatedDishes = emptyList(),
            labels = null
        ),
        DishModel(
            id = "2",
            deleted = false,
            createdAt = "2025-01-01T00:00:00Z",
            updatedAt = "2025-01-01T00:00:00Z",
            createdBy = null,
            updatedBy = null,
            deletedBy = null,
            deletedAt = null,
            slug = "banh-mi",
            title = listOf(
                MultiLanguage(lang = "en", data = "Banh Mi"),
                MultiLanguage(lang = "vi", data = "Bánh Mì")
            ),
            shortDescription = listOf(
                MultiLanguage(lang = "en", data = "Vietnamese sandwich"),
                MultiLanguage(lang = "vi", data = "Bánh mì Việt Nam")
            ),
            content = emptyList(),
            tags = emptyList(),
            preparationTime = 15,
            cookingTime = 10,
            difficultLevel = "Easy",
            mealCategories = listOf("Breakfast", "Lunch"),
            ingredientCategories = emptyList(),
            thumbnail = null,
            videos = emptyList(),
            ingredients = emptyList(),
            relatedDishes = emptyList(),
            labels = null
        )
    )

    val sampleVoteGame = DishVoteModel(
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

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            VotingSection(
                voteGame = sampleVoteGame,
                selectedDish = "pho-bo",
                dishes = sampleDishes,
                onDishSelect = {},
                onSubmitVote = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
