package com.shambasmart.di

import android.content.Context
import androidx.work.*
import com.shambasmart.data.sync.SyncManager
import com.shambasmart.data.sync.SyncWorker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Sync module - provides connectivity-triggered sync instead of periodic timers.
 * Sync is triggered when network connectivity is restored.
 */
@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
    
    // Periodic sync removed - using connectivity-triggered sync in SyncManager
    // SyncManager.registerConnectivitySync() handles sync on network restoration
}
