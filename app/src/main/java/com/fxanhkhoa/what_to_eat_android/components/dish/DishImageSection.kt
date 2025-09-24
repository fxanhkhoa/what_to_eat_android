package com.fxanhkhoa.what_to_eat_android.components.dish

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.shared.DifficultyLevel
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.shared.contant.DishTitleHorizontalGradient
import androidx.core.content.ContextCompat

@Composable
fun DishImageSection(
    dish: DishModel,
    modifier: Modifier = Modifier,
    language: Language = Language.ENGLISH
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    val title = remember(dish, language) { dish.title.firstOrNull { it.lang == language.code }?.data ?: "Unknown Dish" }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(0.dp)
            .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Image
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(dish.thumbnail)
                    .crossfade(true)
                    .build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
            )

            // Bottom gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Title and difficulty chip
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                // Difficulty chip
                dish.difficultLevel?.let { dl ->
                    val difficulty = DifficultyLevel.from(dl)

                    // Map to string resource ids (same mapping used elsewhere in the project)
                    val difficultyResId = when (difficulty) {
                        DifficultyLevel.EASY -> com.fxanhkhoa.what_to_eat_android.R.string.easy
                        DifficultyLevel.MEDIUM -> com.fxanhkhoa.what_to_eat_android.R.string.medium
                        DifficultyLevel.HARD -> com.fxanhkhoa.what_to_eat_android.R.string.hard
                    }

                    // Resolve color from difficulty.colorRes
                    val colorInt = ContextCompat.getColor(context, difficulty.colorRes)
                    val chipColor = Color(colorInt)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(color = chipColor.copy(alpha = 0.8f), shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        // Icon - use difficulty.iconName to find drawable
                        val iconName = difficulty.iconName
                        val resId = context.resources.getIdentifier(iconName, "drawable", context.packageName)
                        if (resId != 0) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = resId),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Text(
                            text = localizationManager.getString(difficultyResId, language),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
