package com.fxanhkhoa.what_to_eat_android.components.game.flipping_card

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.viewmodel.GameCard

@Composable
fun GameBoardView(
    cards: List<GameCard>,
    onCardTapped: (GameCard) -> Unit,
    language: Language,
    localizationManager: LocalizationManager
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 615.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(cards, key = { it.id }) { card ->
            DishCardView(
                card = card,
                onTapped = { onCardTapped(card) },
                language = language,
                localizationManager = localizationManager
            )
        }
    }
}

