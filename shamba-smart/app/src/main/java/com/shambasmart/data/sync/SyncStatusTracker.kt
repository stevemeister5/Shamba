package com.shambasmart.data.sync

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class SyncStatus(
    val lastSyncTimestamp: Long = 0,
    val lastSyncResult: String = "none", // "success", "failure", "none"
    val pendingSyncCount: Int = 0,
    val errorMessage: String? = null
)

@Singleton
class SyncStatusTracker @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
        private val LAST_SYNC_RESULT = stringPreferencesKey("last_sync_result")
        private val PENDING_SYNC_COUNT = longPreferencesKey("pending_sync_count")
        private val ERROR_MESSAGE = stringPreferencesKey("sync_error_message")
    }

    val syncStatus: Flow<SyncStatus> = dataStore.data.map { preferences ->
        SyncStatus(
            lastSyncTimestamp = preferences[LAST_SYNC_TIMESTAMP] ?: 0,
            lastSyncResult = preferences[LAST_SYNC_RESULT] ?: "none",
            pendingSyncCount = (preferences[PENDING_SYNC_COUNT] ?: 0).toInt(),
            errorMessage = preferences[ERROR_MESSAGE]
        )
    }

    suspend fun updateSyncSuccess() {
        dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIMESTAMP] = System.currentTimeMillis()
            preferences[LAST_SYNC_RESULT] = "success"
            preferences[ERROR_MESSAGE] = ""
        }
    }

    suspend fun updateSyncFailure(errorMessage: String) {
        dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIMESTAMP] = System.currentTimeMillis()
            preferences[LAST_SYNC_RESULT] = "failure"
            preferences[ERROR_MESSAGE] = errorMessage
        }
    }

    suspend fun updatePendingCount(count: Int) {
        dataStore.edit { preferences ->
            preferences[PENDING_SYNC_COUNT] = count.toLong()
        }
    }

    suspend fun clearSyncStatus() {
        dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIMESTAMP] = 0
            preferences[LAST_SYNC_RESULT] = "none"
            preferences[PENDING_SYNC_COUNT] = 0
            preferences[ERROR_MESSAGE] = ""
        }
    }
}