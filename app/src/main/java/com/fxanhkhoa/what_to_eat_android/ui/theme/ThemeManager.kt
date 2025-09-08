package com.fxanhkhoa.what_to_eat_android.ui.theme

import android.content.Context
import androidx.compose.runtime.*
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

class ThemeManager(private val context: Context) {
    companion object {
        private val IS_DARK_THEME_KEY = booleanPreferencesKey("is_dark_theme")
        private val USE_DYNAMIC_COLOR_KEY = booleanPreferencesKey("use_dynamic_color")
        private val FOLLOW_SYSTEM_THEME_KEY = booleanPreferencesKey("follow_system_theme")
    }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_DARK_THEME_KEY] ?: false
    }

    val useDynamicColor: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_DYNAMIC_COLOR_KEY] ?: true
    }

    val followSystemTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FOLLOW_SYSTEM_THEME_KEY] ?: true
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_THEME_KEY] = isDark
        }
    }

    suspend fun setDynamicColor(useDynamic: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_DYNAMIC_COLOR_KEY] = useDynamic
        }
    }

    suspend fun setFollowSystemTheme(followSystem: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FOLLOW_SYSTEM_THEME_KEY] = followSystem
        }
    }
}

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

data class ThemeSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = true,
    val isDarkTheme: Boolean = false
)
