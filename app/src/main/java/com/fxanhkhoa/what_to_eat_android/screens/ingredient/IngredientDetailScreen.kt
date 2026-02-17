package com.fxanhkhoa.what_to_eat_android.screens.ingredient

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.components.ingredient.detail.*
import com.fxanhkhoa.what_to_eat_android.model.Ingredient
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider
import com.fxanhkhoa.what_to_eat_android.services.IngredientService
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientDetailScreen(
    ingredientId: String,
    navController: NavController,
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    var selectedImageIndex by remember { mutableIntStateOf(0) }
    var ingredient by remember { mutableStateOf<Ingredient?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val ingredientService = RetrofitProvider.createService<IngredientService>()
    val coroutineScope = rememberCoroutineScope()

    var language by remember { mutableStateOf(Language.ENGLISH) }

    // Observe language changes
    LaunchedEffect(Unit) {
        language = localizationManager.currentLanguage.first()
    }

    // Handle system back button
    BackHandler {
        navController.popBackStack()
    }

    // Fetch ingredient by ID from service
    LaunchedEffect(ingredientId) {
        coroutineScope.launch {
            try {
                isLoading = true
                errorMessage = null
                val fetchedIngredient = ingredientService.findOne(ingredientId)
                Log.d("IngredientDetailScreen", "Fetched ingredient: $fetchedIngredient")
                ingredient = fetchedIngredient
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                errorMessage = when {
                    e.message?.contains("404") == true ->
                        localizationManager.getString(R.string.unknown_ingredient, language)
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Network error. Please check your connection."
                    else ->
                        "Failed to load ingredient: ${e.message}"
                }
            }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (ingredient == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(
                    text = errorMessage ?: localizationManager.getString(R.string.unknown_ingredient, language),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text(localizationManager.getString(R.string.back, language))
                }
            }
        }
        return
    }

    // Safe access to ingredient - we know it's not null at this point
    val currentIngredient = ingredient ?: return
    val ingredientTitle = currentIngredient.getTitle(language.code)
        ?: localizationManager.getString(R.string.ingredient, language)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = ingredientTitle,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localizationManager.getString(R.string.back, language)
                        )
                    }
                },
                actions = {
                    ShareButton(
                        ingredient = currentIngredient,
                        localizationManager = localizationManager,
                        language = language
                    )
                },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Image Gallery
            if (currentIngredient.images.isNotEmpty()) {
                ImageGalleryView(
                    images = currentIngredient.images,
                    selectedIndex = selectedImageIndex,
                    onImageChanged = { selectedImageIndex = it },
                    localizationManager = localizationManager,
                    language = language,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }

            // Content sections
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Title Section
                TitleSection(
                    ingredient = currentIngredient,
                    localizationManager = localizationManager,
                    language = language
                )

                // Nutrition Section
                NutritionSection(
                    ingredient = currentIngredient,
                    localizationManager = localizationManager,
                    language = language
                )

                // Categories Section
                CategoriesSection(
                    ingredient = currentIngredient,
                    localizationManager = localizationManager,
                    language = language
                )

                // Additional Details Section
                AdditionalDetailsSection(
                    ingredient = currentIngredient,
                    localizationManager = localizationManager,
                    language = language
                )
            }
        }
    }
}
