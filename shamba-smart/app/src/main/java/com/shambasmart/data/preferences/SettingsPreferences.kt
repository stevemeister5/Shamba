package com.shambasmart.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        private val USER_ROLE = stringPreferencesKey("user_role")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val FARM_NAME = stringPreferencesKey("farm_name")
        private val FARM_LOCATION = stringPreferencesKey("farm_location")
        private val FARM_SIZE = stringPreferencesKey("farm_size")
    }

    val selectedLanguage: Flow<String> = dataStore.data.map { preferences ->
        preferences[SELECTED_LANGUAGE] ?: "English"
    }

    val userRole: Flow<String> = dataStore.data.map { preferences ->
        preferences[USER_ROLE] ?: "Owner"
    }

    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED] ?: true
    }

    val farmName: Flow<String> = dataStore.data.map { preferences ->
        preferences[FARM_NAME] ?: "Shamba Smart Farm"
    }

    val farmLocation: Flow<String> = dataStore.data.map { preferences ->
        preferences[FARM_LOCATION] ?: "Korogwe, Tanga"
    }

    val farmSize: Flow<String> = dataStore.data.map { preferences ->
        preferences[FARM_SIZE] ?: "16 acres"
    }

    suspend fun updateLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[SELECTED_LANGUAGE] = language
        }
    }

    suspend fun updateUserRole(role: String) {
        dataStore.edit { preferences ->
            preferences[USER_ROLE] = role
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateFarmProfile(name: String, location: String, size: String) {
        dataStore.edit { preferences ->
            preferences[FARM_NAME] = name
            preferences[FARM_LOCATION] = location
            preferences[FARM_SIZE] = size
        }
    }
}