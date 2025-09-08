package com.fxanhkhoa.what_to_eat_android.ui.localization

import android.content.Context
import androidx.compose.runtime.*
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
        val languageCode = preferences[LANGUAGE_KEY] ?: Language.ENGLISH.code
        Language.values().find { it.code == languageCode } ?: Language.ENGLISH
    }

    suspend fun setLanguage(language: Language) {
        context.localizationDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language.code
        }
    }

    fun getLocalizedString(key: String, language: Language = Language.ENGLISH): String {
        return when (language) {
            Language.ENGLISH -> getEnglishStrings()[key] ?: key
            Language.VIETNAMESE -> getVietnameseStrings()[key] ?: getEnglishStrings()[key] ?: key
        }
    }

    private fun getEnglishStrings(): Map<String, String> = mapOf(
        "settings" to "Settings",
        "appearance" to "Appearance",
        "theme" to "Theme",
        "select_theme" to "Select Theme",
        "light" to "Light",
        "dark" to "Dark",
        "system" to "System",
        "dark_mode" to "Dark Mode",
        "language" to "Language",
        "select_language" to "Select Language",
        "english" to "English",
        "vietnamese" to "Vietnamese",
        "about" to "About",
        "version" to "Version",
        "dynamic_colors" to "Dynamic Colors",
        "follow_system" to "Follow System",
        "use_material_you" to "Use Material You Colors"
    )

    private fun getVietnameseStrings(): Map<String, String> = mapOf(
        "settings" to "Cài Đặt",
        "appearance" to "Giao Diện",
        "theme" to "Chủ Đề",
        "select_theme" to "Chọn Chủ Đề",
        "light" to "Sáng",
        "dark" to "Tối",
        "system" to "Hệ Thống",
        "dark_mode" to "Chế Độ Tối",
        "language" to "Ngôn Ngữ",
        "select_language" to "Chọn Ngôn Ngữ",
        "english" to "Tiếng Anh",
        "vietnamese" to "Tiếng Việt",
        "about" to "Thông Tin",
        "version" to "Phiên Bản",
        "dynamic_colors" to "Màu Động",
        "follow_system" to "Theo Hệ Thống",
        "use_material_you" to "Sử Dụng Màu Material You"
    )
}
