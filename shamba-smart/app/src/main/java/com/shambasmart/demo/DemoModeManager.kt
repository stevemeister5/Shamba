package com.shambasmart.demo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.room.Room
import com.shambasmart.data.local.ShambaDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoModeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val IS_DEMO_MODE = booleanPreferencesKey("is_demo_mode")
    }

    val isDemoMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_DEMO_MODE] ?: false
    }

    private var demoDatabase: ShambaDatabase? = null

    fun createDemoDatabase(): ShambaDatabase {
        if (demoDatabase == null) {
            demoDatabase = Room.inMemoryDatabaseBuilder(
                context,
                ShambaDatabase::class.java
            ).build()
        }
        return demoDatabase!!
    }

    suspend fun launchDemo() {
        dataStore.edit { preferences ->
            preferences[IS_DEMO_MODE] = true
        }
    }

    suspend fun exitDemo() {
        dataStore.edit { preferences ->
            preferences[IS_DEMO_MODE] = false
        }
        demoDatabase?.close()
        demoDatabase = null
    }

    fun getDemoDatabase(): ShambaDatabase? = demoDatabase
}