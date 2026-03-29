package com.shambasmart.demo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.shambasmart.data.local.ShambaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Hilt module for Demo Mode dependency injection.
 */
@Module
@InstallIn(SingletonComponent::class)
object DemoModeModule {

    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun provideDemoModeManager(
        @ApplicationContext context: Context,
        dataStore: DataStore<Preferences>,
        coroutineScope: CoroutineScope
    ): DemoModeManager = DemoModeManager(context, dataStore, coroutineScope)
}