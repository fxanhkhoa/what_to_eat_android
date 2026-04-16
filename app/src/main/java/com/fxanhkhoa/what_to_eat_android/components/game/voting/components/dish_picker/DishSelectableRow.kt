package com.fxanhkhoa.what_to_eat_android.components.game.voting.components.dish_picker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
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
internal fun DishSelectableRow(
    dish: DishModel,
    isSelected: Boolean,
    isAlreadyAdded: Boolean,
    onToggle: () -> Unit,
    language: Language,
    localizationManager: LocalizationManager,
    colorScheme: ColorScheme
) {
    val localizedTitle = dish.getTitle(language.code) ?: dish.slug
    val localizedDescription = dish.getShortDescription(language.code)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isAlreadyAdded, onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = if (isSelected) 4.dp else 2.dp,
        color = colorScheme.surface,
        border = if (isAlreadyAdded) {
            BorderStroke(2.dp, Color.Green)
        } else if (isSelected) {
            BorderStroke(2.dp, colorScheme.primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = dish.thumbnail,
                contentDescription = localizedTitle,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.ic_launcher_foreground),
                placeholder = painterResource(R.drawable.ic_launcher_foreground)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = localizedTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (localizedDescription != null) {
                    Text(
                        text = localizedDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dish.preparationTime != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = colorScheme.primaryContainer.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = dish.difficultLevel.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            when {
                isAlreadyAdded -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Already added",
                        tint = Color.Green,
                        modifier = Modifier.size(28.dp)
                    )
                }
                isSelected -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Outlined.Circle,
                        contentDescription = "Not selected",
                        tint = Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

