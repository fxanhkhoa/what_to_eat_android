package com.fxanhkhoa.what_to_eat_android.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.ui.theme.GradientEnd
import com.fxanhkhoa.what_to_eat_android.ui.theme.GradientStart
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.utils.rememberSharedAuthViewModel
import com.fxanhkhoa.what_to_eat_android.viewmodel.EditProfileViewModel
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val authViewModel = rememberSharedAuthViewModel()
    val viewModel = remember { EditProfileViewModel(context, authViewModel) }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val localizationManager = remember { LocalizationManager(context) }
    var currentLanguage by remember { mutableStateOf(Language.ENGLISH) }
    LaunchedEffect(Unit) {
        currentLanguage = localizationManager.currentLanguage.first()
    }

    // Localized strings
    val str = remember(currentLanguage) {
        fun s(id: Int) = localizationManager.getString(id, currentLanguage)
        object {
            val title          = s(R.string.edit_profile_title)
            val save           = s(R.string.edit_profile_save)
            val saving         = s(R.string.edit_profile_saving)
            val saveChanges    = s(R.string.edit_profile_save_changes)
            val success        = s(R.string.edit_profile_success)
            val sectionPersonal= s(R.string.edit_profile_section_personal)
            val fullName       = s(R.string.edit_profile_full_name)
            val email          = s(R.string.edit_profile_email)
            val sectionContact = s(R.string.edit_profile_section_contact)
            val phone          = s(R.string.edit_profile_phone)
            val address        = s(R.string.edit_profile_address)
            val sectionAdditional = s(R.string.edit_profile_section_additional)
            val dateOfBirth    = s(R.string.edit_profile_date_of_birth)
            val selectDate     = s(R.string.edit_profile_select_date)
            val avatarHint     = s(R.string.edit_profile_avatar_hint)
            val avatarTitle    = s(R.string.edit_profile_avatar_dialog_title)
            val avatarPrompt   = s(R.string.edit_profile_avatar_dialog_prompt)
            val avatarUrlLabel = s(R.string.edit_profile_avatar_url_label)
            val avatarUrlPlaceholder = s(R.string.edit_profile_avatar_url_placeholder)
            val avatarPreview  = s(R.string.edit_profile_avatar_preview)
            val avatarApply    = s(R.string.edit_profile_avatar_apply)
            val cancel         = s(R.string.cancel)
        }
    }

    // Handle save success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar(str.success)
            viewModel.clearSuccess()
            onSaveSuccess()
        }
    }

    // Handle error
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = str.title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = str.cancel)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveProfile() },
                        enabled = !uiState.isSaving && !uiState.isLoading
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                        } else {
                            Text(text = str.save, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(modifier = modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = modifier.fillMaxSize().padding(innerPadding).background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar section
            item {
                AvatarSection(
                    avatarUrl = uiState.avatarUrl,
                    name = uiState.name,
                    onAvatarUrlChange = { viewModel.onAvatarUrlChange(it) },
                    hint = str.avatarHint,
                    dialogTitle = str.avatarTitle,
                    dialogPrompt = str.avatarPrompt,
                    urlLabel = str.avatarUrlLabel,
                    urlPlaceholder = str.avatarUrlPlaceholder,
                    previewLabel = str.avatarPreview,
                    applyLabel = str.avatarApply,
                    cancelLabel = str.cancel
                )
            }

            // Personal Info section
            item {
                EditSection(title = str.sectionPersonal) {
                    EditField(value = uiState.name, onValueChange = { viewModel.onNameChange(it) }, label = str.fullName, icon = Icons.Filled.Person,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    EditField(value = uiState.email, onValueChange = {}, label = str.email, icon = Icons.Filled.Email, enabled = false,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next))
                }
            }

            // Contact Info section
            item {
                EditSection(title = str.sectionContact) {
                    EditField(value = uiState.phone, onValueChange = { viewModel.onPhoneChange(it) }, label = str.phone, icon = Icons.Filled.Phone,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    EditField(value = uiState.address, onValueChange = { viewModel.onAddressChange(it) }, label = str.address, icon = Icons.Filled.LocationOn,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next))
                }
            }

            // Additional Info section
            item {
                EditSection(title = str.sectionAdditional) {
                    DatePickerField(
                        value = uiState.dateOfBirth,
                        onValueChange = { viewModel.onDateOfBirthChange(it) },
                        label = str.dateOfBirth,
                        selectDatePlaceholder = str.selectDate,
                        language = currentLanguage
                    )
                }
            }

            // Save button (floating CTA at bottom)
            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.saveProfile() },
                    enabled = !uiState.isSaving && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text(str.saving, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    } else {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(str.saveChanges, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun AvatarSection(
    avatarUrl: String,
    name: String,
    onAvatarUrlChange: (String) -> Unit,
    hint: String,
    dialogTitle: String,
    dialogPrompt: String,
    urlLabel: String,
    urlPlaceholder: String,
    previewLabel: String,
    applyLabel: String,
    cancelLabel: String
) {
    var showUrlDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(GradientStart.copy(alpha = 0.15f), GradientEnd.copy(alpha = 0.05f))))
                .padding(vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Avatar
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier.size(90.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUrl.isNotEmpty()) {
                            AsyncImage(model = avatarUrl, contentDescription = "Avatar",
                                modifier = Modifier.size(86.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        } else {
                            Text(
                                text = if (name.isNotEmpty()) name.first().uppercaseChar().toString() else "?",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Edit badge
                    Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                        IconButton(onClick = { showUrlDialog = true }, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = dialogTitle,
                                tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Text(text = hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    // Avatar URL dialog
    if (showUrlDialog) {
        var urlInput by remember { mutableStateOf(avatarUrl) }
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            title = { Text(dialogTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = dialogPrompt, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(value = urlInput, onValueChange = { urlInput = it }, label = { Text(urlLabel) },
                        placeholder = { Text(urlPlaceholder) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri), shape = RoundedCornerShape(10.dp))
                    // Preview
                    AnimatedVisibility(visible = urlInput.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AsyncImage(model = urlInput, contentDescription = previewLabel,
                                modifier = Modifier.size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                            Text(previewLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onAvatarUrlChange(urlInput); showUrlDialog = false }) {
                    Text(applyLabel, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUrlDialog = false }) { Text(cancelLabel) }
            }
        )
    }
}

@Composable
private fun EditSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun EditField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    enabled: Boolean = true,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(22.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = if (placeholder.isNotEmpty()) {
                { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }
            } else null,
            enabled = enabled,
            singleLine = true,
            modifier = Modifier.weight(1f),
            keyboardOptions = keyboardOptions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(10.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    selectDatePlaceholder: String = "Select date",
    language: Language = Language.ENGLISH
) {
    var showPicker by remember { mutableStateOf(false) }

    val isoFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'") }
    val displayFormatter = remember(language) {
        DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.forLanguageTag(language.code))
    }

    val initialMillis = remember(value) {
        if (value.isNotEmpty()) {
            runCatching {
                runCatching {
                    java.time.LocalDateTime.parse(value, isoFormatter).toInstant(ZoneOffset.UTC)
                }.getOrElse {
                    java.time.LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(ZoneOffset.UTC).toInstant()
                }.toEpochMilli()
            }.getOrNull()
        } else null
    }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    val displayText = remember(value, language) {
        if (value.isNotEmpty()) {
            runCatching {
                val localDate = runCatching {
                    java.time.LocalDateTime.parse(value, isoFormatter).toLocalDate()
                }.getOrElse {
                    java.time.LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
                }
                localDate.format(displayFormatter)
            }.getOrElse { value }
        } else ""
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = Icons.Filled.Cake, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            label = { Text(label) },
            placeholder = { Text(selectDatePlaceholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            readOnly = true,
            singleLine = true,
            modifier = Modifier.weight(1f).clickable { showPicker = true },
            trailingIcon = {
                IconButton(onClick = { showPicker = true }) {
                    Icon(imageVector = Icons.Filled.CalendarMonth, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
                }
            },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.primary,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(10.dp)
        )
    }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val isoString = Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).format(isoFormatter)
                        onValueChange(isoString)
                    }
                    showPicker = false
                }) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text(selectDatePlaceholder) }
            },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary,
                    todayContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}


