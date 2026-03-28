package com.shambasmart.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val PERMISSIONS_GRANTED = booleanPreferencesKey("permissions_granted")
    }

    val isOnboardingCompleted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    val arePermissionsGranted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PERMISSIONS_GRANTED] ?: false
    }

    suspend fun completeOnboarding() {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun setPermissionsGranted(granted: Boolean) {
        dataStore.edit { preferences ->
            preferences[PERMISSIONS_GRANTED] = granted
        }
    }

    suspend fun resetOnboarding() {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = false
            preferences[PERMISSIONS_GRANTED] = false
        }
    }
}