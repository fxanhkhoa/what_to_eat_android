package com.fxanhkhoa.what_to_eat_android.screens.game.wheel_of_fortune

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.components.game.wheel_of_fortune.*
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider
import com.fxanhkhoa.what_to_eat_android.services.DishService
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelOfFortuneScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDishDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    var language by remember { mutableStateOf(Language.ENGLISH) }

    var dishes by remember { mutableStateOf<List<DishModel>>(emptyList()) }
    var selectedDishes by remember { mutableStateOf<List<DishModel>>(emptyList()) }
    var rotationAngle by remember { mutableStateOf(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var selectedDish by remember { mutableStateOf<DishModel?>(null) }
    var showDishPicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val dishService = remember { RetrofitProvider.createService<DishService>() }
    val scrollState = rememberScrollState()

    // Animated rotation
    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = if (isSpinning) {
            tween(durationMillis = 3000, easing = FastOutSlowInEasing)
        } else {
            tween(durationMillis = 500, easing = LinearEasing)
        },
        label = "wheel_rotation"
    )

    // Load language and dishes on start
    LaunchedEffect(Unit) {
        language = localizationManager.currentLanguage.first()

        if (selectedDishes.isEmpty()) {
            try {
                val randomDishes = dishService.findRandom(limit = 7)
                dishes = randomDishes
                selectedDishes = randomDishes
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                // Handle error - could show a snackbar or error message
            }
        } else {
            dishes = selectedDishes
            isLoading = false
        }
    }

    // Update dishes when selection changes
    LaunchedEffect(selectedDishes) {
        dishes = selectedDishes
        selectedDish = null
        isSpinning = false
    }

    // Auto-scroll to result when selected dish appears
    LaunchedEffect(selectedDish) {
        if (selectedDish != null) {
            kotlinx.coroutines.delay(300) // Small delay to ensure result section is rendered
            scope.launch {
                scrollState.animateScrollTo(
                    value = scrollState.maxValue,
                    animationSpec = tween(
                        durationMillis = 800,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDishPicker = true }) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = stringResource(R.string.add_dishes),
                            tint = Color(0xFFF3A446)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(30.dp)
            ) {
                // Header
                HeaderSection()

                // Dish Management Section
                DishManagementSection(
                    dishes = dishes,
                    language = language,
                    onRemoveDish = { dish ->
                        selectedDishes = selectedDishes.filter { it.id != dish.id }
                    },
                    onTapDish = { dish ->
                        onNavigateToDishDetail(dish.slug)
                    },
                    onAddDishes = { showDishPicker = true }
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 50.dp),
                        color = Color(0xFFF3A446)
                    )
                } else {
                    // Wheel Container
                    WheelContainer(
                        dishes = dishes,
                        rotationAngle = animatedRotation,
                        language = language,
                        onSectionTap = { dish ->
                            onNavigateToDishDetail(dish.slug)
                        }
                    )

                    // Spin Button
                    SpinButton(
                        isSpinning = isSpinning,
                        isEnabled = dishes.isNotEmpty() && !isSpinning,
                        onSpin = {
                            if (dishes.isNotEmpty() && !isSpinning) {
                                isSpinning = true
                                selectedDish = null

                                // Calculate random spin
                                val randomSpins = Random.nextInt(3, 7) * 360f
                                val randomAngle = Random.nextFloat() * 360f
                                rotationAngle += randomSpins + randomAngle

                                // Calculate selected dish after spin
                                scope.launch {
                                    kotlinx.coroutines.delay(3000)

                                    // The pointer is at the top (270 degrees from 0)
                                    // Sections start at 0 degrees (right/3 o'clock) and go clockwise
                                    val finalAngle = rotationAngle % 360f
                                    val sectionAngle = 360f / dishes.size

                                    // Calculate which section is at the top (270 degrees)
                                    // We need to find which section the pointer (at 270°) is pointing to
                                    val pointerPosition = 270f

                                    // Adjust for the wheel's rotation - subtract rotation to find original position
                                    val adjustedPointerAngle = (pointerPosition - finalAngle) % 360f
                                    val normalizedAngle = if (adjustedPointerAngle < 0f) adjustedPointerAngle + 360f else adjustedPointerAngle

                                    // Calculate which section this angle falls into
                                    var selectedIndex = (normalizedAngle / sectionAngle).toInt() % dishes.size
                                    if (selectedIndex < 0) selectedIndex += dishes.size

                                    selectedDish = dishes[selectedIndex]
                                    isSpinning = false
                                }
                            }
                        }
                    )

                    // Result Section
                    selectedDish?.let { dish ->
                        ResultSection(
                            dish = dish,
                            language = language,
                            onTap = { onNavigateToDishDetail(dish.slug) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }

    // Dish Picker Sheet
    if (showDishPicker) {
        ModalBottomSheet(
            onDismissRequest = { showDishPicker = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            WheelDishPickerView(
                selectedDishes = selectedDishes,
                onDishesChanged = { newDishes ->
                    selectedDishes = newDishes
                },
                onDismiss = { showDishPicker = false },
                maxDishes = 7
            )
        }
    }
}

@Composable
private fun HeaderSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFF3A446),
            modifier = Modifier.size(40.dp)
        )

        Text(
            text = stringResource(R.string.wheel_of_fortune),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = stringResource(R.string.wheel_of_fortune_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DishManagementSection(
    dishes: List<DishModel>,
    language: Language,
    onRemoveDish: (DishModel) -> Unit,
    onTapDish: (DishModel) -> Unit,
    onAddDishes: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dishes_on_wheel),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${dishes.size}/7",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Dishes or empty state
            if (dishes.isEmpty()) {
                EmptyDishesState(onAddDishes = onAddDishes)
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(dishes) { dish ->
                        WheelDishChip(
                            dish = dish,
                            onRemove = { onRemoveDish(dish) },
                            onTap = { onTapDish(dish) },
                            language = language
                        )
                    }

                    // Add more button
                    if (dishes.size < 7) {
                        item {
                            AddMoreButton(onClick = onAddDishes)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyDishesState(onAddDishes: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp)
        )

        Text(
            text = stringResource(R.string.no_dishes_on_wheel),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onAddDishes,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF3A446)
            )
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.add_dishes))
        }
    }
}

@Composable
private fun AddMoreButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(80.dp, 100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3A446).copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = Color(0xFFF3A446)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color(0xFFF3A446),
                modifier = Modifier.size(32.dp)
            )

            Text(
                text = stringResource(R.string.add_more),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFF3A446)
            )
        }
    }
}

