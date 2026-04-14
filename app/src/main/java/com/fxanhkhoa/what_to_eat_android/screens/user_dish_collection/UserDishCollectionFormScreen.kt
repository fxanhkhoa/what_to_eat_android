package com.fxanhkhoa.what_to_eat_android.screens.user_dish_collection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.utils.TokenManager
import com.fxanhkhoa.what_to_eat_android.viewmodel.UserDishCollectionFormViewModel
import kotlinx.coroutines.flow.first

private val availableIcons = listOf(
    "cake" to "🎂",
    "favorite" to "❤️",
    "celebration" to "🎉",
    "restaurant" to "🍽️",
    "local_bar" to "🍹",
    "restaurant_menu" to "📋",
    "dinner_dining" to "🍴",
    "brunch_dining" to "🥞",
    "fastfood" to "🍔",
    "ramen_dining" to "🍜",
    "set_meal" to "🍱",
    "lunch_dining" to "🥗"
)

private val availableColors = listOf(
    "#ef4444" to "Red",
    "#f97316" to "Orange",
    "#eab308" to "Yellow",
    "#22c55e" to "Green",
    "#3b82f6" to "Blue",
    "#8b5cf6" to "Purple",
    "#ec4899" to "Pink",
    "#6366f1" to "Indigo"
)

private val occasions = listOf(
    "birthday", "anniversary", "holiday", "party",
    "wedding", "picnic", "date night", "family gathering", "other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDishCollectionFormScreen(
    collectionId: String? = null,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    viewModel: UserDishCollectionFormViewModel = viewModel()
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    var language by remember { mutableStateOf(Language.ENGLISH) }

    val collection by viewModel.collection.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val selectedDishes by viewModel.selectedDishes.collectAsStateWithLifecycle()
    val saveSuccess by viewModel.saveSuccess.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var userId by remember { mutableStateOf("") }

    // Form state
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedOccasion by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("cake") }
    var selectedColor by remember { mutableStateOf("#3b82f6") }
    var isPublic by remember { mutableStateOf(false) }
    var dishSearchQuery by remember { mutableStateOf("") }
    var occasionDropdownExpanded by remember { mutableStateOf(false) }

    val isEditMode = collectionId != null
    val strings = remember(language) {
        fun s(id: Int) = localizationManager.getString(id, language)
        object {
            val createTitle = s(R.string.create_collection)
            val editTitle = s(R.string.edit_collection)
            val name = s(R.string.collection_name)
            val description = s(R.string.collection_description)
            val occasion = s(R.string.collection_occasion)
            val icon = s(R.string.collection_icon)
            val color = s(R.string.collection_color)
            val isPublicLabel = s(R.string.collection_is_public)
            val dishes = s(R.string.collection_dishes)
            val searchHint = s(R.string.search_dishes_hint)
            val save = s(R.string.save_collection)
            val back = s(R.string.back)
            val nameRequired = s(R.string.collection_name_required)
        }
    }

    // Load on start
    LaunchedEffect(Unit) {
        language = localizationManager.currentLanguage.first()
        val ui = TokenManager.getInstance(context).getUserInfo()
        userId = ui?.id ?: ""
        if (collectionId != null) {
            viewModel.loadCollection(collectionId)
        }
    }

    // Populate form when collection loads
    LaunchedEffect(collection) {
        collection?.let { col ->
            name = col.name
            description = col.description ?: ""
            selectedOccasion = col.occasion ?: ""
            selectedIcon = col.icon ?: "cake"
            selectedColor = col.color ?: "#3b82f6"
            isPublic = col.isPublic
        }
    }

    // Navigate back on save success
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) strings.editTitle else strings.createTitle,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            viewModel.save(
                                name = name,
                                description = description,
                                occasion = selectedOccasion,
                                icon = selectedIcon,
                                color = selectedColor,
                                isPublic = isPublic,
                                userId = userId,
                                collectionId = collectionId
                            )
                        },
                        enabled = !isSaving && name.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(strings.save)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Error
            errorMessage?.let { err ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = err,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Name
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.name) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = name.isBlank(),
                    singleLine = true,
                    supportingText = if (name.isBlank()) {
                        { Text(strings.nameRequired) }
                    } else null
                )
            }

            // Description
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(strings.description) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }

            // Occasion
            item {
                ExposedDropdownMenuBox(
                    expanded = occasionDropdownExpanded,
                    onExpandedChange = { occasionDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedOccasion,
                        onValueChange = {},
                        label = { Text(strings.occasion) },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = occasionDropdownExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = occasionDropdownExpanded,
                        onDismissRequest = { occasionDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("—") },
                            onClick = { selectedOccasion = ""; occasionDropdownExpanded = false }
                        )
                        occasions.forEach { occ ->
                            DropdownMenuItem(
                                text = { Text(occ) },
                                onClick = { selectedOccasion = occ; occasionDropdownExpanded = false }
                            )
                        }
                    }
                }
            }

            // Icon picker
            item {
                Text(strings.icon, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableIcons) { (iconName, emoji) ->
                        val isSelected = selectedIcon == iconName
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedIcon = iconName },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }

            // Color picker
            item {
                Text(strings.color, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableColors) { (hex, _) ->
                        val color = runCatching { Color(android.graphics.Color.parseColor(hex)) }
                            .getOrElse { Color(0xFF3b82f6) }
                        val isSelected = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(color, RoundedCornerShape(20.dp))
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedColor = hex }
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // isPublic toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(strings.isPublicLabel, style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = isPublic, onCheckedChange = { isPublic = it })
                }
            }

            // Dishes section header
            item {
                Text(strings.dishes, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }

            // Selected dishes chips
            if (selectedDishes.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        selectedDishes.chunked(2).forEach { pair ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                pair.forEach { dish ->
                                    SelectedDishChip(
                                        dish = dish,
                                        onRemove = { viewModel.removeDish(dish.slug) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Dish search
            item {
                OutlinedTextField(
                    value = dishSearchQuery,
                    onValueChange = {
                        dishSearchQuery = it
                        viewModel.updateSearchQuery(it)
                    },
                    label = { Text(strings.searchHint) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (dishSearchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                dishSearchQuery = ""
                                viewModel.updateSearchQuery("")
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true
                )
            }

            // Search results
            if (searchResults.isNotEmpty()) {
                items(searchResults) { dish ->
                    val isAlreadyAdded = selectedDishes.any { it.slug == dish.slug }
                    SearchResultDishRow(
                        dish = dish,
                        isAdded = isAlreadyAdded,
                        onAdd = { viewModel.addDish(dish) },
                        onRemove = { viewModel.removeDish(dish.slug) }
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SelectedDishChip(
    dish: DishModel,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = dish.title.firstOrNull()?.data ?: dish.slug
    InputChip(
        selected = true,
        onClick = onRemove,
        label = { Text(title, maxLines = 1) },
        trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) },
        modifier = modifier
    )
}

@Composable
private fun SearchResultDishRow(
    dish: DishModel,
    isAdded: Boolean,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    val title = dish.title.firstOrNull()?.data ?: dish.slug
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (isAdded) onRemove() else onAdd() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        IconButton(onClick = { if (isAdded) onRemove() else onAdd() }) {
            Icon(
                if (isAdded) Icons.Default.RemoveCircle else Icons.Default.AddCircle,
                contentDescription = null,
                tint = if (isAdded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
    HorizontalDivider()
}
