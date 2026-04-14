package com.fxanhkhoa.what_to_eat_android.components.game.wheel_of_fortune

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fxanhkhoa.what_to_eat_android.R
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

private enum class PickerMode {
    ALL_DISHES,
    MY_LISTS,
}

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
    var pickerMode by remember { mutableStateOf(PickerMode.ALL_DISHES) }
    var collections by remember { mutableStateOf<List<UserDishCollectionModel>>(emptyList()) }
    var isCollectionLoading by remember { mutableStateOf(false) }
    var isApplyingCollection by remember { mutableStateOf(false) }
    var collectionError by remember { mutableStateOf<String?>(null) }

    var searchText by remember { mutableStateOf("") }
    val dishes by viewModel.dishes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()

    // Observe language changes
    LaunchedEffect(Unit) {
        language = localizationManager.currentLanguage.first()
        val query = QueryDishDto(page = 1, limit = 20)
        viewModel.loadDishes(query)

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

    // Handle search text changes
    LaunchedEffect(searchText) {
        viewModel.updateSearchKeyword(searchText)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.pick_dishes_for_wheel),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cancel)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = onDismiss,
                        enabled = selectedDishes.isNotEmpty()
                    ) {
                        Text(stringResource(R.string.done))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Selected dishes header
            SelectedDishesHeader(
                selectedDishes = selectedDishes,
                maxDishes = maxDishes,
                language = language,
                onRemove = { dish ->
                    onDishesChanged(selectedDishes.filter { it.id != dish.id })
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            TabRow(selectedTabIndex = pickerMode.ordinal) {
                Tab(
                    selected = pickerMode == PickerMode.ALL_DISHES,
                    onClick = { pickerMode = PickerMode.ALL_DISHES },
                    text = { Text(stringResource(R.string.wheel_picker_tab_all_dishes)) }
                )
                Tab(
                    selected = pickerMode == PickerMode.MY_LISTS,
                    onClick = { pickerMode = PickerMode.MY_LISTS },
                    text = { Text(stringResource(R.string.wheel_picker_tab_my_lists)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CollectionsBookmark,
                            contentDescription = null
                        )
                    }
                )
            }

            if (pickerMode == PickerMode.ALL_DISHES) {
                SearchBar(
                    searchText = searchText,
                    onSearchTextChanged = { searchText = it },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )

                AvailableDishesGrid(
                    dishes = dishes,
                    selectedDishes = selectedDishes,
                    isLoading = isLoading,
                    maxDishes = maxDishes,
                    language = language,
                    onDishTap = { dish ->
                        if (selectedDishes.any { it.id == dish.id }) {
                            onDishesChanged(selectedDishes.filter { it.id != dish.id })
                        } else if (selectedDishes.size < maxDishes) {
                            onDishesChanged(selectedDishes + dish)
                        }
                    },
                    onLoadMore = {
                        viewModel.loadMoreDishes()
                    },
                    onRefresh = {
                        scope.launch {
                            viewModel.refreshDishes()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                CollectionPickerSection(
                    collections = collections,
                    isLoading = isCollectionLoading,
                    isApplyingCollection = isApplyingCollection,
                    errorMessage = collectionError,
                    maxDishes = maxDishes,
                    onApplyCollection = { collection ->
                        if (collection.dishSlugs.orEmpty().isEmpty()) {
                            return@CollectionPickerSection
                        }

                        scope.launch {
                            isApplyingCollection = true
                            val resolvedDishes = collection.dishSlugs.orEmpty()
                                .distinct()
                                .take(maxDishes)
                                .mapNotNull { slug ->
                                    runCatching { dishService.findBySlug(slug) }.getOrNull()
                                }

                            if (resolvedDishes.isNotEmpty()) {
                                onDishesChanged(resolvedDishes)
                                pickerMode = PickerMode.ALL_DISHES
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

@Composable
private fun CollectionPickerSection(
    collections: List<UserDishCollectionModel>,
    isLoading: Boolean,
    isApplyingCollection: Boolean,
    errorMessage: String?,
    maxDishes: Int,
    onApplyCollection: (UserDishCollectionModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            !errorMessage.isNullOrBlank() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }

            collections.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.wheel_picker_no_lists),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(collections, key = { it.id }) { collection ->
                        val isDisabled = isApplyingCollection || collection.dishSlugs.orEmpty().isEmpty()

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isDisabled) { onApplyCollection(collection) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = collection.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = stringResource(
                                            R.string.wheel_picker_list_dish_count,
                                            collection.dishSlugs.orEmpty().size,
                                            maxDishes
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Button(
                                    onClick = { onApplyCollection(collection) },
                                    enabled = !isDisabled,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isDisabled) {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        } else {
                                            Color(0xFFEE8B2D)
                                        }
                                    )
                                ) {
                                    Text(stringResource(R.string.wheel_picker_apply_list))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedDishesHeader(
    selectedDishes: List<DishModel>,
    maxDishes: Int,
    language: Language,
    onRemove: (DishModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.selected_dishes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${selectedDishes.size}/$maxDishes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedDishes.size >= maxDishes) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selected dishes or empty state
            if (selectedDishes.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_dishes_selected),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    textAlign = TextAlign.Start
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(selectedDishes) { dish ->
                        SelectedDishChip(
                            dish = dish,
                            onRemove = { onRemove(dish) },
                            language = language
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    searchText: String,
    onSearchTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = searchText,
        onValueChange = onSearchTextChanged,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(stringResource(R.string.search_dishes))
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (searchText.isNotEmpty()) {
                IconButton(onClick = { onSearchTextChanged("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.clear_search),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
private fun AvailableDishesGrid(
    dishes: List<DishModel>,
    selectedDishes: List<DishModel>,
    isLoading: Boolean,
    maxDishes: Int,
    language: Language,
    onDishTap: (DishModel) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: suspend () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading && dishes.isEmpty()) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (dishes.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_dishes_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Dishes grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(dishes) { dish ->
                    val isSelected = selectedDishes.any { it.id == dish.id }
                    val canSelect = selectedDishes.size < maxDishes

                    SelectableDishCard(
                        dish = dish,
                        isSelected = isSelected,
                        canSelect = canSelect,
                        onTap = { onDishTap(dish) },
                        language = language
                    )

                    // Load more when reaching the last item
                    if (dish.id == dishes.lastOrNull()?.id) {
                        LaunchedEffect(dish.id) {
                            onLoadMore()
                        }
                    }
                }

                // Loading indicator at the bottom
                if (isLoading && dishes.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
