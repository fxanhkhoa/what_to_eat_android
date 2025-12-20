package com.fxanhkhoa.what_to_eat_android.screens.game.voting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.components.game.voting.CustomDishAddView
import com.fxanhkhoa.what_to_eat_android.components.game.voting.DishSearchAndSelectView
import com.fxanhkhoa.what_to_eat_android.viewmodel.VotingGameCreateViewModel
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import kotlinx.coroutines.flow.first
// Import separated components
import com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_create.HeaderSection
import com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_create.VoteInfoSection
import com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_create.SelectedDishesSection
import com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_create.AddDishesSection
import com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_create.BottomActionBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VotingCreateScreen(
    onNavigateBack: () -> Unit,
    viewModel: VotingGameCreateViewModel = viewModel()
) {
    val voteTitle by viewModel.voteTitle.collectAsState()
    val voteDescription by viewModel.voteDescription.collectAsState()
    val selectedDishes by viewModel.selectedDishes.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()
    val showSuccess by viewModel.showSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showClearAlert by remember { mutableStateOf(false) }
    var showAddDishSheet by remember { mutableStateOf(false) }
    var showCustomDishSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    var language by remember { mutableStateOf(Language.ENGLISH) }

    // Observe language changes
    LaunchedEffect(Unit) {
        language = localizationManager.currentLanguage.first()
    }

    // Navigate back on success
    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            onNavigateBack()
        }
    }

    // Show error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = localizationManager.getString(R.string.close, language))
                    }
                },
                actions = {
                    TextButton(
                        onClick = { showClearAlert = true },
                        enabled = selectedDishes.isNotEmpty()
                    ) {
                        Text(localizationManager.getString(R.string.clear_all, language))
                    }
                }
            )
        },
        bottomBar = {
            BottomActionBar(
                dishCount = selectedDishes.size,
                canCreate = voteTitle.trim().isNotEmpty() && selectedDishes.size >= 2,
                isCreating = isCreating,
                onCreateClick = { viewModel.createVote() },
                language = language
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Section
            item {
                HeaderSection(language = language)
            }

            // Vote Info Section
            item {
                VoteInfoSection(
                    voteTitle = voteTitle,
                    voteDescription = voteDescription,
                    onTitleChange = { viewModel.updateVoteTitle(it) },
                    onDescriptionChange = { viewModel.updateVoteDescription(it) },
                    language = language
                )
            }

            // Selected Dishes Section
            item {
                SelectedDishesSection(
                    dishes = selectedDishes,
                    onRemoveDish = { index -> viewModel.removeDish(index) },
                    language = language
                )
            }

            // Add Dishes Section
            item {
                AddDishesSection(
                    onSearchAndAdd = { showAddDishSheet = true },
                    onAddCustomDish = { showCustomDishSheet = true },
                    language = language
                )
            }
        }
    }

    // Clear confirmation dialog
    if (showClearAlert) {
        AlertDialog(
            onDismissRequest = { showClearAlert = false },
            title = { Text(localizationManager.getString(R.string.confirm_clear, language)) },
            text = { Text(localizationManager.getString(R.string.clear_all_dishes_confirmation, language)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllDishes()
                        showClearAlert = false
                    }
                ) {
                    Text(localizationManager.getString(R.string.clear, language), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAlert = false }) {
                    Text(localizationManager.getString(R.string.cancel, language))
                }
            }
        )
    }

    // Add Dish Bottom Sheet
    if (showAddDishSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddDishSheet = false }
        ) {
            DishSearchAndSelectView(
                votingViewModel = viewModel,
                onDismiss = { showAddDishSheet = false },
                language = language
            )
        }
    }

    // Custom Dish Bottom Sheet
    if (showCustomDishSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCustomDishSheet = false }
        ) {
            CustomDishAddView(
                viewModel = viewModel,
                onDismiss = { showCustomDishSheet = false }
            )
        }
    }
}
