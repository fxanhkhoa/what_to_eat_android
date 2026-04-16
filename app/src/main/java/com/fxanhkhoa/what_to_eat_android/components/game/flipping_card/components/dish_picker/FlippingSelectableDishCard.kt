package com.fxanhkhoa.what_to_eat_android.components.game.flipping_card.components.dish_picker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager

@Composable
internal fun FlippingSelectableDishCard(
    dish: DishModel,
    isSelected: Boolean,
    canSelect: Boolean,
    onTap: () -> Unit,
    language: Language,
    localizationManager: LocalizationManager
) {
    val colorScheme = MaterialTheme.colorScheme
    val localizedTitle = dish.getTitle(language.code) ?: dish.slug
    val localizedDescription = dish.getShortDescription(language.code)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable(enabled = canSelect || isSelected, onClick = onTap),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = if (isSelected) 8.dp else 4.dp,
        border = if (isSelected) BorderStroke(2.dp, colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                AsyncImage(
                    model = dish.thumbnail,
                    contentDescription = localizedTitle,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.ic_launcher_foreground),
                    placeholder = painterResource(R.drawable.ic_launcher_foreground)
                )
                if (isSelected) {
                    Box(
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                            .background(colorScheme.primary.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                } else if (!canSelect) {
                    Box(
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = "Cannot select",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = localizedTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(40.dp)
                )
                if (localizedDescription != null) {
                    Text(
                        text = localizedDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.height(28.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(28.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth().height(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dish.preparationTime != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                painter = painterResource(R.drawable.ic_launcher_foreground),
                                contentDescription = "Time",
                                modifier = Modifier.size(12.dp),
                                tint = colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${dish.preparationTime} ${localizationManager.getString(R.string.mins, language)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (dish.difficultLevel != null) {
                        Surface(shape = RoundedCornerShape(4.dp), color = colorScheme.primaryContainer.copy(alpha = 0.2f)) {
                            Text(
                                text = localizationManager.getString(
                                    when (dish.difficultLevel.lowercase()) {
                                        "easy"   -> R.string.difficulty_easy
                                        "medium" -> R.string.difficulty_medium
                                        "hard"   -> R.string.difficulty_hard
                                        else     -> R.string.difficulty_medium
                                    },
                                    language
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

