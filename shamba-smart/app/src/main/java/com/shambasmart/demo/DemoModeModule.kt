package com.shambasmart.demo

import android.content.Context
import androidx.room.Room
import com.shambasmart.data.local.ShambaDatabase
import com.shambasmart.data.local.dao.*
import com.shambasmart.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DemoMode

@Module
@InstallIn(SingletonComponent::class)
object DemoModeModule {

    @Provides
    @Singleton
    @DemoMode
    fun provideDemoDatabase(@ApplicationContext context: Context): ShambaDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            ShambaDatabase::class.java
        ).build()
    }

    @Provides
    @Singleton
    @DemoMode
    fun provideDemoAnimalRepository(
        @DemoMode database: ShambaDatabase
    ): AnimalRepository = AnimalRepositoryImpl(database.animalDao())

    @Provides
    @Singleton
    @DemoMode
    fun provideDemoCropRepository(
        @DemoMode database: ShambaDatabase
    ): CropRepository = CropRepositoryImpl(database.cropDao())

    @Provides
    @Singleton
    @DemoMode
    fun provideDemoCheeseRepository(
        @DemoMode database: ShambaDatabase
    ): CheeseRepository = CheeseRepositoryImpl(database.cheeseDao())

    @Provides
    @Singleton
    @DemoMode
    fun provideDemoFeedRepository(
        @DemoMode database: ShambaDatabase
    ): FeedRepository = FeedRepositoryImpl(database.feedDao())

    @Provides
    @Singleton
    @DemoMode
    fun provideDemoFinanceRepository(
        @DemoMode database: ShambaDatabase
    ): FinanceRepository = FinanceRepositoryImpl(database.financeDao())

    @Provides
    @Singleton
    @DemoMode
    fun provideDemoLabourRepository(
        @DemoMode database: ShambaDatabase
    ): LabourRepository = LabourRepositoryImpl(database.labourDao())

    @Provides
    @Singleton
    @DemoMode
    fun provideDemoCalendarRepository(
        @DemoMode database: ShambaDatabase
    ): CalendarRepository = CalendarRepositoryImpl(database.calendarDao())

    @Provides
    @Singleton
    @DemoMode
    fun provideDemoWeatherRepository(
        @DemoMode database: ShambaDatabase
    ): WeatherRepository = WeatherRepositoryImpl(database.weatherDao())

    @Provides
    @Singleton
    @DemoMode
    fun provideDemoMaintenanceRepository(
        @DemoMode database: ShambaDatabase
    ): MaintenanceRepository = MaintenanceRepositoryImpl(database.maintenanceDao())

    @Provides
    @Singleton
    @DemoMode
    fun provideDemoMaarifaRepository(
        @DemoMode database: ShambaDatabase
    ): MaarifaRepository = MaarifaRepositoryImpl(database.maarifaDao())

    @Provides
    @Singleton
    @DemoMode
    fun provideDemoAlertRepository(
        @DemoMode database: ShambaDatabase
    ): AlertRepository = AlertRepositoryImpl(database.alertDao())

    @Provides
    @Singleton
    @DemoMode
    fun provideDemoMapRepository(
        @DemoMode database: ShambaDatabase
    ): MapRepository = MapRepositoryImpl(database.mapDao())
}