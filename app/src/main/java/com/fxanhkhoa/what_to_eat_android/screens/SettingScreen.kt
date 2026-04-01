package com.fxanhkhoa.what_to_eat_android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.fxanhkhoa.what_to_eat_android.ui.localization.*
import com.fxanhkhoa.what_to_eat_android.ui.theme.*
import com.fxanhkhoa.what_to_eat_android.data.dto.NotificationPreferences
import com.fxanhkhoa.what_to_eat_android.data.dto.UpdateNotificationPreferencesDto
import com.fxanhkhoa.what_to_eat_android.utils.rememberSharedAuthViewModel
import com.fxanhkhoa.what_to_eat_android.utils.rememberSharedNotificationViewModel
import com.fxanhkhoa.what_to_eat_android.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(onNavigateToPrivacyPolicy: () -> Unit = {}) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    val localizationManager = remember { LocalizationManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // Notification preferences
    val authViewModel = rememberSharedAuthViewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
    val notificationViewModel = rememberSharedNotificationViewModel()
    val notificationPrefs by notificationViewModel.preferences.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) notificationViewModel.loadPreferences()
    }

    // Collect theme preferences
    val isDarkTheme by themeManager.isDarkTheme.collectAsStateWithLifecycle(initialValue = true)
    val useDynamicColor by themeManager.useDynamicColor.collectAsStateWithLifecycle(initialValue = false)
    val followSystemTheme by themeManager.followSystemTheme.collectAsStateWithLifecycle(initialValue = false)

    // Collect language preference
    val currentLanguage by localizationManager.currentLanguage.collectAsStateWithLifecycle(initialValue = Language.ENGLISH)

    // Get localized strings that update immediately when language changes
    val settingsText = remember(currentLanguage) { localizationManager.getString(R.string.settings, currentLanguage) }
    val appearanceText = remember(currentLanguage) { localizationManager.getString(R.string.appearance, currentLanguage) }
    val themeText = remember(currentLanguage) { localizationManager.getString(R.string.theme, currentLanguage) }
    val lightText = remember(currentLanguage) { localizationManager.getString(R.string.light, currentLanguage) }
    val darkText = remember(currentLanguage) { localizationManager.getString(R.string.dark, currentLanguage) }
    val systemText = remember(currentLanguage) { localizationManager.getString(R.string.system, currentLanguage) }
    val materialYouText = remember(currentLanguage) { localizationManager.getString(R.string.use_material_you, currentLanguage) }
    val languageText = remember(currentLanguage) { localizationManager.getString(R.string.language, currentLanguage) }
    val aboutText = remember(currentLanguage) { localizationManager.getString(R.string.about, currentLanguage) }
    val versionText = remember(currentLanguage) { localizationManager.getString(R.string.version, currentLanguage) }
    val privacyPolicyText = remember(currentLanguage) { localizationManager.getString(R.string.privacy_policy, currentLanguage) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                    )
                )
            ),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Text(
                text = settingsText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Appearance Section
        item {
            SettingsCard(
                title = appearanceText
            ) {
                // Theme Selection
                Text(
                    text = themeText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Theme Options
                Column(
                    modifier = Modifier
                        .selectableGroup()
                        .padding(bottom = 16.dp)
                ) {
                    ThemeOption(
                        text = lightText,
                        icon = Icons.Filled.WbSunny,
                        isSelected = !followSystemTheme && !isDarkTheme,
                        onClick = {
                            coroutineScope.launch {
                                themeManager.setFollowSystemTheme(false)
                                themeManager.setDarkTheme(false)
                            }
                        }
                    )

                    ThemeOption(
                        text = darkText,
                        icon = Icons.Filled.NightsStay,
                        isSelected = !followSystemTheme && isDarkTheme,
                        onClick = {
                            coroutineScope.launch {
                                themeManager.setFollowSystemTheme(false)
                                themeManager.setDarkTheme(true)
                            }
                        }
                    )

                    ThemeOption(
                        text = systemText,
                        icon = Icons.Filled.Settings,
                        isSelected = followSystemTheme,
                        onClick = {
                            coroutineScope.launch {
                                themeManager.setFollowSystemTheme(true)
                            }
                        }
                    )
                }

                // Dynamic Colors Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp)
                        )
                        Text(
                            text = materialYouText,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Switch(
                        checked = useDynamicColor,
                        onCheckedChange = { newValue ->
                            coroutineScope.launch {
                                themeManager.setDynamicColor(newValue)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }

        // Language Section
        item {
            SettingsCard(
                title = languageText
            ) {
                Column(
                    modifier = Modifier.selectableGroup()
                ) {
                    LanguageOption(
                        language = Language.ENGLISH,
                        currentLanguage = currentLanguage,
                        onClick = { newLanguage ->
                            coroutineScope.launch {
                                localizationManager.setLanguage(newLanguage)
                            }
                        }
                    )

                    LanguageOption(
                        language = Language.VIETNAMESE,
                        currentLanguage = currentLanguage,
                        onClick = { newLanguage ->
                            coroutineScope.launch {
                                localizationManager.setLanguage(newLanguage)
                            }
                        }
                    )
                }
            }
        }

        // Notifications Section (only shown when logged in)
        if (isLoggedIn) {
            item {
                SettingsCard(title = "Notifications") {
                    val prefs = notificationPrefs

                    // Helper to send a partial update
                    fun patch(block: UpdateNotificationPreferencesDto.() -> UpdateNotificationPreferencesDto) {
                        notificationViewModel.updatePreferences(
                            UpdateNotificationPreferencesDto().block()
                        )
                    }

                    // ── Per-type toggles ─────────────────────────────────────
                    NotificationToggleRow(
                        icon = Icons.Filled.Chat,
                        label = "Chat Notifications",
                        description = "Messages from other users",
                        checked = prefs?.chatEnabled ?: true,
                        enabled = prefs != null,
                        onCheckedChange = { enabled ->
                            notificationViewModel.updatePreferences(
                                UpdateNotificationPreferencesDto(chatEnabled = enabled)
                            )
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    NotificationToggleRow(
                        icon = Icons.Filled.Notifications,
                        label = "Activity Notifications",
                        description = "Votes, game results and app activity",
                        checked = prefs?.activityEnabled ?: true,
                        enabled = prefs != null,
                        onCheckedChange = { enabled ->
                            notificationViewModel.updatePreferences(
                                UpdateNotificationPreferencesDto(activityEnabled = enabled)
                            )
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    NotificationToggleRow(
                        icon = Icons.Filled.Campaign,
                        label = "Marketing Notifications",
                        description = "Promotions, tips and new features",
                        checked = prefs?.marketingEnabled ?: true,
                        enabled = prefs != null,
                        onCheckedChange = { enabled ->
                            notificationViewModel.updatePreferences(
                                UpdateNotificationPreferencesDto(marketingEnabled = enabled)
                            )
                        }
                    )

                    // ── Quiet hours ───────────────────────────────────────────
                    if (prefs != null) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Text(
                            text = "Quiet Hours",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        var quietStart by remember { mutableStateOf(prefs.quietHoursStart ?: "") }
                        var quietEnd   by remember { mutableStateOf(prefs.quietHoursEnd   ?: "") }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = quietStart,
                                onValueChange = { quietStart = it },
                                label = { Text("From", fontSize = 12.sp) },
                                placeholder = { Text("22:00", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                trailingIcon = {
                                    if (quietStart.isNotBlank()) {
                                        IconButton(onClick = {
                                            quietStart = ""
                                            notificationViewModel.updatePreferences(
                                                UpdateNotificationPreferencesDto(quietHoursStart = "")
                                            )
                                        }) {
                                            Icon(Icons.Filled.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            )

                            OutlinedTextField(
                                value = quietEnd,
                                onValueChange = { quietEnd = it },
                                label = { Text("Until", fontSize = 12.sp) },
                                placeholder = { Text("08:00", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                trailingIcon = {
                                    if (quietEnd.isNotBlank()) {
                                        IconButton(onClick = {
                                            quietEnd = ""
                                            notificationViewModel.updatePreferences(
                                                UpdateNotificationPreferencesDto(quietHoursEnd = "")
                                            )
                                        }) {
                                            Icon(Icons.Filled.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                notificationViewModel.updatePreferences(
                                    UpdateNotificationPreferencesDto(
                                        quietHoursStart = quietStart.ifBlank { null },
                                        quietHoursEnd   = quietEnd.ifBlank { null }
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Quiet Hours")
                        }
                    }
                }
            }
        }

        // About Section
        item {
            SettingsCard(
                title = aboutText
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = versionText,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "1.0.0",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToPrivacyPolicy() }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = privacyPolicyText,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            content()
        }
    }
}

@Composable
private fun ThemeOption(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            fontSize = 16.sp,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NotificationToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = label, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
private fun LanguageOption(
    language: Language,
    currentLanguage: Language,
    onClick: (Language) -> Unit
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    val englishText = localizationManager.getString(R.string.english, currentLanguage)
    val vietnameseText = localizationManager.getString(R.string.vietnamese, currentLanguage)
    val isSelected = currentLanguage == language
    val languageText = when (language) {
        Language.ENGLISH -> englishText
        Language.VIETNAMESE -> vietnameseText
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = { onClick(language) }
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onClick(language) },
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = languageText,
            fontSize = 16.sp,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
