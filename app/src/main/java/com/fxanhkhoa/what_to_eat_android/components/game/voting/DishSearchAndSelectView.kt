package com.fxanhkhoa.what_to_eat_android.components.game.voting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.fxanhkhoa.what_to_eat_android.viewmodel.VotingGameCreateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishSearchAndSelectView(
    votingViewModel: VotingGameCreateViewModel,
    onDismiss: () -> Unit,
    language: Language = Language.ENGLISH,
    dishViewModel: DishListViewModel = viewModel()
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    val colorScheme = MaterialTheme.colorScheme

    var searchText by remember { mutableStateOf("") }
    var selectedDishSlugs by remember { mutableStateOf(setOf<String>()) }

    val dishes by dishViewModel.dishes.collectAsStateWithLifecycle()
    val isLoading by dishViewModel.isLoading.collectAsStateWithLifecycle()
    val selectedVoteDishes by votingViewModel.selectedDishes.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        val query = QueryDishDto(page = 1, limit = 10)
        dishViewModel.loadDishes(query)
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
                    TextButton(
                        onClick = {
                            // Add selected dishes
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
            )
        },
        bottomBar = {
            BottomActionBar(
                selectedCount = selectedDishSlugs.size,
                onAddDishes = {
                    // Add selected dishes
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            SearchBar(
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
        }
    }
}

@Composable
private fun SearchBar(
    searchText: String,
    onSearchTextChanged: (String) -> Unit,
    language: Language,
    localizationManager: LocalizationManager,
    colorScheme: ColorScheme
) {
    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchTextChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = {
            Text(localizationManager.getString(R.string.search_dishes, language))
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray
            )
        },
        trailingIcon = {
            if (searchText.isNotEmpty()) {
                IconButton(onClick = { onSearchTextChanged("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color.Gray
                    )
                }
            }
        },
        shape = RoundedCornerShape(10.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = colorScheme.surfaceVariant,
            focusedContainerColor = colorScheme.surfaceVariant
        )
    )
}

@Composable
private fun DishSelectableRow(
    dish: DishModel,
    isSelected: Boolean,
    isAlreadyAdded: Boolean,
    onToggle: () -> Unit,
    language: Language,
    localizationManager: LocalizationManager,
    colorScheme: ColorScheme
) {
    val localizedTitle = dish.getTitle(language.code) ?: dish.slug
    val localizedDescription = dish.getShortDescription(language.code)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isAlreadyAdded, onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = if (isSelected) 4.dp else 2.dp,
        color = colorScheme.surface,
        border = if (isAlreadyAdded) {
            androidx.compose.foundation.BorderStroke(2.dp, Color.Green)
        } else if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, colorScheme.primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dish Image
            AsyncImage(
                model = dish.thumbnail,
                contentDescription = localizedTitle,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.ic_launcher_foreground),
                placeholder = painterResource(R.drawable.ic_launcher_foreground)
            )

            // Dish Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = localizedTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (localizedDescription != null) {
                    Text(
                        text = localizedDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dish.preparationTime != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
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
                                text = dish.difficultLevel.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Selection Indicator
            when {
                isAlreadyAdded -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Already added",
                        tint = Color.Green,
                        modifier = Modifier.size(28.dp)
                    )
                }
                isSelected -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Outlined.Circle,
                        contentDescription = "Not selected",
                        tint = Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomActionBar(
    selectedCount: Int,
    onAddDishes: () -> Unit,
    language: Language,
    localizationManager: LocalizationManager
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Column {
            Divider()

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
                        text = "$selectedCount ${localizationManager.getString(R.string.dishes_selected, language)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (selectedCount > 0) {
                        Text(
                            text = localizationManager.getString(R.string.add_more_dishes, language),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onAddDishes,
                    enabled = selectedCount > 0,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color.Gray
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = localizationManager.getString(R.string.add_dishes, language),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun NumberOfCardsEditor(
    numberOfCards: Int,
    onNumberOfCardsChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(2, 3, 4, 5, 6, 7, 8, 9, 10)
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Number of Voting Options",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$numberOfCards options",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            painter = painterResource(
                                if (expanded) android.R.drawable.arrow_up_float
                                else android.R.drawable.arrow_down_float
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "$option options",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (option == numberOfCards) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onNumberOfCardsChanged(option)
                                expanded = false
                            },
                            leadingIcon = {
                                if (option == numberOfCards) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                    }
                }
            }

            Text(
                text = "Select how many dishes will be in the voting",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
