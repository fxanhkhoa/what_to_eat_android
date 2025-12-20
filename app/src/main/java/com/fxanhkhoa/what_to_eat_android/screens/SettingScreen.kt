package com.fxanhkhoa.what_to_eat_android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.fxanhkhoa.what_to_eat_android.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen() {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    val localizationManager = remember { LocalizationManager(context) }
    val coroutineScope = rememberCoroutineScope()

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
