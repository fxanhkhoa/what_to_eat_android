package com.fxanhkhoa.what_to_eat_android.components.game.wheel_of_fortune

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.components.game.wheel_of_fortune.components.dish_picker.WheelAvailableDishesGrid
import com.fxanhkhoa.what_to_eat_android.components.game.wheel_of_fortune.components.dish_picker.WheelCollectionPickerSection
import com.fxanhkhoa.what_to_eat_android.components.game.wheel_of_fortune.components.dish_picker.WheelPickerMode
import com.fxanhkhoa.what_to_eat_android.components.game.wheel_of_fortune.components.dish_picker.WheelSearchBar
import com.fxanhkhoa.what_to_eat_android.components.game.wheel_of_fortune.components.dish_picker.WheelSelectedDishesHeader
import com.fxanhkhoa.what_to_eat_android.data.dto.QueryDishDto
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.model.UserDishCollectionModel
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider
import com.fxanhkhoa.what_to_eat_android.services.DishService
import com.fxanhkhoa.what_to_eat_android.services.UserDishCollectionService
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.utils.TokenManager
import com.fxanhkhoa.what_to_eat_android.viewmodel.DishListViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelDishPickerView(
    selectedDishes: List<DishModel>,
    onDishesChanged: (List<DishModel>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    maxDishes: Int = 7,
    viewModel: DishListViewModel = viewModel()
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    val dishService = remember { RetrofitProvider.createService<DishService>() }
    val collectionService = remember { RetrofitProvider.createService<UserDishCollectionService>() }
    val tokenManager = remember { TokenManager.getInstance(context) }

    var language by remember { mutableStateOf(Language.ENGLISH) }
    var pickerMode by remember { mutableStateOf(WheelPickerMode.ALL_DISHES) }
    var collections by remember { mutableStateOf<List<UserDishCollectionModel>>(emptyList()) }
    var isCollectionLoading by remember { mutableStateOf(false) }
    var isApplyingCollection by remember { mutableStateOf(false) }
    var collectionError by remember { mutableStateOf<String?>(null) }

    var searchText by remember { mutableStateOf("") }
    val dishes by viewModel.dishes.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        language = localizationManager.currentLanguage.first()
        viewModel.loadDishes(QueryDishDto(page = 1, limit = 20))

        val userId = tokenManager.getUserInfo()?.id
        if (userId.isNullOrBlank()) {
            collectionError = context.getString(R.string.wheel_picker_login_required)
        } else {
            isCollectionLoading = true
            try {
                collections = collectionService.findAll(mapOf("userId" to userId)).data.orEmpty()
            } catch (_: Exception) {
                collectionError = context.getString(R.string.wheel_picker_list_load_failed)
            } finally {
                isCollectionLoading = false
            }
        }
    }

    LaunchedEffect(searchText) { viewModel.updateSearchKeyword(searchText) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = localizationManager.getString(R.string.pick_dishes_for_wheel, language),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = localizationManager.getString(R.string.cancel, language)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onDismiss, enabled = selectedDishes.isNotEmpty()) {
                        Text(localizationManager.getString(R.string.done, language))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            WheelSelectedDishesHeader(
                selectedDishes = selectedDishes,
                maxDishes = maxDishes,
                language = language,
                localizationManager = localizationManager,
                onRemove = { dish -> onDishesChanged(selectedDishes.filter { it.id != dish.id }) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            TabRow(selectedTabIndex = pickerMode.ordinal) {
                Tab(
                    selected = pickerMode == WheelPickerMode.ALL_DISHES,
                    onClick = { pickerMode = WheelPickerMode.ALL_DISHES },
                    text = { Text(localizationManager.getString(R.string.wheel_picker_tab_all_dishes, language)) }
                )
                Tab(
                    selected = pickerMode == WheelPickerMode.MY_LISTS,
                    onClick = { pickerMode = WheelPickerMode.MY_LISTS },
                    text = { Text(localizationManager.getString(R.string.wheel_picker_tab_my_lists, language)) },
                    icon = { Icon(imageVector = Icons.Default.CollectionsBookmark, contentDescription = null) }
                )
            }

            if (pickerMode == WheelPickerMode.ALL_DISHES) {
                WheelSearchBar(
                    searchText = searchText,
                    onSearchTextChanged = { searchText = it },
                    language = language,
                    localizationManager = localizationManager,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
                WheelAvailableDishesGrid(
                    dishes = dishes,
                    selectedDishes = selectedDishes,
                    isLoading = isLoading,
                    maxDishes = maxDishes,
                    language = language,
                    localizationManager = localizationManager,
                    onDishTap = { dish ->
                        if (selectedDishes.any { it.id == dish.id }) {
                            onDishesChanged(selectedDishes.filter { it.id != dish.id })
                        } else if (selectedDishes.size < maxDishes) {
                            onDishesChanged(selectedDishes + dish)
                        }
                    },
                    onLoadMore = { viewModel.loadMoreDishes() },
                    onRefresh = { scope.launch { viewModel.refreshDishes() } },
                    modifier = Modifier.weight(1f)
                )
            } else {
                WheelCollectionPickerSection(
                    collections = collections,
                    isLoading = isCollectionLoading,
                    isApplyingCollection = isApplyingCollection,
                    errorMessage = collectionError,
                    maxDishes = maxDishes,
                    language = language,
                    localizationManager = localizationManager,
                    onApplyCollection = { collection ->
                        if (collection.dishSlugs.orEmpty()
                                .isEmpty()
                        ) return@WheelCollectionPickerSection
                        scope.launch {
                            isApplyingCollection = true
                            val resolvedDishes = collection.dishSlugs.orEmpty()
                                .distinct()
                                .take(maxDishes)
                                .mapNotNull { slug -> runCatching { dishService.findBySlug(slug) }.getOrNull() }
                            if (resolvedDishes.isNotEmpty()) {
                                onDishesChanged(resolvedDishes)
                                pickerMode = WheelPickerMode.ALL_DISHES
                            }
                            isApplyingCollection = false
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

