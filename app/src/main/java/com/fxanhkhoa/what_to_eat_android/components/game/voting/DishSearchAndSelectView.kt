package com.fxanhkhoa.what_to_eat_android.components.game.voting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.components.game.voting.components.dish_picker.DishSelectableRow
import com.fxanhkhoa.what_to_eat_android.components.game.voting.components.dish_picker.VotingBottomActionBar
import com.fxanhkhoa.what_to_eat_android.components.game.voting.components.dish_picker.VotingCollectionPickerSection
import com.fxanhkhoa.what_to_eat_android.components.game.voting.components.dish_picker.VotingPickerMode
import com.fxanhkhoa.what_to_eat_android.components.game.voting.components.dish_picker.VotingSearchBar
import com.fxanhkhoa.what_to_eat_android.data.dto.QueryDishDto
import com.fxanhkhoa.what_to_eat_android.model.UserDishCollectionModel
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider
import com.fxanhkhoa.what_to_eat_android.services.DishService
import com.fxanhkhoa.what_to_eat_android.services.UserDishCollectionService
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.utils.TokenManager
import com.fxanhkhoa.what_to_eat_android.viewmodel.DishListViewModel
import com.fxanhkhoa.what_to_eat_android.viewmodel.VotingGameCreateViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishSearchAndSelectView(
    votingViewModel: VotingGameCreateViewModel,
    onDismiss: () -> Unit,
    language: Language = Language.ENGLISH,
    startOnMyLists: Boolean = false,
    dishViewModel: DishListViewModel = viewModel()
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    val colorScheme = MaterialTheme.colorScheme
    val dishService = remember { RetrofitProvider.createService<DishService>() }
    val collectionService = remember { RetrofitProvider.createService<UserDishCollectionService>() }
    val tokenManager = remember { TokenManager.getInstance(context) }

    var searchText by remember { mutableStateOf("") }
    var selectedDishSlugs by remember { mutableStateOf(setOf<String>()) }
    var pickerMode by remember {
        mutableStateOf(if (startOnMyLists) VotingPickerMode.MY_LISTS else VotingPickerMode.ALL_DISHES)
    }
    var collections by remember { mutableStateOf<List<UserDishCollectionModel>>(emptyList()) }
    var isCollectionLoading by remember { mutableStateOf(false) }
    var isApplyingCollection by remember { mutableStateOf(false) }
    var collectionError by remember { mutableStateOf<String?>(null) }

    val dishes by dishViewModel.dishes.collectAsStateWithLifecycle()
    val isLoading by dishViewModel.isLoading.collectAsStateWithLifecycle()
    val selectedVoteDishes by votingViewModel.selectedDishes.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val query = QueryDishDto(page = 1, limit = 10)
        dishViewModel.loadDishes(query)

        val userId = tokenManager.getUserInfo()?.id
        if (userId.isNullOrBlank()) {
            collectionError = context.getString(R.string.wheel_picker_login_required)
        } else {
            isCollectionLoading = true
            try {
                val response = collectionService.findAll(mapOf("userId" to userId))
                collections = response.data.orEmpty()
            } catch (_: Exception) {
                collectionError = context.getString(R.string.wheel_picker_list_load_failed)
            } finally {
                isCollectionLoading = false
            }
        }
    }

    LaunchedEffect(searchText) {
        dishViewModel.updateSearchKeyword(searchText)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(localizationManager.getString(R.string.search_and_add, language))
                },
                navigationIcon = {
                    TextButton(onClick = onDismiss) {
                        Text(localizationManager.getString(R.string.cancel, language))
                    }
                },
                actions = {
                    if (pickerMode == VotingPickerMode.ALL_DISHES) {
                        TextButton(
                            onClick = {
                                selectedDishSlugs.forEach { slug ->
                                    dishes.firstOrNull { it.slug == slug }?.let { dish ->
                                        votingViewModel.addDish(dish)
                                    }
                                }
                                selectedDishSlugs = setOf()
                                onDismiss()
                            },
                            enabled = selectedDishSlugs.isNotEmpty()
                        ) {
                            Text(localizationManager.getString(R.string.done, language))
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (pickerMode == VotingPickerMode.ALL_DISHES) {
                VotingBottomActionBar(
                    selectedCount = selectedDishSlugs.size,
                    onAddDishes = {
                        selectedDishSlugs.forEach { slug ->
                            dishes.firstOrNull { it.slug == slug }?.let { dish ->
                                votingViewModel.addDish(dish)
                            }
                        }
                        selectedDishSlugs = setOf()
                        onDismiss()
                    },
                    language = language,
                    localizationManager = localizationManager
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mode tabs
            TabRow(selectedTabIndex = pickerMode.ordinal) {
                Tab(
                    selected = pickerMode == VotingPickerMode.ALL_DISHES,
                    onClick = { pickerMode = VotingPickerMode.ALL_DISHES },
                    text = { Text(localizationManager.getString(R.string.wheel_picker_tab_all_dishes, language)) }
                )
                Tab(
                    selected = pickerMode == VotingPickerMode.MY_LISTS,
                    onClick = { pickerMode = VotingPickerMode.MY_LISTS },
                    text = { Text(localizationManager.getString(R.string.wheel_picker_tab_my_lists, language)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CollectionsBookmark,
                            contentDescription = null
                        )
                    }
                )
            }

            if (pickerMode == VotingPickerMode.ALL_DISHES) {
                // Search Bar
                VotingSearchBar(
                    searchText = searchText,
                    onSearchTextChanged = { searchText = it },
                    language = language,
                    localizationManager = localizationManager,
                    colorScheme = colorScheme
                )

                // Dish List
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    dishes.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_launcher_foreground),
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = localizationManager.getString(R.string.no_dishes_found, language),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = localizationManager.getString(R.string.try_adjusting_search_criteria, language),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(dishes, key = { it.id }) { dish ->
                                val isSelected = selectedDishSlugs.contains(dish.slug)
                                val isAlreadyAdded = selectedVoteDishes.any {
                                    !it.isCustom && it.slug == dish.slug
                                }

                                DishSelectableRow(
                                    dish = dish,
                                    isSelected = isSelected,
                                    isAlreadyAdded = isAlreadyAdded,
                                    onToggle = {
                                        if (!isAlreadyAdded) {
                                            selectedDishSlugs = if (isSelected) {
                                                selectedDishSlugs - dish.slug
                                            } else {
                                                selectedDishSlugs + dish.slug
                                            }
                                        }
                                    },
                                    language = language,
                                    localizationManager = localizationManager,
                                    colorScheme = colorScheme
                                )

                                // Load more when reaching last item
                                if (dish == dishes.lastOrNull()) {
                                    LaunchedEffect(dish) {
                                        dishViewModel.loadMoreDishes()
                                    }
                                }
                            }

                            // Loading indicator at bottom
                            if (isLoading && dishes.isNotEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // My Lists tab — bulk-add collection dishes to the vote
                VotingCollectionPickerSection(
                    collections = collections,
                    isLoading = isCollectionLoading,
                    isApplyingCollection = isApplyingCollection,
                    errorMessage = collectionError,
                    language = language,
                    localizationManager = localizationManager,
                    onApplyCollection = { collection ->
                        if (collection.dishSlugs.orEmpty()
                                .isEmpty()
                        ) return@VotingCollectionPickerSection
                        coroutineScope.launch {
                            isApplyingCollection = true
                            collection.dishSlugs.orEmpty().distinct().forEach { slug ->
                                runCatching { dishService.findBySlug(slug) }
                                    .getOrNull()
                                    ?.let { votingViewModel.addDish(it) }
                            }
                            isApplyingCollection = false
                            onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


