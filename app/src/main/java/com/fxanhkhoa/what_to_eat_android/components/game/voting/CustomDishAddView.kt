package com.fxanhkhoa.what_to_eat_android.components.game.voting

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.viewmodel.VotingGameCreateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDishAddView(
    viewModel: VotingGameCreateViewModel,
    onDismiss: () -> Unit,
    language: Language = Language.ENGLISH
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    val colorScheme = MaterialTheme.colorScheme

    var dishTitle by remember { mutableStateOf("") }
    var dishURL by remember { mutableStateOf("") }
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }

    val selectedDishes by viewModel.selectedDishes.collectAsStateWithLifecycle()
    val canAddDish = dishTitle.trim().isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(localizationManager.getString(R.string.add_custom_dish, language))
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomActionBar(
                canAddDish = canAddDish,
                onAddDish = {
                    val trimmedTitle = dishTitle.trim()
                    val trimmedURL = dishURL.trim()

                    when {
                        trimmedTitle.isEmpty() -> {
                            alertMessage = localizationManager.getString(
                                R.string.custom_dish_alert_empty_title,
                                language
                            )
                            showAlert = true
                        }
                        selectedDishes.any { it.isCustom && it.customTitle == trimmedTitle } -> {
                            alertMessage = localizationManager.getString(
                                R.string.custom_dish_alert_duplicate_title,
                                language
                            )
                            showAlert = true
                        }
                        trimmedURL.isNotEmpty() && !isValidURL(trimmedURL) -> {
                            alertMessage = localizationManager.getString(
                                R.string.custom_dish_alert_invalid_url,
                                language
                            )
                            showAlert = true
                        }
                        else -> {
                            viewModel.addCustomDish(
                                title = trimmedTitle,
                                url = trimmedURL.ifEmpty { null }
                            )
                            onDismiss()
                        }
                    }
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
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section
            HeaderSection(
                language = language,
                localizationManager = localizationManager
            )

            // Form Section
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                FormSection(
                    dishTitle = dishTitle,
                    dishURL = dishURL,
                    onTitleChange = { dishTitle = it },
                    onURLChange = { dishURL = it },
                    language = language,
                    localizationManager = localizationManager,
                    colorScheme = colorScheme
                )

                PreviewSection(
                    dishTitle = dishTitle,
                    dishURL = dishURL,
                    language = language,
                    localizationManager = localizationManager,
                    colorScheme = colorScheme
                )
            }
        }
    }

    // Alert Dialog
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = {
                Text(localizationManager.getString(R.string.error, language))
            },
            text = {
                Text(alertMessage)
            },
            confirmButton = {
                TextButton(onClick = { showAlert = false }) {
                    Text(localizationManager.getString(R.string.ok, language))
                }
            }
        )
    }
}

@Composable
private fun HeaderSection(
    language: Language,
    localizationManager: LocalizationManager
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFF9800).copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = localizationManager.getString(R.string.add_custom_dish, language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = localizationManager.getString(R.string.custom_dish_header_desc, language),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FormSection(
    dishTitle: String,
    dishURL: String,
    onTitleChange: (String) -> Unit,
    onURLChange: (String) -> Unit,
    language: Language,
    localizationManager: LocalizationManager,
    colorScheme: ColorScheme
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dish Title Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row {
                    Text(
                        text = localizationManager.getString(R.string.custom_dish_title, language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = " *",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Red
                    )
                }

                OutlinedTextField(
                    value = dishTitle,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(localizationManager.getString(R.string.custom_dish_title_placeholder, language))
                    },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }

            // Dish URL Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = localizationManager.getString(R.string.custom_dish_url, language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = dishURL,
                    onValueChange = onURLChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(localizationManager.getString(R.string.custom_dish_url_placeholder, language))
                    },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Text(
                    text = localizationManager.getString(R.string.custom_dish_url_helper, language),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PreviewSection(
    dishTitle: String,
    dishURL: String,
    language: Language,
    localizationManager: LocalizationManager,
    colorScheme: ColorScheme
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = localizationManager.getString(R.string.Preview, language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (dishTitle.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Preview Image
                        AsyncImage(
                            model = dishURL.ifEmpty { null },
                            contentDescription = dishTitle,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.ic_launcher_foreground),
                            placeholder = painterResource(R.drawable.ic_launcher_foreground)
                        )

                        // Preview Info
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dishTitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFF9800).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = localizationManager.getString(R.string.custom_dish, language),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFF9800),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (dishURL.isNotEmpty()) {
                                Text(
                                    text = dishURL,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = localizationManager.getString(R.string.custom_dish_preview_placeholder, language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BottomActionBar(
    canAddDish: Boolean,
    onAddDish: () -> Unit,
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
                        text = localizationManager.getString(R.string.custom_dish, language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = localizationManager.getString(R.string.custom_dish_bottom_desc, language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onAddDish,
                    enabled = canAddDish,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800),
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
                        text = localizationManager.getString(R.string.add_custom_dish, language),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun isValidURL(urlString: String): Boolean {
    return Patterns.WEB_URL.matcher(urlString).matches() &&
           (urlString.startsWith("http://") || urlString.startsWith("https://"))
}

