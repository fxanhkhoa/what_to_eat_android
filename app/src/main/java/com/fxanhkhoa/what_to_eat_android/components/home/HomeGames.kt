package com.fxanhkhoa.what_to_eat_android.components.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager

// Data class for GameMenuItem
data class GameMenuItem(
    val iconRes: Int, // Drawable resource ID
    val title: String, // Localized string or key
    val description: String, // Localized string or key
    val onClick: () -> Unit
)

@Composable
fun GameMenuItemView(item: GameMenuItem, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Button(
        onClick = item.onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.surface,
            contentColor = colorScheme.onSurface
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .width(160.dp)
            .height(300.dp)
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = colorScheme.surfaceVariant
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = colorScheme.background,
                        shape = CircleShape
                    )
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black.copy(alpha = 0.3f),
                        spotColor = colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.title, // Replace with localized string if needed
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.description, // Replace with localized string if needed
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                maxLines = 3,
                textAlign = TextAlign.Center,
                modifier = Modifier.height(48.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeGames(
    navController: NavController,
    modifier: Modifier = Modifier,
    language: Language = Language.ENGLISH
) {
    // Define your game items with string resource IDs and drawable resource IDs
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    val wheelOfFortuneTitle = remember(language) {
        localizationManager.getString(
            R.string.wheel_of_fortune,
            language
        )
    }

    val wheelOfFortuneDesc = remember(language) {
        localizationManager.getString(
            R.string.wheel_of_fortune_desc,
            language
        )
    }

    val flippingCardTitle = remember(language) {
        localizationManager.getString(
            R.string.flipping_card,
            language
        )
    }

    val flippingCardDesc = remember(language) {
        localizationManager.getString(
            R.string.flipping_card_desc,
            language
        )
    }

    val voteGameTitle = remember(language) {
        localizationManager.getString(
            R.string.vote_game,
            language
        )
    }

    val voteGameDesc = remember(language) {
        localizationManager.getString(
            R.string.vote_game_desc,
            language
        )
    }

    val gameSectionText = remember(language) {
        localizationManager.getString(
            R.string.game_section_title,
            language
        )
    }

    val gameItems = listOf(
        GameMenuItem(
            iconRes = R.drawable.wheel_of_fortune_menu, // Replace with your actual drawable
            title = wheelOfFortuneTitle,
            description = wheelOfFortuneDesc,
            onClick = { navController.navigate("wheel_of_fortune") }
        ),
        GameMenuItem(
            iconRes = R.drawable.flipping_card_menu, // Replace with your actual drawable
            title = flippingCardTitle,
            description = flippingCardDesc,
            onClick = { navController.navigate("flipping_card") }
        ),
        GameMenuItem(
            iconRes = R.drawable.vote_menu, // Replace with your actual drawable
            title = voteGameTitle,
            description = voteGameDesc,
            onClick = { navController.navigate("vote_game") }
        )
    )

    Column(modifier = modifier.padding(vertical = 16.dp)) {
        Text(
            text = gameSectionText,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(gameItems) { item ->
                GameMenuItemView(item = item)
            }
        }
    }
}
