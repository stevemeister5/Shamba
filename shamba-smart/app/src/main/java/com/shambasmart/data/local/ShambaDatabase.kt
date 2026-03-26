package com.shambasmart.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shambasmart.data.local.dao.*
import com.shambasmart.data.local.entity.*
import com.shambasmart.data.local.converter.Converters

@Database(
    entities = [
        Animal::class,
        HealthRecord::class,
        ReproductionRecord::class,
        MilkProduction::class,
        WeightEntry::class,
        Plot::class,
        CropPlanting::class,
        HarvestRecord::class,
        SilageInventory::class,
        WeatherLog::class,
        MilkCollection::class,
        CheeseBatch::class,
        FeedInventory::class,
        StoreItem::class,
        Income::class,
        Expense::class,
        Loan::class,
        Worker::class,
        AttendanceRecord::class,
        Task::class,
        CalendarEvent::class,
        SyncStatus::class,
        AudioEvent::class,
        MaintenanceTask::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ShambaDatabase : RoomDatabase() {
    abstract fun animalDao(): AnimalDao
    abstract fun healthRecordDao(): HealthRecordDao
    abstract fun reproductionDao(): ReproductionDao
    abstract fun milkProductionDao(): MilkProductionDao
    abstract fun plotDao(): PlotDao
    abstract fun cropDao(): CropDao
    abstract fun harvestDao(): HarvestDao
    abstract fun silageDao(): SilageDao
    abstract fun weatherDao(): WeatherDao
    abstract fun cheeseDao(): CheeseDao
    abstract fun feedDao(): FeedDao
    abstract fun storeDao(): StoreDao
    abstract fun financialDao(): FinancialDao
    abstract fun workerDao(): WorkerDao
    abstract fun taskDao(): TaskDao
    abstract fun calendarDao(): CalendarDao
    abstract fun syncDao(): SyncDao
    abstract fun audioEventDao(): AudioEventDao
    abstract fun maintenanceTaskDao(): MaintenanceTaskDao
}
