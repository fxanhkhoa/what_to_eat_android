package com.fxanhkhoa.what_to_eat_android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.components.home.FeaturedDishes
import com.fxanhkhoa.what_to_eat_android.components.SearchBar
import com.fxanhkhoa.what_to_eat_android.components.SearchBarResult
import com.fxanhkhoa.what_to_eat_android.components.home.ContactSection
import com.fxanhkhoa.what_to_eat_android.components.home.HomeBanner
import com.fxanhkhoa.what_to_eat_android.components.home.HomeGames
import com.fxanhkhoa.what_to_eat_android.components.home.RandomDishes
import com.fxanhkhoa.what_to_eat_android.data.dto.QueryDishDto
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onSelectBottomBarItem: ((Int) -> Unit)? = null // Add callback for bottom bar selection
) {
    var searchText by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) } // Integer state counter to trigger refresh
    val coroutineScope = rememberCoroutineScope()

    // Search-related state
    var searchResults by remember { mutableStateOf<List<DishModel>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    val dishService = remember { RetrofitProvider.dishService }

    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    var language by remember { mutableStateOf(Language.ENGLISH) }

    // Observe language changes
    LaunchedEffect(Unit) {
        language = localizationManager.currentLanguage.first()
    }

    // Handle search with debouncing
    LaunchedEffect(searchText) {
        // Cancel previous search job
        searchJob?.cancel()

        if (searchText.isEmpty()) {
            searchResults = emptyList()
            isSearching = false
            return@LaunchedEffect
        }

        // Debounce: wait 500ms before searching
        searchJob = coroutineScope.launch {
            delay(500)
            isSearching = true

            try {
                val query = QueryDishDto(
                    page = 1,
                    limit = 10,
                    keyword = searchText
                )
                val url = query.buildSearchFuzzyUrl()
                val result = dishService.searchFuzzy(url)
                searchResults = result.data
            } catch (e: Exception) {
                searchResults = emptyList()
            } finally {
                isSearching = false
            }
        }
    }

    // Create pull refresh state
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                // Increment refresh trigger to reload FeaturedDishes and RandomDishes
                refreshTrigger++
                // Wait a bit to show the refresh indicator
                delay(1000)
                isRefreshing = false
            }
        }
    )

    val homeText = localizationManager.getString(
        R.string.home,
        language
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
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
                text = homeText,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            SearchBar(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth(),
                language = language
            )
            SearchBarResult(
                text = searchText,
                dishes = searchResults,
                isLoading = isSearching,
                language = language,
                onDishClick = { slug ->
                    navController.navigate("dish/$slug")
                    searchText = "" // Clear search after navigation
                },
                modifier = Modifier.fillMaxWidth()
            )
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                FeaturedDishes(
                    modifier = Modifier.padding(vertical = 16.dp),
                    navController = navController,
                    refreshTrigger = refreshTrigger // Pass refresh trigger
                )

                // Add some additional content for scrolling demonstration
                Spacer(modifier = Modifier.height(24.dp))

                HomeGames(
                    navController = navController,
                    modifier = Modifier.fillMaxWidth(),
                    language = language
                )

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

                RandomDishes(
                    modifier = Modifier,
                    refreshTrigger = refreshTrigger, // Pass refresh trigger
                    navController = navController
                )

                Spacer(modifier = Modifier.height(24.dp))

                ContactSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    language = language
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Pull refresh indicator
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
