package com.fxanhkhoa.what_to_eat_android.screens.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import kotlinx.coroutines.flow.first

@Composable
fun GameScreen(
    onWheelOfFortune: () -> Unit = {},
    onFlippingCard: () -> Unit = {},
    onVoteGame: () -> Unit = {}
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    var language by remember { mutableStateOf(Language.ENGLISH) }

    // Observe language changes
    LaunchedEffect(Unit) {
        language = localizationManager.currentLanguage.first()
    }

    val wheelOfFortuneTitle = remember(language) {
        localizationManager.getString(
            R.string.wheel_of_fortune,
            language
        )
    }

    val wheelOfFortuneDesc =
        localizationManager.getString(
            R.string.wheel_of_fortune_desc,
            language
        )

    val flippingCardTitle =
        localizationManager.getString(
            R.string.flipping_card,
            language
        )

    val flippingCardDesc =
        localizationManager.getString(
            R.string.flipping_card_desc,
            language
        )

    val voteGameTitle =
        localizationManager.getString(
            R.string.vote_game,
            language
        )

    val voteGameDesc =
        localizationManager.getString(
            R.string.vote_game_desc,
            language
        )

    val gameItems = listOf(
        GameMenuItem(
            titleRes = wheelOfFortuneTitle,
            descRes = wheelOfFortuneDesc,
            iconRes = R.drawable.wheel_of_fortune_menu,
            onClick = onWheelOfFortune
        ),
        GameMenuItem(
            titleRes = flippingCardTitle,
            descRes = flippingCardDesc,
            iconRes = R.drawable.flipping_card_menu,
            onClick = onFlippingCard
        ),
        GameMenuItem(
            titleRes = voteGameTitle,
            descRes = voteGameDesc,
            iconRes = R.drawable.vote_menu,
            onClick = onVoteGame
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        HeaderSection(language = language)
        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height(340.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(gameItems) { item ->
                GameCard(item)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        FeaturedSection(language = language)
    }
}

@Composable
private fun HeaderSection(language: Language = Language.ENGLISH) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    val gameSectionTitle = localizationManager.getString(
            R.string.game_section_title,
            language
        )

    val gameSectionSubtitle = localizationManager.getString(
        R.string.game_section_subtitle,
        language
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_gamecontroller),
            contentDescription = null,
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = gameSectionTitle,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = gameSectionSubtitle,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 4.dp, bottom = 8.dp)
        )
    }
}

@Composable
private fun GameCard(item: GameMenuItem) {
    Card(
        onClick = item.onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(64.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFF3A446), Color(0xFFA06235))
                            ),
                            shape = CircleShape
                        )
                )
                Image(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.titleRes,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                text = item.descRes,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.play_now),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun FeaturedSection(language: Language = Language.ENGLISH) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    val featureGameTitle = localizationManager.getString(
        R.string.featured_games,
        language
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = featureGameTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color.Yellow,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = stringResource(R.string.coming_soon),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// Data class for game menu items
private data class GameMenuItem(
    val titleRes: String,
    val descRes: String,
    val iconRes: Int,
    val onClick: () -> Unit
)
