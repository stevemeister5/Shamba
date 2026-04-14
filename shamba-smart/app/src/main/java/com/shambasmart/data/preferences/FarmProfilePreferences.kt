package com.shambasmart.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class FarmProfile(
    val name: String = "",
    val ownerName: String = "",
    val ownerPhone: String = "",
    val sizeAcres: Double = 16.0,
    val location: String = "Korogwe, Tanga",
    val latitude: Double = -5.15,
    val longitude: Double = 38.48
)

@Singleton
class FarmProfilePreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val FARM_NAME = stringPreferencesKey("farm_name")
        private val OWNER_NAME = stringPreferencesKey("owner_name")
        private val OWNER_PHONE = stringPreferencesKey("owner_phone")
        private val SIZE_ACRES = doublePreferencesKey("size_acres")
        private val LOCATION = stringPreferencesKey("location")
        private val LATITUDE = doublePreferencesKey("latitude")
        private val LONGITUDE = doublePreferencesKey("longitude")
    }

    val farmName: Flow<String> = dataStore.data.map { preferences ->
        preferences[FARM_NAME] ?: ""
    }

    val farmProfile: Flow<FarmProfile> = dataStore.data.map { preferences ->
        FarmProfile(
            name = preferences[FARM_NAME] ?: "",
            ownerName = preferences[OWNER_NAME] ?: "",
            ownerPhone = preferences[OWNER_PHONE] ?: "",
            sizeAcres = preferences[SIZE_ACRES] ?: 16.0,
            location = preferences[LOCATION] ?: "Korogwe, Tanga",
            latitude = preferences[LATITUDE] ?: -5.15,
            longitude = preferences[LONGITUDE] ?: 38.48
        )
    }

    suspend fun saveFarmProfile(profile: FarmProfile) {
        dataStore.edit { preferences ->
            preferences[FARM_NAME] = profile.name
            preferences[OWNER_NAME] = profile.ownerName
            preferences[OWNER_PHONE] = profile.ownerPhone
            preferences[SIZE_ACRES] = profile.sizeAcres
            preferences[LOCATION] = profile.location
            preferences[LATITUDE] = profile.latitude
            preferences[LONGITUDE] = profile.longitude
        }
    }

    suspend fun clearFarmProfile() {
        dataStore.edit { preferences ->
            preferences.remove(FARM_NAME)
            preferences.remove(OWNER_NAME)
            preferences.remove(OWNER_PHONE)
            preferences.remove(SIZE_ACRES)
            preferences.remove(LOCATION)
            preferences.remove(LATITUDE)
            preferences.remove(LONGITUDE)
        }
    }
}