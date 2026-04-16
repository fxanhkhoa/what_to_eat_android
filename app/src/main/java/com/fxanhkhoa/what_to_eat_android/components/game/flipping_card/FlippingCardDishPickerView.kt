package com.fxanhkhoa.what_to_eat_android.components.game.flipping_card

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.components.game.flipping_card.components.dish_picker.AvailableDishesGrid
import com.fxanhkhoa.what_to_eat_android.components.game.flipping_card.components.dish_picker.FlippingCollectionPickerSection
import com.fxanhkhoa.what_to_eat_android.components.game.flipping_card.components.dish_picker.FlippingPickerMode
import com.fxanhkhoa.what_to_eat_android.components.game.flipping_card.components.dish_picker.SearchHeader
import com.fxanhkhoa.what_to_eat_android.components.game.flipping_card.components.dish_picker.SelectedDishesHeader
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlippingCardDishPickerView(
    selectedDishes: List<DishModel>,
    onDishesSelected: (List<DishModel>) -> Unit,
    onDismiss: () -> Unit,
    language: Language = Language.ENGLISH,
    viewModel: DishListViewModel = viewModel()
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    val dishService = remember { RetrofitProvider.createService<DishService>() }
    val collectionService = remember { RetrofitProvider.createService<UserDishCollectionService>() }
    val tokenManager = remember { TokenManager.getInstance(context) }

    var searchText by remember { mutableStateOf("") }
    var localSelectedDishes by remember { mutableStateOf(selectedDishes) }
    val maxDishes = 12

    var pickerMode by remember { mutableStateOf(FlippingPickerMode.ALL_DISHES) }
    var collections by remember { mutableStateOf<List<UserDishCollectionModel>>(emptyList()) }
    var isCollectionLoading by remember { mutableStateOf(false) }
    var isApplyingCollection by remember { mutableStateOf(false) }
    var collectionError by remember { mutableStateOf<String?>(null) }

    val dishes by viewModel.dishes.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadDishes(QueryDishDto())
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
                        localizationManager.getString(R.string.pick_dishes_for_cards, language),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onDismiss) {
                        Text(localizationManager.getString(R.string.cancel, language))
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onDishesSelected(localSelectedDishes); onDismiss() },
                        enabled = localSelectedDishes.isNotEmpty()
                    ) {
                        Text(localizationManager.getString(R.string.done, language))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SelectedDishesHeader(
                selectedDishes = localSelectedDishes,
                maxDishes = maxDishes,
                onRemove = { dish ->
                    localSelectedDishes = localSelectedDishes.filter { it.id != dish.id }
                },
                language = language,
                localizationManager = localizationManager
            )

            TabRow(selectedTabIndex = pickerMode.ordinal) {
                Tab(
                    selected = pickerMode == FlippingPickerMode.ALL_DISHES,
                    onClick = { pickerMode = FlippingPickerMode.ALL_DISHES },
                    text = { Text(localizationManager.getString(R.string.wheel_picker_tab_all_dishes, language)) }
                )
                Tab(
                    selected = pickerMode == FlippingPickerMode.MY_LISTS,
                    onClick = { pickerMode = FlippingPickerMode.MY_LISTS },
                    text = { Text(localizationManager.getString(R.string.wheel_picker_tab_my_lists, language)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CollectionsBookmark,
                            contentDescription = null
                        )
                    }
                )
            }

            if (pickerMode == FlippingPickerMode.ALL_DISHES) {
                SearchHeader(
                    searchText = searchText,
                    onSearchTextChanged = { searchText = it },
                    language = language,
                    localizationManager = localizationManager
                )
                AvailableDishesGrid(
                    dishes = dishes,
                    selectedDishes = localSelectedDishes,
                    isLoading = isLoading,
                    maxDishes = maxDishes,
                    onDishTap = { dish ->
                        val isSelected = localSelectedDishes.any { it.id == dish.id }
                        localSelectedDishes = if (isSelected) {
                            localSelectedDishes.filter { it.id != dish.id }
                        } else if (localSelectedDishes.size < maxDishes) {
                            localSelectedDishes + dish
                        } else {
                            localSelectedDishes
                        }
                    },
                    onLoadMore = { viewModel.loadMoreDishes() },
                    onRefresh = { coroutineScope.launch { viewModel.refreshDishes() } },
                    language = language,
                    localizationManager = localizationManager
                )
            } else {
                FlippingCollectionPickerSection(
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
                        ) return@FlippingCollectionPickerSection
                        coroutineScope.launch {
                            isApplyingCollection = true
                            val resolvedDishes = collection.dishSlugs.orEmpty()
                                .distinct()
                                .take(maxDishes)
                                .mapNotNull { slug ->
                                    runCatching { dishService.findBySlug(slug) }.getOrNull()
                                }
                            if (resolvedDishes.isNotEmpty()) {
                                localSelectedDishes = resolvedDishes
                                pickerMode = FlippingPickerMode.ALL_DISHES
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


