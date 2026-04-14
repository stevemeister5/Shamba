package com.shambasmart.demo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.room.Room
import com.shambasmart.data.local.ShambaDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the lifecycle of Demo Mode.
 * 
 * When demo mode is active:
 * - An in-memory Room database is created and seeded with demo data
 * - All ViewModels use the demo repositories
 * - A persistent banner is shown on every screen
 * 
 * When demo mode is exited:
 * - The in-memory database is closed (all data destroyed)
 * - The app returns to the launch choice screen
 */
@Singleton
class DemoModeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val coroutineScope: CoroutineScope
) {
    private val _isDemoMode = MutableStateFlow(false)
    val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    private var demoDatabase: ShambaDatabase? = null
    
    val demoDatabaseFlow: Flow<ShambaDatabase?> = _isDemoMode.map { active ->
        if (active) demoDatabase else null
    }

    companion object {
        val DEMO_MODE_KEY = booleanPreferencesKey("demo_mode_active")
        
        fun fromAppContext(context: Context): DemoModeManager {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                DemoModeEntryPoint::class.java
            )
            return entryPoint.demoModeManager()
        }
        
        @EntryPoint
        @InstallIn(SingletonComponent::class)
        interface DemoModeEntryPoint {
            fun demoModeManager(): DemoModeManager
        }
    }

    /**
     * Initialize demo mode state from DataStore on app startup.
     * Should be called in the Application class.
     */
    fun initialize() {
        coroutineScope.launch(Dispatchers.IO) {
            val preferences = dataStore.data.first()
            val isActive = preferences[DEMO_MODE_KEY] ?: false
            _isDemoMode.value = isActive
            
            // If demo mode was active when app was killed,
            // we need to recreate the in-memory database
            if (isActive) {
                createDemoDatabase()
            }
        }
    }

    /**
     * Launch demo mode — creates in-memory database and seeds it.
     */
    fun launchDemo(onComplete: () -> Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            // Mark demo mode as active in DataStore
            dataStore.edit { prefs ->
                prefs[DEMO_MODE_KEY] = true
            }
            
            // Create and seed the in-memory database
            createDemoDatabase()
            
            // Update state
            _isDemoMode.value = true
            
            // Navigate to dashboard
            launch(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    /**
     * Exit demo mode — closes the in-memory database.
     * All demo data is destroyed.
     */
    fun exitDemo(onComplete: () -> Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            // Mark demo mode as inactive in DataStore
            dataStore.edit { prefs ->
                prefs[DEMO_MODE_KEY] = false
            }
            
            // Close the in-memory database
            demoDatabase?.close()
            demoDatabase = null
            
            // Update state
            _isDemoMode.value = false
            
            // Navigate to launch choice
            launch(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    /**
     * Get the demo database instance if demo mode is active.
     */
    fun getDemoDatabase(): ShambaDatabase? = demoDatabase

    /**
     * Create a fresh in-memory database and seed it with demo data.
     */
    private suspend fun createDemoDatabase() {
        // Close any existing demo database
        demoDatabase?.close()
        
        // Create new in-memory database
        demoDatabase = Room.inMemoryDatabaseBuilder(
            context,
            ShambaDatabase::class.java
        ).build()
        
        // Seed with demo data
        demoDatabase?.let { db ->
            DemoDataSeeder.seedAll(db)
        }
    }
}