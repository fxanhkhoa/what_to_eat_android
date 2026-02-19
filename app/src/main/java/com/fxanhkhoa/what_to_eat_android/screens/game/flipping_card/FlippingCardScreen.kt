package com.fxanhkhoa.what_to_eat_android.screens.game.flipping_card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.components.game.flipping_card.DishManagementSection
import com.fxanhkhoa.what_to_eat_android.components.game.flipping_card.DishRevealView
import com.fxanhkhoa.what_to_eat_android.components.game.flipping_card.EmptyGameStateView
import com.fxanhkhoa.what_to_eat_android.components.game.flipping_card.FlippingCardDishPickerView
import com.fxanhkhoa.what_to_eat_android.components.game.flipping_card.GameActionsView
import com.fxanhkhoa.what_to_eat_android.components.game.flipping_card.GameBoardView
import com.fxanhkhoa.what_to_eat_android.components.game.flipping_card.GameHeaderView
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.viewmodel.FlippingCardViewModel
import com.fxanhkhoa.what_to_eat_android.viewmodel.GameState
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlippingCardScreen(
    onDismiss: () -> Unit,
    onNavigateToDishDetail: (DishModel) -> Unit,
    viewModel: FlippingCardViewModel = viewModel()
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    var language by remember { mutableStateOf(Language.ENGLISH) }

    // Observe language changes
    LaunchedEffect(Unit) {
        language = localizationManager.currentLanguage.first()
    }
    val colorScheme = MaterialTheme.colorScheme

    var showDishPicker by remember { mutableStateOf(false) }
    var showGridSizeDialog by remember { mutableStateOf(false) }

    val dishes by viewModel.dishes.collectAsStateWithLifecycle()
    val selectedDishes by viewModel.selectedDishes.collectAsStateWithLifecycle()
    val cards by viewModel.cards.collectAsStateWithLifecycle()
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val selectedDish by viewModel.selectedDish.collectAsStateWithLifecycle()
    val numberOfCards by viewModel.numberOfCards.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadDishes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(localizationManager.getString(R.string.flipping_card, language))
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showGridSizeDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Grid size settings",
                            tint = colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showDishPicker = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add dishes",
                            tint = colorScheme.primary
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp)
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
                    EmptyGameStateView(
                        onAddDishes = { showDishPicker = true },
                        language = language,
                        localizationManager = localizationManager
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Header
                        GameHeaderView(
                            language = language,
                            localizationManager = localizationManager
                        )

                        // Dish management section
                        DishManagementSection(
                            dishes = dishes,
                            onAddDishes = { showDishPicker = true },
                            onRemoveDish = { dish ->
                                viewModel.removeDishFromGame(dish)
                            },
                            language = language,
                            localizationManager = localizationManager
                        )

                        // Game board
                        GameBoardView(
                            cards = cards,
                            onCardTapped = { card ->
                                viewModel.cardTapped(card)
                            },
                            language = language,
                            localizationManager = localizationManager
                        )

                        // Action button
                        GameActionsView(
                            gameState = gameState,
                            onNewGame = { viewModel.startNewGame() },
                            language = language,
                            localizationManager = localizationManager
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }

            // Game completion overlay
            if (gameState == GameState.COMPLETED && selectedDish != null) {
                DishRevealView(
                    dish = selectedDish!!,
                    onNewGame = { viewModel.startNewGame() },
                    onClose = {
                        viewModel.clearError()
                        viewModel.updateSelectedDish(null)
                        viewModel.updateGameState(GameState.PLAYING)
                    },
                    onNavigateToDetail = onNavigateToDishDetail,
                    language = language,
                    localizationManager = localizationManager
                )
            }
        }
    }

    // Grid size settings dialog
    if (showGridSizeDialog) {
        AlertDialog(
            onDismissRequest = { showGridSizeDialog = false },
            title = {
                Text(localizationManager.getString(R.string.select_grid_size, language))
            },
            text = {
                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GridSizeOption(
                        text = localizationManager.getString(R.string.grid_size_small, language),
                        selected = numberOfCards == 6,
                        onClick = {
                            viewModel.updateNumberOfCards(6)
                            showGridSizeDialog = false
                        }
                    )
                    GridSizeOption(
                        text = localizationManager.getString(R.string.grid_size_medium, language),
                        selected = numberOfCards == 9,
                        onClick = {
                            viewModel.updateNumberOfCards(9)
                            showGridSizeDialog = false
                        }
                    )
                    GridSizeOption(
                        text = localizationManager.getString(R.string.grid_size_large, language),
                        selected = numberOfCards == 12,
                        onClick = {
                            viewModel.updateNumberOfCards(12)
                            showGridSizeDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showGridSizeDialog = false }) {
                    Text(localizationManager.getString(R.string.close, language))
                }
            }
        )
    }

    // Dish picker dialog
    if (showDishPicker) {
        Dialog(onDismissRequest = { showDishPicker = false }) {
            FlippingCardDishPickerView(
                selectedDishes = selectedDishes,
                onDishesSelected = { newDishes ->
                    viewModel.updateSelectedDishes(newDishes)
                },
                onDismiss = { showDishPicker = false },
                language = language
            )
        }
    }
}

@Composable
private fun GridSizeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
