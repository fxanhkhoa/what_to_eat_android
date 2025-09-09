package com.fxanhkhoa.what_to_eat_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fxanhkhoa.what_to_eat_android.ui.theme.ThemeProvider
import com.fxanhkhoa.what_to_eat_android.ui.components.FancyBottomNavigationBar
import com.fxanhkhoa.what_to_eat_android.ui.components.TopAppBarWithUserIcon
import com.fxanhkhoa.what_to_eat_android.ui.components.bottomNavItems
import com.fxanhkhoa.what_to_eat_android.screens.*
import com.fxanhkhoa.what_to_eat_android.utils.rememberSharedAuthViewModel
import com.fxanhkhoa.what_to_eat_android.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThemeProvider {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var selectedItemIndex by remember { mutableIntStateOf(0) }
    val authViewModel = rememberSharedAuthViewModel()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBarWithUserIcon(
                title = "What to Eat",
                onUserIconClick = {
                    if (authViewModel.isLoggedIn.value) {
                        navController.navigate("profile")
                    } else {
                        navController.navigate("login")
                    }
                    // Handle unauthorized user click - could show login dialog or navigate to login
                    // For now, this is just a placeholder
                }
            )
        },
        bottomBar = {
            // Add safe area for bottom bar using WindowInsets
            Box(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            ) {
                FancyBottomNavigationBar(
                    items = bottomNavItems,
                    selectedItemIndex = selectedItemIndex,
                    onItemSelected = { index ->
                        selectedItemIndex = index
                        navController.navigate(bottomNavItems[index].route) {
                            // Pop up to start destination to avoid building up a large stack
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when re-selecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = bottomNavItems[0].route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen() }
            composable("dish") { DishView() }
            composable("ingredient") { IngredientView() }
            composable("game") { GameView() }
            composable("profile") {
                ProfileScreen(
                    onBackPressed = { navController.popBackStack() },
                    onNavigateToLogin = { navController.navigate("home") }
                )
            }
            composable("settings") { SettingScreen() }
            composable("login") {
                LoginScreen(
                    onBackPressed = { navController.popBackStack() },
                    onLoginSuccess = {
                        navController.popBackStack()
                        navController.navigate("profile")
                    }
                )
            }
        }
    }
}
