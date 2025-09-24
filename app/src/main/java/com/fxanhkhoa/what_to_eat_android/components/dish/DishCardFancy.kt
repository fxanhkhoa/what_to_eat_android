package com.fxanhkhoa.what_to_eat_android.components.dish

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.shared.DifficultyLevel
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import kotlinx.coroutines.flow.first
import androidx.compose.ui.res.colorResource
import androidx.navigation.NavController


@Composable
fun DishCardFancy(
    dish: DishModel,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    var currentLanguage by remember { mutableStateOf(Language.ENGLISH) }
    val colorScheme = MaterialTheme.colorScheme
    val difficulty = DifficultyLevel.from(dish.difficultLevel)
    val title = dish.title.firstOrNull { it.lang == currentLanguage.code }?.data ?: ""
    val shortDesc =
        dish.shortDescription.firstOrNull { it.lang == currentLanguage.code }?.data ?: ""
    val slicedTitle = if (title.split(" ").size <= 7) title else title.split(" ").take(7)
        .joinToString(" ") + "..."
    val slicedDesc =
        if (shortDesc.split(" ").size <= 12) shortDesc else shortDesc.split(" ").take(12)
            .joinToString(" ") + "..."
    val backgroundImages = listOf(
        R.drawable.food_card_bg_1,
        R.drawable.food_card_bg_2,
        R.drawable.food_card_bg_3,
        R.drawable.food_card_bg_4
    )
    val bgImageRes = backgroundImages.random()
    val scrollState = rememberScrollState()

    val preparationText = remember(currentLanguage) {
        localizationManager.getString(
            R.string.preparation,
            currentLanguage
        )
    }

    val minText = remember(currentLanguage) {
        localizationManager.getString(
            R.string.mins,
            currentLanguage
        )
    }

    val cookingText = remember(currentLanguage) {
        localizationManager.getString(
            R.string.cooking,
            currentLanguage
        )
    }

    val difficultyText = remember(currentLanguage) {
        val stringResId = when (difficulty) {
            DifficultyLevel.EASY -> R.string.easy
            DifficultyLevel.MEDIUM -> R.string.medium
            DifficultyLevel.HARD -> R.string.hard
        }
        localizationManager.getString(
            stringResId,
            currentLanguage
        )
    }

    LaunchedEffect(key1 = Unit) {
        val lang = localizationManager.currentLanguage.first()
        currentLanguage = lang
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick?.invoke() },
        color = colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Box {
            Image(
                painter = painterResource(id = bgImageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.25f),
                contentScale = ContentScale.Crop
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .offset(x = (-20).dp)
                        .clip(RoundedCornerShape(60.dp))
                ) {
                    AsyncImage(
                        model = dish.thumbnail,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(60.dp))
                    )
                }
                Spacer(modifier = Modifier.width(0.dp))
                Column(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 8.dp)
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = slicedTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colorScheme.primary
                    )
                    Text(
                        text = slicedDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.preparation_time),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = preparationText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.primary
                                )
                            }
                            Text(
                                text = "${dish.preparationTime ?: 0} $minText",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.cooking_time),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = cookingText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.primary
                                )
                            }
                            Text(
                                text = "${dish.cookingTime ?: 0} $minText",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val diff = dish.difficultLevel
                            if (diff == "easy") {
                                Image(
                                    painter = painterResource(id = R.drawable.easy),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            if (diff == "medium") {
                                Image(
                                    painter = painterResource(id = R.drawable.medium),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            if (diff == "hard") {
                                Image(
                                    painter = painterResource(id = R.drawable.hard),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            if (diff != null) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = colorScheme.surface,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = difficultyText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorResource(id = difficulty.colorRes)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
