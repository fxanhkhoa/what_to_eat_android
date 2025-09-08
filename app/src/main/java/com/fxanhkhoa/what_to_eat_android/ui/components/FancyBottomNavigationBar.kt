package com.fxanhkhoa.what_to_eat_android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.luminance
import com.fxanhkhoa.what_to_eat_android.ui.theme.*

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun FancyBottomNavigationBar(
    items: List<BottomNavItem>,
    selectedItemIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Use theme-aware colors that work with both light and dark themes
    val isSystemDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val backgroundGradient = if (isSystemDark) {
        // Dark theme - Use warm dark neutrals
        listOf(
            DarkBackgroundGradientStart,
            DarkBackgroundGradientMid1,
            DarkBackgroundGradientMid2,
            DarkBackgroundGradientEnd
        )
    } else {
        // Light theme - Use warm light neutrals
        listOf(
            LightBackgroundGradientStart,
            LightBackgroundGradientEnd
        )
    }

    val primaryGradient = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
            )
            .background(
                brush = Brush.horizontalGradient(backgroundGradient),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            )
    ) {
        // Animated background glow effect
        val infiniteTransition = rememberInfiniteTransition(label = "glow")
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowAlpha"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha * 0.3f),
                            Color.Transparent
                        ),
                        radius = 400f
                    ),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                FancyBottomNavItem(
                    item = item,
                    isSelected = selectedItemIndex == index,
                    onClick = { onItemSelected(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FancyBottomNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedColors = listOf(
        BottomNavSelectedStart,
        BottomNavSelectedEnd
    )

    val unselectedColor = BottomNavUnselected

    // Animations
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    val animatedIconColor by animateColorAsState(
        targetValue = if (isSelected) selectedColors[0] else unselectedColor,
        animationSpec = tween(400, easing = EaseInOutCubic),
        label = "iconColor"
    )

    val animatedTextColor by animateColorAsState(
        targetValue = if (isSelected) selectedColors[1] else unselectedColor,
        animationSpec = tween(400, easing = EaseInOutCubic),
        label = "textColor"
    )

    val animatedBackgroundAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(400, easing = EaseInOutCubic),
        label = "backgroundAlpha"
    )

    // Pulsing animation for selected item
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = if (isSelected) {
            infiniteRepeatable(
                animation = tween(1500, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Restart
            )
        },
        label = "pulseScale"
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .scale(animatedScale * pulseScale)
                .shadow(
                    elevation = if (isSelected) 16.dp else 0.dp,
                    shape = CircleShape,
                    ambientColor = selectedColors[0].copy(alpha = 0.4f),
                    spotColor = selectedColors[1].copy(alpha = 0.4f)
                )
                .background(
                    brush = if (isSelected) {
                        Brush.radialGradient(
                            colors = listOf(
                                selectedColors[0].copy(alpha = 0.3f * animatedBackgroundAlpha),
                                selectedColors[1].copy(alpha = 0.1f * animatedBackgroundAlpha),
                                Color.Transparent
                            ),
                            radius = 80f
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(Color.Transparent, Color.Transparent)
                        )
                    },
                    shape = CircleShape
                )
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    brush = if (isSelected) {
                        Brush.horizontalGradient(selectedColors.map { it.copy(alpha = 0.6f) })
                    } else {
                        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = animatedIconColor,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = item.label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = animatedTextColor
        )

        // Animated indicator dot
        if (isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(
                        brush = Brush.horizontalGradient(selectedColors),
                        shape = CircleShape
                    )
                    .scale(animatedScale)
            )
        }
    }
}

val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Filled.Home, "home"),
    BottomNavItem("Dish", Icons.Filled.Restaurant, "dish"),
    BottomNavItem("Ingredient", Icons.Filled.LocalGroceryStore, "ingredient"),
    BottomNavItem("Game", Icons.Filled.SportsEsports, "game"),
    BottomNavItem("Settings", Icons.Filled.Settings, "settings")
)
