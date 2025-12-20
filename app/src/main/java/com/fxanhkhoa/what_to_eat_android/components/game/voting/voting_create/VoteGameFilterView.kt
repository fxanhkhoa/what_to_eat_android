package com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.viewmodel.VoteGameListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteGameFilterView(
    viewModel: VoteGameListViewModel,
    onDismiss: () -> Unit,
    language: Language = Language.ENGLISH
) {
    val sortBy by viewModel.sortBy.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val hasActiveFilters = viewModel.hasActiveFilters

    var selectedSortBy by remember { mutableStateOf(sortBy) }
    var selectedSortOrder by remember { mutableStateOf(sortOrder) }

    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(localizationManager.getString(R.string.filter_vote_games, language))
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = localizationManager.getString(R.string.cancel, language))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            selectedSortBy = "createdAt"
                            selectedSortOrder = "desc"
                        },
                        enabled = hasActiveFilters
                    ) {
                        Text(localizationManager.getString(R.string.reset, language))
                    }
                }
            )
        },
        bottomBar = {
            BottomActionBar(
                hasActiveFilters = hasActiveFilters,
                onReset = {
                    selectedSortBy = "createdAt"
                    selectedSortOrder = "desc"
                },
                onApply = {
                    viewModel.applySorting(selectedSortBy, selectedSortOrder)
                    onDismiss()
                },
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
                VoteFilterHeaderSection(language = language)
            }

            // Sort Section
            item {
                SortSection(
                    selectedSortBy = selectedSortBy,
                    selectedSortOrder = selectedSortOrder,
                    onSortByChange = { selectedSortBy = it },
                    onSortOrderChange = { selectedSortOrder = it },
                    language = language
                )
            }

            // Quick Actions Section
            item {
                QuickActionsSection(
                    onQuickFilter = { sortBy, sortOrder ->
                        selectedSortBy = sortBy
                        selectedSortOrder = sortOrder
                    },
                    language = language
                )
            }
        }
    }
}

@Composable
private fun VoteFilterHeaderSection(language: Language = Language.ENGLISH) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = localizationManager.getString(R.string.filter_and_sort, language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = localizationManager.getString(R.string.customize_vote_games_display, language),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SortSection(
    selectedSortBy: String,
    selectedSortOrder: String,
    onSortByChange: (String) -> Unit,
    onSortOrderChange: (String) -> Unit,
    language: Language = Language.ENGLISH
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = localizationManager.getString(R.string.sort_options, language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Sort By
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = localizationManager.getString(R.string.sort_by, language),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SortOptionRow(
                        title = localizationManager.getString(R.string.creation_date, language),
                        subtitle = localizationManager.getString(R.string.sort_by_creation_date, language),
                        value = "createdAt",
                        selectedValue = selectedSortBy,
                        onSelect = onSortByChange
                    )

                    SortOptionRow(
                        title = localizationManager.getString(R.string.title, language),
                        subtitle = localizationManager.getString(R.string.sort_by_title, language),
                        value = "title",
                        selectedValue = selectedSortBy,
                        onSelect = onSortByChange
                    )

                    SortOptionRow(
                        title = localizationManager.getString(R.string.vote_count, language),
                        subtitle = localizationManager.getString(R.string.sort_by_vote_count, language),
                        value = "voteCount",
                        selectedValue = selectedSortBy,
                        onSelect = onSortByChange
                    )

                    SortOptionRow(
                        title = localizationManager.getString(R.string.dish_count, language),
                        subtitle = localizationManager.getString(R.string.sort_by_dish_count, language),
                        value = "dishCount",
                        selectedValue = selectedSortBy,
                        onSelect = onSortByChange
                    )
                }
            }

            // Sort Order
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = localizationManager.getString(R.string.sort_order, language),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SortOrderButton(
                        title = localizationManager.getString(R.string.newest_first, language),
                        icon = Icons.Default.ArrowDownward,
                        value = "desc",
                        selectedValue = selectedSortOrder,
                        onSelect = onSortOrderChange,
                        modifier = Modifier.weight(1f)
                    )

                    SortOrderButton(
                        title = localizationManager.getString(R.string.oldest_first, language),
                        icon = Icons.Default.ArrowUpward,
                        value = "asc",
                        selectedValue = selectedSortOrder,
                        onSelect = onSortOrderChange,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SortOptionRow(
    title: String,
    subtitle: String,
    value: String,
    selectedValue: String,
    onSelect: (String) -> Unit
) {
    val isSelected = selectedValue == value

    Card(
        onClick = { onSelect(value) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            } else {
                Color.Transparent
            }
        ),
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        }
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
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SortOrderButton(
    title: String,
    icon: ImageVector,
    value: String,
    selectedValue: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = selectedValue == value

    Button(
        onClick = { onSelect(value) },
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    onQuickFilter: (String, String) -> Unit,
    language: Language = Language.ENGLISH
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = localizationManager.getString(R.string.quick_filters, language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickFilterButton(
                    title = localizationManager.getString(R.string.most_popular, language),
                    subtitle = localizationManager.getString(R.string.sort_by_vote_count_desc, language),
                    icon = Icons.Default.ThumbUp,
                    onClick = { onQuickFilter("voteCount", "desc") }
                )

                QuickFilterButton(
                    title = localizationManager.getString(R.string.recently_created, language),
                    subtitle = localizationManager.getString(R.string.show_newest_first, language),
                    icon = Icons.Default.Schedule,
                    onClick = { onQuickFilter("createdAt", "desc") }
                )

                QuickFilterButton(
                    title = localizationManager.getString(R.string.most_dishes, language),
                    subtitle = localizationManager.getString(R.string.votes_with_most_dishes, language),
                    icon = Icons.Default.RestaurantMenu,
                    onClick = { onQuickFilter("dishCount", "desc") }
                )

                QuickFilterButton(
                    title = localizationManager.getString(R.string.alphabetical, language),
                    subtitle = localizationManager.getString(R.string.sort_by_title_az, language),
                    icon = Icons.Default.SortByAlpha,
                    onClick = { onQuickFilter("title", "asc") }
                )
            }
        }
    }
}

@Composable
private fun QuickFilterButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun BottomActionBar(
    hasActiveFilters: Boolean,
    onReset: () -> Unit,
    onApply: () -> Unit,
    language: Language = Language.ENGLISH
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Column {
            Divider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onReset,
                    enabled = hasActiveFilters,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(localizationManager.getString(R.string.reset, language))
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = localizationManager.getString(R.string.apply_filters, language),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
