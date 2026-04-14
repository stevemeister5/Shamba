package com.shambasmart

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.shambasmart.demo.DemoModeManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Main application class for Shamba Smart.
 * Initializes Hilt for dependency injection and configures WorkManager.
 */
@HiltAndroidApp
class ShambaSmartApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var demoModeManager: DemoModeManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize demo mode state from DataStore
        // This restores the in-memory database if demo mode was active when app was killed
        demoModeManager.initialize()
    }

    companion object {
        lateinit var instance: ShambaSmartApplication
            private set
    }
}
