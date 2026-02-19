package com.fxanhkhoa.what_to_eat_android.screens.game.voting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_create.VoteGameFilterView
import com.fxanhkhoa.what_to_eat_android.model.DishVoteItem
import com.fxanhkhoa.what_to_eat_android.model.DishVoteModel
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.viewmodel.AuthViewModel
import com.fxanhkhoa.what_to_eat_android.viewmodel.VoteGameListViewModel
import com.fxanhkhoa.what_to_eat_android.utils.DateUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun VoteGameListScreen(
    authViewModel: AuthViewModel,
    navController: NavController,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: VoteGameListViewModel = viewModel<VoteGameListViewModel>(),
    language: Language = Language.ENGLISH
) {
    val voteGames by viewModel.voteGames.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasMorePages by viewModel.hasMorePages.collectAsState()
    val hasActiveFilters = viewModel.hasActiveFilters
    val isAuthenticated by authViewModel.isLoggedIn.collectAsState()

    var searchText by remember { mutableStateOf("") }
    var showingCreateVote by remember { mutableStateOf(false) }
    var showingFilterSheet by remember { mutableStateOf(false) }
    var voteGameForResults by remember { mutableStateOf<DishVoteModel?>(null) }
    var hasLoadedAfterLogin by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    val scope = rememberCoroutineScope()

    // Load data when authenticated
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            if (!hasLoadedAfterLogin) {
                viewModel.refreshVoteGames()
            }
        }
    }

    // Load data on first appear
    LaunchedEffect(Unit) {
        if (isAuthenticated) {
            viewModel.loadVoteGames()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localizationManager.getString(R.string.voting_game, language)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = localizationManager.getString(
                                R.string.back,
                                language
                            )
                        )
                    }
                },
                actions = {
                    if (isAuthenticated) {
                        IconButton(onClick = { showingCreateVote = true }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = localizationManager.getString(
                                    R.string.create_vote,
                                    language
                                ),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        if (!isAuthenticated) {
            LoginRequiredView(
                onLogin = onNavigateToLogin,
                onBack = onNavigateBack,
                language = language,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            AuthenticatedContentView(
                voteGames = voteGames,
                isLoading = isLoading,
                hasMorePages = hasMorePages,
                hasActiveFilters = hasActiveFilters,
                searchText = searchText,
                onSearchTextChange = {
                    searchText = it
                    viewModel.updateSearchKeyword(it)
                },
                onFilterClick = { showingFilterSheet = true },
                onVoteGameClick = {
                    // Navigate to vote game detail screen
                    // Note: Don't set selectedVoteGame here as it would create double rendering
                    navController.navigate("dish_vote_game/${it.id}")
                },
                onShowResults = { voteGameForResults = it },
                onLoadMore = { viewModel.loadMoreVoteGames() },
                onRefresh = {
                    scope.launch {
                        viewModel.refreshVoteGames()
                    }
                },
                onCreate = { showingCreateVote = true },
                language = language,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }

    // Create Vote Sheet
    if (showingCreateVote) {
        VotingCreateScreen(
            onNavigateBack = {
                showingCreateVote = false
                viewModel.loadVoteGames()
            }
        )
    }

    // Filter Sheet
    if (showingFilterSheet) {
        VoteGameFilterView(
            viewModel = viewModel,
            onDismiss = {
                showingFilterSheet = false
                viewModel.loadVoteGames()
            },
            language = language
        )
    }


    // Results Sheet
    voteGameForResults?.let { voteGame ->
        VoteResultsView(
            dishVote = voteGame,
            onDismiss = { voteGameForResults = null }
        )
    }
}

@Composable
private fun LoginRequiredView(
    onLogin: () -> Unit,
    onBack: () -> Unit,
    language: Language = Language.ENGLISH,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Icon
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title and Message
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = localizationManager.getString(R.string.login_required, language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = localizationManager.getString(R.string.login_required_message, language),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Person, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizationManager.getString(R.string.login_to_continue, language))
            }

            TextButton(onClick = onBack) {
                Text(
                    text = localizationManager.getString(R.string.go_back, language),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
private fun AuthenticatedContentView(
    voteGames: List<DishVoteModel>?,
    isLoading: Boolean,
    hasMorePages: Boolean,
    hasActiveFilters: Boolean,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onVoteGameClick: (DishVoteModel) -> Unit,
    onShowResults: (DishVoteModel) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: suspend () -> Unit,
    onCreate: () -> Unit,
    language: Language = Language.ENGLISH,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    val scope = rememberCoroutineScope()

    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(isRefreshing, onRefresh = {
        isRefreshing = true
        scope.launch {
            delay(1000) // Simulate network delay
            isRefreshing = false
        }
    })

    Column(modifier = modifier.fillMaxSize()) {
        // Search and Filter Header
        SearchAndFilterHeader(
            searchText = searchText,
            onSearchTextChange = onSearchTextChange,
            hasActiveFilters = hasActiveFilters,
            onFilterClick = onFilterClick,
            language = language
        )

        // Vote Games List with Pull-to-Refresh
        Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
            when {
                isLoading && (voteGames == null || voteGames.isEmpty()) -> {
                    LoadingView()
                }

                voteGames == null || voteGames.isEmpty() -> {
                    EmptyStateView(
                        title = localizationManager.getString(
                            R.string.no_vote_games_found,
                            language
                        ),
                        subtitle = if (hasActiveFilters || searchText.isNotEmpty()) {
                            localizationManager.getString(R.string.try_adjusting_search, language)
                        } else {
                            localizationManager.getString(R.string.create_first_vote_game, language)
                        },
                        icon = Icons.AutoMirrored.Filled.List,
                        onCreate = if (hasActiveFilters || searchText.isNotEmpty()) null else onCreate,
                        createButtonText = localizationManager.getString(
                            R.string.create_vote,
                            language
                        )
                    )
                }

                else -> {
                    VoteGamesList(
                        voteGames = voteGames,
                        hasMorePages = hasMorePages,
                        isLoading = isLoading,
                        onVoteGameClick = onVoteGameClick,
                        onShowResults = onShowResults,
                        onLoadMore = onLoadMore,
                        language = language
                    )
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun SearchAndFilterHeader(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    hasActiveFilters: Boolean,
    onFilterClick: () -> Unit,
    language: Language = Language.ENGLISH
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    localizationManager.getString(
                        R.string.search_vote_games,
                        language
                    )
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = { onSearchTextChange("") }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = localizationManager.getString(
                                R.string.clear,
                                language
                            )
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        // Filter Button
        IconButton(
            onClick = onFilterClick,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            Box {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = localizationManager.getString(R.string.filter, language),
                    tint = if (hasActiveFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (hasActiveFilters) {
                    Surface(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd),
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ) {}
                }
            }
        }
    }
}

@Composable
private fun VoteGamesList(
    voteGames: List<DishVoteModel>,
    hasMorePages: Boolean,
    isLoading: Boolean,
    onVoteGameClick: (DishVoteModel) -> Unit,
    onShowResults: (DishVoteModel) -> Unit,
    onLoadMore: () -> Unit,
    language: Language = Language.ENGLISH
) {
    val listState = rememberLazyListState()

    if (isLoading && voteGames.isEmpty()) {
        LoadingView()
        return
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(voteGames, key = { it.id }) { voteGame ->
            VoteGameCard(
                voteGame = voteGame,
                onVoteNow = { onVoteGameClick(voteGame) },
                onShowResults = { onShowResults(voteGame) },
                language = language
            )
        }

        // Load More Indicator
        if (hasMorePages) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                    LaunchedEffect(Unit) {
                        onLoadMore()
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyStateView(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onCreate: (() -> Unit)? = null,
    createButtonText: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // Create Button
        if (onCreate != null && createButtonText != null) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onCreate,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = createButtonText)
            }
        }
    }
}

// MARK: - Vote Game Card
@Composable
fun VoteGameCard(
    voteGame: DishVoteModel,
    onVoteNow: () -> Unit,
    onShowResults: () -> Unit,
    language: Language = Language.ENGLISH
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    var showingShareSheet by remember { mutableStateOf(false) }

    val totalVotes = voteGame.dishVoteItems.sumOf {
        it.voteUser.size + it.voteAnonymous.size
    }
    val dishCount = voteGame.dishVoteItems.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with Share Button
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = voteGame.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Share Button
                        IconButton(
                            onClick = { showingShareSheet = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = localizationManager.getString(
                                    R.string.share,
                                    language
                                ),
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Status Badge
                        Surface(
                            color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = localizationManager.getString(R.string.active, language),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }

                if (voteGame.description.isNotEmpty()) {
                    Text(
                        text = voteGame.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    // Dish Count
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$dishCount ${
                                localizationManager.getString(
                                    R.string.dishes,
                                    language
                                )
                            }",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Vote Count
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$totalVotes ${
                                localizationManager.getString(
                                    R.string.votes,
                                    language
                                )
                            }",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Created Date
                Text(
                    text = DateUtil.formatDate(voteGame.createdAt, language),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Dish Preview
            if (voteGame.dishVoteItems.isNotEmpty()) {
                DishPreviewSection(
                    dishVoteItems = voteGame.dishVoteItems,
                    language = language
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onShowResults,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        localizationManager.getString(R.string.view_results, language),
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onVoteNow,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = localizationManager.getString(R.string.vote_now, language),
                        fontWeight = FontWeight.Medium,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                }
            }
        }
    }

    // Share Sheet
    if (showingShareSheet) {
        // TODO: Implement share functionality
        showingShareSheet = false
    }
}

@Composable
private fun DishPreviewSection(
    dishVoteItems: List<DishVoteItem>,
    language: Language = Language.ENGLISH
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = localizationManager.getString(R.string.dishes_in_this_vote, language),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            dishVoteItems.take(5).forEach { item ->
                DishPreviewChip(item = item, language = language)
            }

            if (dishVoteItems.size > 5) {
                Surface(
                    color = Color.Gray.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "+${dishVoteItems.size - 5} ${
                            localizationManager.getString(
                                R.string.more,
                                language
                            )
                        }",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun DishPreviewChip(
    item: DishVoteItem,
    language: Language = Language.ENGLISH
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    val backgroundColor = if (item.isCustom) {
        Color(0xFFFF9800).copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    }

    val contentColor = if (item.isCustom) Color(0xFFFF9800) else MaterialTheme.colorScheme.primary

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (item.isCustom) Icons.Default.Person else Icons.Default.RestaurantMenu,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = contentColor
            )
            Text(
                text = if (item.isCustom) {
                    item.customTitle ?: localizationManager.getString(R.string.custom, language)
                } else {
                    item.slug
                },
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
