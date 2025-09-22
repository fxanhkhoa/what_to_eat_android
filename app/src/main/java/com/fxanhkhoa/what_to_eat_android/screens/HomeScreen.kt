package com.fxanhkhoa.what_to_eat_android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fxanhkhoa.what_to_eat_android.components.home.FeaturedDishes
import com.fxanhkhoa.what_to_eat_android.components.SearchBar
import com.fxanhkhoa.what_to_eat_android.components.SearchBarResult
import com.fxanhkhoa.what_to_eat_android.components.home.ContactSection
import com.fxanhkhoa.what_to_eat_android.components.home.HomeBanner
import com.fxanhkhoa.what_to_eat_android.components.home.RandomDishes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onSelectBottomBarItem: ((Int) -> Unit)? = null // Add callback for bottom bar selection
) {
    var searchText by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Home",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        SearchBar(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth()
        )
        SearchBarResult(
            text = searchText, modifier = Modifier.fillMaxWidth()
        )
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            FeaturedDishes(
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Add some additional content for scrolling demonstration
            Spacer(modifier = Modifier.height(24.dp))

            HomeBanner(
                navController = navController,
                modifier = Modifier,
                onNavigateToDish = {
                    onSelectBottomBarItem?.invoke(1) // 1 is the index for Dish
                    navController.navigate("dish")
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            RandomDishes(modifier = Modifier)

            Spacer(modifier = Modifier.height(24.dp))

            ContactSection(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
