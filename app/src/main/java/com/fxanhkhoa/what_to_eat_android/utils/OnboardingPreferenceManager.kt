package com.fxanhkhoa.what_to_eat_android.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding_prefs")

class OnboardingPreferenceManager(private val context: Context) {

    companion object {
        private val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
    }

    val hasSeenOnboarding: Flow<Boolean> = context.onboardingDataStore.data
        .map { prefs -> prefs[HAS_SEEN_ONBOARDING] ?: false }

    suspend fun markOnboardingComplete() {
        context.onboardingDataStore.edit { prefs ->
            prefs[HAS_SEEN_ONBOARDING] = true
        }
    }
}

