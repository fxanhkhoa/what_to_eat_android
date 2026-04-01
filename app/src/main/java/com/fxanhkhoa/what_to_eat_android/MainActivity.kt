package com.fxanhkhoa.what_to_eat_android

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fxanhkhoa.what_to_eat_android.screens.game.flipping_card.FlippingCardScreen
import com.fxanhkhoa.what_to_eat_android.screens.dish.DishDetailScreen
import com.fxanhkhoa.what_to_eat_android.ui.theme.ThemeProvider
import com.fxanhkhoa.what_to_eat_android.ui.components.FancyBottomNavigationBar
import com.fxanhkhoa.what_to_eat_android.ui.components.InAppNotificationBanner
import com.fxanhkhoa.what_to_eat_android.ui.components.TopAppBarWithUserIcon
import com.fxanhkhoa.what_to_eat_android.ui.components.bottomNavItems
import com.fxanhkhoa.what_to_eat_android.screens.*
import com.fxanhkhoa.what_to_eat_android.screens.profile.EditProfileScreen
import com.fxanhkhoa.what_to_eat_android.screens.profile.ProfileScreen
import com.fxanhkhoa.what_to_eat_android.screens.dish.DishListScreen
import com.fxanhkhoa.what_to_eat_android.screens.game.GameScreen
import com.fxanhkhoa.what_to_eat_android.screens.game.voting.VoteGameListScreen
import com.fxanhkhoa.what_to_eat_android.screens.game.voting.VotingCreateScreen
import com.fxanhkhoa.what_to_eat_android.screens.game.wheel_of_fortune.WheelOfFortuneScreen
import com.fxanhkhoa.what_to_eat_android.screens.ingredient.IngredientDetailScreen
import com.fxanhkhoa.what_to_eat_android.screens.ingredient.IngredientListScreen
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.utils.AppState
import com.fxanhkhoa.what_to_eat_android.utils.rememberSharedAuthViewModel
import com.fxanhkhoa.what_to_eat_android.utils.rememberSharedNotificationViewModel
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider
import com.fxanhkhoa.what_to_eat_android.screens.game.voting.RealTimeVoteGameView
import com.fxanhkhoa.what_to_eat_android.services.FCMService
import com.google.firebase.messaging.FirebaseMessaging
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("MainActivity", "POST_NOTIFICATIONS granted: $granted")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize RetrofitProvider with context to enable AuthInterceptor
        RetrofitProvider.initialize(applicationContext)

        // Create notification channel (safe to call multiple times)
        createNotificationChannel()

        // Persist current FCM token and register with backend if already logged in
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Log.d("MainActivity", "Current FCM token: $token")
            lifecycleScope.launch {
                try {
                    val tm = com.fxanhkhoa.what_to_eat_android.utils.TokenManager
                        .getInstance(applicationContext)
                    tm.saveFCMToken(token)
                    if (tm.hasAccessToken()) {
                        Log.d("MainActivity", "User logged in — registering FCM token")
                        com.fxanhkhoa.what_to_eat_android.services.NotificationService
                            .getInstance(applicationContext)
                            .registerToken(token)
                            .onSuccess { Log.d("MainActivity", "FCM token registered successfully") }
                            .onFailure { e -> Log.e("MainActivity", "FCM token registration failed: ${e.message}") }
                    } else {
                        Log.d("MainActivity", "No access token — skipping FCM registration until login")
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "FCM token setup error: ${e.message}", e)
                }
            }
        }

        // Request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Handle deep-link route from a system-notification tap
        val deepLinkFromNotification = intent?.getStringExtra(FCMService.EXTRA_DEEP_LINK)

        setContent {
            ThemeProvider {
                MainScreen(initialDeepLinkRoute = deepLinkFromNotification)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        // If the app is already open and the user taps a system notification
        intent.getStringExtra(FCMService.EXTRA_DEEP_LINK)?.let { route ->
            val context = applicationContext
            val vm = com.fxanhkhoa.what_to_eat_android.viewmodel.NotificationViewModel
                .getInstanceOrNull()
            vm?.setPendingDeepLinkRoute(route)
        }
    }

    override fun onStart() {
        super.onStart()
        AppState.isInForeground = true
    }

    override fun onStop() {
        super.onStop()
        AppState.isInForeground = false
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(FCMService.CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                FCMService.CHANNEL_ID,
                FCMService.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Foodiary push notifications"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun MainScreen(initialDeepLinkRoute: String? = null) {
    val navController = rememberNavController()
    var selectedItemIndex by remember { mutableIntStateOf(0) }
    val authViewModel = rememberSharedAuthViewModel()
    val notificationViewModel = rememberSharedNotificationViewModel()

    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    var language by remember { mutableStateOf(Language.ENGLISH) }

    // Observe language changes
    LaunchedEffect(Unit) {
        language = localizationManager.currentLanguage.first()
    }

    // Navigate to deep-link from system notification tap (app was in background)
    LaunchedEffect(initialDeepLinkRoute) {
        initialDeepLinkRoute?.let { route ->
            navController.navigate(route)
        }
    }

    // Consume pending deep-link posted by onNewIntent (app was already open)
    val pendingDeepLinkRoute by notificationViewModel.pendingDeepLinkRoute.collectAsState()
    LaunchedEffect(pendingDeepLinkRoute) {
        pendingDeepLinkRoute?.let {
            notificationViewModel.consumePendingDeepLinkRoute()
            navController.navigate(it)
        }
    }

    // Load initial unread count when user is logged in
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            notificationViewModel.refreshUnreadCount()
        }
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
                },
                onNotificationClick = {
                    navController.navigate("notifications")
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
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = bottomNavItems[0].route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        bottom = innerPadding.calculateBottomPadding(),
                        top = innerPadding.calculateTopPadding(),
                    )
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
                        DishDetailScreen(
                            slug = slug,
                            onBackPressed = { navController.popBackStack() })
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
                        RealTimeVoteGameView(
                            voteId,
                            onDismiss = { navController.popBackStack() },
                            language = language
                        )
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
                        onNavigateToLogin = { navController.navigate("home") },
                        onNavigateToEditProfile = { navController.navigate("edit_profile") }
                    )
                }
                composable("edit_profile") {
                    EditProfileScreen(
                        onBackPressed = { navController.popBackStack() },
                        onSaveSuccess = { navController.popBackStack() }
                    )
                }
                composable("privacy_policy") {
                    PrivacyPolicyScreen(
                        onBackPressed = { navController.popBackStack() }
                    )
                }
                composable("settings") {
                    SettingScreen(
                        onNavigateToPrivacyPolicy = { navController.navigate("privacy_policy") }
                    )
                }
                composable("login") {
                    LoginScreen(
                        onBackPressed = { navController.popBackStack() },
                        onLoginSuccess = {
                            navController.popBackStack()
                            navController.navigate("profile")
                        }
                    )
                }
                composable("notifications") {
                    NotificationScreen(navController = navController)
                }
            }

            // In-app notification banner — floats above all content
            InAppNotificationBanner(
                navController = navController,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = innerPadding.calculateTopPadding())
            )
        }
    }
}