@Composable
private fun WheelContainer(
    dishes: List<DishModel>,
    rotationAngle: Float,
    language: Language,
    onSectionTap: (DishModel) -> Unit
) {
    Box(
        modifier = Modifier.size(300.dp),
        contentAlignment = Alignment.Center
    ) {
        // Wheel
        WheelCanvas(
            dishes = dishes,
            rotationAngle = rotationAngle,
            language = language,
            onSectionTap = onSectionTap
        )

        // Pointer at top
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
        ) {
            WheelPointer()
        }
    }
}

@Composable
private fun SpinButton(
    isSpinning: Boolean,
    isEnabled: Boolean,
    onSpin: () -> Unit
) {
    Button(
        onClick = onSpin,
        enabled = isEnabled,
        modifier = Modifier
            .height(56.dp)
            .widthIn(min = 200.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF3A446),
            disabledContainerColor = Color(0xFFF3A446).copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Icon(
            imageVector = if (isSpinning) Icons.Default.Lock else Icons.Default.PlayArrow,
            contentDescription = null,
            tint = Color.White
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = stringResource(if (isSpinning) R.string.spinning else R.string.spin_the_wheel),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
private fun ResultSection(
    dish: DishModel,
    language: Language,
    onTap: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.winner_celebration),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF3A446)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTap() },
            shape = RoundedCornerShape(15.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dish image
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(dish.thumbnail)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Dish image",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = dish.getTitle(language.code) ?: dish.slug,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2
                    )

                    Text(
                        text = stringResource(R.string.tap_to_view_details),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
