package com.fxanhkhoa.what_to_eat_android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fxanhkhoa.what_to_eat_android.ui.theme.GradientEnd
import com.fxanhkhoa.what_to_eat_android.ui.theme.GradientStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView() {
    ScreenTemplate(
        title = "Home",
        icon = Icons.Filled.Home,
        description = "Welcome to What to Eat App!"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishView() {
    ScreenTemplate(
        title = "Dishes",
        icon = Icons.Filled.Restaurant,
        description = "Discover delicious dishes here!"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientView() {
    ScreenTemplate(
        title = "Ingredients",
        icon = Icons.Filled.LocalGroceryStore,
        description = "Find fresh ingredients for your recipes!"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameView() {
    ScreenTemplate(
        title = "Game",
        icon = Icons.Filled.SportsEsports,
        description = "Play fun food-related games!"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenTemplate(
    title: String,
    icon: ImageVector,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(80.dp),
                    tint = GradientStart
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
