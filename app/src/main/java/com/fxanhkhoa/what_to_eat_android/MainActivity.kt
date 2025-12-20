package com.fxanhkhoa.what_to_eat_android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fxanhkhoa.what_to_eat_android.screens.game.flipping_card.FlippingCardScreen
import com.fxanhkhoa.what_to_eat_android.screens.dish.DishDetailScreen
import com.fxanhkhoa.what_to_eat_android.ui.theme.ThemeProvider
import com.fxanhkhoa.what_to_eat_android.ui.components.FancyBottomNavigationBar
import com.fxanhkhoa.what_to_eat_android.ui.components.TopAppBarWithUserIcon
import com.fxanhkhoa.what_to_eat_android.ui.components.bottomNavItems
import com.fxanhkhoa.what_to_eat_android.screens.*
import com.fxanhkhoa.what_to_eat_android.screens.dish.DishListScreen
import com.fxanhkhoa.what_to_eat_android.screens.game.GameScreen
import com.fxanhkhoa.what_to_eat_android.screens.game.voting.VoteGameListScreen
import com.fxanhkhoa.what_to_eat_android.screens.game.voting.VotingCreateScreen
import com.fxanhkhoa.what_to_eat_android.screens.game.wheel_of_fortune.WheelOfFortuneScreen
import com.fxanhkhoa.what_to_eat_android.screens.ingredient.IngredientDetailScreen
import com.fxanhkhoa.what_to_eat_android.screens.ingredient.IngredientListScreen
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.utils.rememberSharedAuthViewModel
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider
import com.fxanhkhoa.what_to_eat_android.screens.game.voting.RealTimeVoteGameView
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize RetrofitProvider with context to enable AuthInterceptor
        RetrofitProvider.initialize(applicationContext)

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

    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    var language by remember { mutableStateOf(Language.ENGLISH) }

    // Observe language changes
    LaunchedEffect(Unit) {
        language = localizationManager.currentLanguage.first()
    }

    // Listen to navigation changes to sync selectedItemIndex
    LaunchedEffect(selectedItemIndex) {
        Log.d("MainScreen", "Selected Item Index: $selectedItemIndex")
        navController.navigate(bottomNavItems[selectedItemIndex].route) {
            // Pop up to start destination to avoid building up a large stack
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            // Avoid multiple copies of the same destination
            launchSingleTop = true
            // Restore state when re-selecting a previously selected item
            restoreState = false
        }
    }

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
            composable("home") {
                HomeScreen(
                    navController = navController,
                    onSelectBottomBarItem = { index -> selectedItemIndex = index }
                )
            }
            composable("dish") { DishListScreen(navController = navController) }
            composable(route = "dish/{slug}") { backStackEntry ->
                val slug = backStackEntry.arguments?.getString("slug") ?: ""
                if (slug.isNotEmpty()) {
                    // Pass navController pop back as the onBackPressed handler so the top app bar back button works
                    DishDetailScreen(slug = slug, onBackPressed = { navController.popBackStack() })
                } else {
                    DishView()
                }
            }
            composable("ingredient") { IngredientListScreen(navController = navController) }
            composable(route = "ingredient_detail/{ingredientId}") { backStackEntry ->
                val ingredientId = backStackEntry.arguments?.getString("ingredientId") ?: ""
                if (ingredientId.isNotEmpty()) {
                    // Here you would typically fetch the ingredient by ID
                    // For now, we'll need to modify IngredientDetailScreen to accept an ingredientId
                    IngredientDetailScreen(
                        ingredientId = ingredientId,
                        navController = navController,
                    )
                } else {
                    // Fallback - navigate back to ingredient list if no ID provided
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
            composable("game") {
                GameScreen(
                    onWheelOfFortune = { navController.navigate("wheel_of_fortune") },
                    onFlippingCard = { navController.navigate("flipping_card") },
                    onVoteGame = { navController.navigate("dish_vote_list") }
                )
            }
            composable("dish_vote_list") {
                VoteGameListScreen(
                    authViewModel = authViewModel,
                    navController = navController,
                    onNavigateToLogin = {
                        navController.navigate("login")
                    },
                    onNavigateBack = { navController.popBackStack() },
                    language = language
                )
            }
            composable("create_dish_vote") {
                VotingCreateScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable("dish_vote_game/{voteId}") { backStackEntry ->
                val voteId = backStackEntry.arguments?.getString("voteId") ?: ""
                if (voteId.isEmpty()) {
                    VoteGameListScreen(
                        authViewModel = authViewModel,
                        navController = navController,
                        onNavigateToLogin = {
                            navController.navigate("login")
                        },
                        onNavigateBack = { navController.popBackStack() },
                        language = language
                    )
                } else {
                    RealTimeVoteGameView(voteId, onDismiss = { navController.popBackStack() })
                }
            }
            composable("wheel_of_fortune") {
                WheelOfFortuneScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToDishDetail = { slug ->
                        navController.navigate("dish/$slug")
                    },
                    modifier = Modifier
                )
            }
            composable("flipping_card") {
                FlippingCardScreen(
                    onDismiss = { navController.popBackStack() },
                    onNavigateToDishDetail = { dish ->
                        navController.navigate("dish/${dish.slug}")
                    }
                )
            }
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
