package com.fxanhkhoa.what_to_eat_android.screens.dish

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import com.fxanhkhoa.what_to_eat_android.components.dish.*
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.model.Ingredient
import com.fxanhkhoa.what_to_eat_android.services.DishService
import com.fxanhkhoa.what_to_eat_android.services.IngredientService
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider.createService
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishDetailScreen(
    slug: String,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {}
) {
    var dish by remember { mutableStateOf<DishModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    var language by remember { mutableStateOf(Language.ENGLISH) }

    LaunchedEffect(Unit) {
        language = localizationManager.currentLanguage.first()
    }

    var checkedIngredients by remember { mutableStateOf(setOf<String>()) }
    var fullIngredients by remember { mutableStateOf<Map<String, Ingredient>>(emptyMap()) }
    var isLoadingIngredients by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Handle system back button
    BackHandler {
        onBackPressed()
    }

    // Fetch dish by slug when screen appears
    LaunchedEffect(slug) {
        isLoading = true
        errorMessage = null
        try {
            val service = createService<DishService>()
            val fetched = withContext(Dispatchers.IO) { service.findBySlug(slug) }
            dish = fetched
        } catch (_: Exception) {
            errorMessage = "Failed to load dish"
        } finally {
            isLoading = false
        }
    }

    // Helper to load full ingredient details for the dish
    suspend fun loadFullIngredientsFor(d: DishModel) {
        if (d.ingredients.isEmpty()) return
        isLoadingIngredients = true
        try {
            val ingredientService = createService<IngredientService>()
            // Parallel fetch all ingredients (best-effort; individual failures ignored)
            val pairs: List<Pair<String, Ingredient>?> = coroutineScope {
                d.ingredients.map { ing ->
                    async(Dispatchers.IO) {
                        try {
                            val obj = ingredientService.findOne(ing.ingredientId)
                            ing.ingredientId to obj
                        } catch (_: Exception) {
                            null
                        }
                    }
                }.awaitAll()
            }

            val results = pairs.filterNotNull().toMap()
            fullIngredients = results
            fullIngredients.forEach {
                (id, ingredient) ->
                Log.d("DishDetailScreen", "Ingredient loaded: $id -> ${ingredient.title[0].data}")
            }
            Log.d("DishDetailScreen", "Loaded ${results.size} ingredients for dish ${d.id}")
        } catch (_: Exception) {
            // If fetching ingredients fails, keep previous map (no-op)
        } finally {
            isLoadingIngredients = false
        }
    }

    // Trigger ingredients load after dish fetch
    LaunchedEffect(dish) {
        dish?.let { d ->
            try {
                // Call suspend loader directly from LaunchedEffect
                loadFullIngredientsFor(d)
            } catch (_: Exception) {
                // ignore
            }
        }
    }

    // UI with gradient background
    if (isLoading) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator()
        }
        return
    }

    if (errorMessage != null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Text(
                text = errorMessage ?: "Unknown error",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }

    val currentDish = dish ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = currentDish.getTitle(language.code) ?: "Dish")
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                        )
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(innerPadding)
        ) {
            DishImageSection(dish = currentDish, language = language)

            Spacer(modifier = Modifier.height(12.dp))

            DishTitleSection(dish = currentDish, language = language)

            Spacer(modifier = Modifier.height(8.dp))

            QuickInfoSection(dish = currentDish, language = language)

            // Short description
            currentDish.getShortDescription(language.code)?.let { desc ->
                Spacer(modifier = Modifier.height(8.dp))
                DescriptionSection(description = desc, language = language)
            }

            // Full content / recipe
            currentDish.getContent(language.code)?.let { content ->
                Spacer(modifier = Modifier.height(8.dp))
                ContentSection(content = content, language = language)
            }

            // Videos
            if (currentDish.videos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                VideoSection(videos = currentDish.videos, language = language, onOpenUrl = { url ->
                    // default behavior handled in VideoSection
                })
            }

            // Ingredients section - pass current states and handlers
            if (currentDish.ingredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                IngredientSection(
                    modifier = Modifier,
                    dish = currentDish,
                    fullIngredients = fullIngredients,
                    checkedIngredients = checkedIngredients,
                    onToggleIngredient = { ingredientId, isChecked ->
                        checkedIngredients = if (isChecked) checkedIngredients + ingredientId else checkedIngredients - ingredientId
                    },
                    isLoadingIngredients = isLoadingIngredients,
                    onLoadIngredients = {
                        // If not already loading and ingredients map empty, trigger a background load
                        if (!isLoadingIngredients && fullIngredients.isEmpty()) {
                            coroutineScope.launch {
                                try {
                                    loadFullIngredientsFor(currentDish)
                                } catch (_: Exception) {}
                            }
                        }
                    },
                    language = language
                )
            }

        // Tags
        if (currentDish.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            TagSection(tags = currentDish.tags, language = language)
        }

        // Ingredient categories
        if (currentDish.ingredientCategories.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            IngredientCategoriesSection(
                dishIngredientCategories = currentDish.ingredientCategories,
                language = language,
                onCategoryClick = { category ->
                    // handle category click if needed
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
