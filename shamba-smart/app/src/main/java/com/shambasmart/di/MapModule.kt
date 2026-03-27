package com.shambasmart.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.shambasmart.data.local.dao.MapLayerDao
import com.shambasmart.data.local.dao.MapMarkerDao
import com.shambasmart.data.local.dao.MapTileCacheDao
import com.shambasmart.data.local.ShambaDatabase
import com.shambasmart.map.integration.MapAutoUpdater
import com.shambasmart.map.offline.OfflineMapManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapModule {

    @Provides
    @Singleton
    fun provideMapMarkerDao(database: ShambaDatabase): MapMarkerDao {
        return database.mapMarkerDao()
    }

    @Provides
    @Singleton
    fun provideMapLayerDao(database: ShambaDatabase): MapLayerDao {
        return database.mapLayerDao()
    }

    @Provides
    @Singleton
    fun provideMapTileCacheDao(database: ShambaDatabase): MapTileCacheDao {
        return database.mapTileCacheDao()
    }

    @Provides
    @Singleton
    fun provideOfflineMapManager(
        @ApplicationContext context: Context
    ): OfflineMapManager {
        return OfflineMapManager(context)
    }

    @Provides
    @Singleton
    fun provideMapAutoUpdater(
        mapMarkerDao: MapMarkerDao
    ): MapAutoUpdater {
        return MapAutoUpdater(mapMarkerDao)
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }
}
