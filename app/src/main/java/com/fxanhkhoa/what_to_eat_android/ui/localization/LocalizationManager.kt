package com.fxanhkhoa.what_to_eat_android.ui.localization

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.*
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

val Context.localizationDataStore: DataStore<Preferences> by preferencesDataStore(name = "localization_preferences")

class LocalizationManager(private val context: Context) {

    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("selected_language")
    }

    val currentLanguage: Flow<Language> = context.localizationDataStore.data.map { preferences ->
        val savedCode = preferences[LANGUAGE_KEY]
        if (savedCode != null) {
            // Use the explicitly saved preference
            Language.entries.find { it.code == savedCode } ?: Language.ENGLISH
        } else {
            // No preference saved yet — match against the device locale, fallback to English
            val deviceLanguage = Locale.getDefault().language          // e.g. "vi", "en", "fr"
            Language.entries.find { it.code == deviceLanguage } ?: Language.ENGLISH
        }
    }

    suspend fun setLanguage(language: Language) {
        // Save language preference
        context.localizationDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language.code
        }

        // Apply the language change immediately to the app
        val locale = Locale.forLanguageTag(language.code)
        val localeList = LocaleListCompat.create(locale)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    // Get localized string based on current language - for immediate updates
    fun getString(stringResId: Int, language: Language): String {
        val locale = Locale.forLanguageTag(language.code)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config).getString(stringResId)
    }
}
