package com.fxanhkhoa.what_to_eat_android.components.game.flipping_card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.data.dto.QueryDishDto
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
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
    val colorScheme = MaterialTheme.colorScheme

    var searchText by remember { mutableStateOf("") }
    var localSelectedDishes by remember { mutableStateOf(selectedDishes) }
    val maxDishes = 12

    val dishes by viewModel.dishes.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val query = QueryDishDto()
        viewModel.loadDishes(query)
    }

    LaunchedEffect(searchText) {
        viewModel.updateSearchKeyword(searchText)
    }

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
                        onClick = {
                            onDishesSelected(localSelectedDishes)
                            onDismiss()
                        },
                        enabled = localSelectedDishes.isNotEmpty()
                    ) {
                        Text(localizationManager.getString(R.string.done, language))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorScheme.background,
                            colorScheme.surfaceVariant
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Selected dishes header
                SelectedDishesHeader(
                    selectedDishes = localSelectedDishes,
                    maxDishes = maxDishes,
                    onRemove = { dish ->
                        localSelectedDishes = localSelectedDishes.filter { it.id != dish.id }
                    },
                    language = language,
                    localizationManager = localizationManager
                )

                // Search header
                SearchHeader(
                    searchText = searchText,
                    onSearchTextChanged = { searchText = it },
                    language = language,
                    localizationManager = localizationManager
                )

                // Available dishes grid
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
                    onLoadMore = {
                        viewModel.loadMoreDishes()
                    },
                    onRefresh = {
                        coroutineScope.launch {
                            viewModel.refreshDishes()
                        }
                    },
                    language = language,
                    localizationManager = localizationManager
                )
            }
        }
    }
}

@Composable
private fun SelectedDishesHeader(
    selectedDishes: List<DishModel>,
    maxDishes: Int,
    onRemove: (DishModel) -> Unit,
    language: Language,
    localizationManager: LocalizationManager
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = localizationManager.getString(R.string.selected_dishes, language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "${selectedDishes.size}/$maxDishes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selectedDishes.size >= maxDishes) {
                            Color.Red
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedDishes.isEmpty()) {
                    Text(
                        text = localizationManager.getString(R.string.no_dishes_selected, language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(selectedDishes) { dish ->
                            SelectedDishChip(
                                dish = dish,
                                onRemove = { onRemove(dish) },
                                language = language,
                                localizationManager = localizationManager
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedDishChip(
    dish: DishModel,
    onRemove: () -> Unit,
    language: Language,
    localizationManager: LocalizationManager
) {
    val localizedTitle = dish.getTitle(language.code) ?: dish.slug

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AsyncImage(
                model = dish.thumbnail,
                contentDescription = localizedTitle,
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.ic_launcher_foreground),
                placeholder = painterResource(R.drawable.ic_launcher_foreground)
            )

            Text(
                text = localizedTitle,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color.Red,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchHeader(
    searchText: String,
    onSearchTextChanged: (String) -> Unit,
    language: Language,
    localizationManager: LocalizationManager
) {
    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchTextChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        placeholder = {
            Text(localizationManager.getString(R.string.search_dishes, language))
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
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
private fun AvailableDishesGrid(
    dishes: List<DishModel>,
    selectedDishes: List<DishModel>,
    isLoading: Boolean,
    maxDishes: Int,
    onDishTap: (DishModel) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: suspend () -> Unit,
    language: Language,
    localizationManager: LocalizationManager
) {
    when {
        isLoading && dishes.isEmpty() -> {
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
                Text(
                    text = localizationManager.getString(R.string.no_dishes_found, language),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(dishes, key = { it.id }) { dish ->
                    val isSelected = selectedDishes.any { it.id == dish.id }
                    val canSelect = selectedDishes.size < maxDishes

                    FlippingSelectableDishCard(
                        dish = dish,
                        isSelected = isSelected,
                        canSelect = canSelect,
                        onTap = { onDishTap(dish) },
                        language = language,
                        localizationManager = localizationManager
                    )

                    // Load more when reaching last item
                    if (dish == dishes.lastOrNull()) {
                        LaunchedEffect(dish) {
                            onLoadMore()
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
}

@Composable
private fun FlippingSelectableDishCard(
    dish: DishModel,
    isSelected: Boolean,
    canSelect: Boolean,
    onTap: () -> Unit,
    language: Language,
    localizationManager: LocalizationManager
) {
    val colorScheme = MaterialTheme.colorScheme
    val localizedTitle = dish.getTitle(language.code) ?: dish.slug
    val localizedDescription = dish.getShortDescription(language.code)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable(
                enabled = canSelect || isSelected,
                onClick = onTap
            ),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = if (isSelected) 8.dp else 4.dp,
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, colorScheme.primary)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Image section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                AsyncImage(
                    model = dish.thumbnail,
                    contentDescription = localizedTitle,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.ic_launcher_foreground),
                    placeholder = painterResource(R.drawable.ic_launcher_foreground)
                )

                // Selection overlay
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorScheme.primary.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                } else if (!canSelect) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = "Cannot select",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dish info section
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title
                Text(
                    text = localizedTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(40.dp)
                )

                // Description
                if (localizedDescription != null) {
                    Text(
                        text = localizedDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.height(28.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(28.dp))
                }

                // Bottom info row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dish.preparationTime != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_launcher_foreground),
                                contentDescription = "Time",
                                modifier = Modifier.size(12.dp),
                                tint = colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${dish.preparationTime} ${localizationManager.getString(R.string.mins, language)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (dish.difficultLevel != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = colorScheme.primaryContainer.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = localizationManager.getString(
                                    when (dish.difficultLevel.lowercase()) {
                                        "easy" -> R.string.difficulty_easy
                                        "medium" -> R.string.difficulty_medium
                                        "hard" -> R.string.difficulty_hard
                                        else -> R.string.difficulty_medium
                                    },
                                    language
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
