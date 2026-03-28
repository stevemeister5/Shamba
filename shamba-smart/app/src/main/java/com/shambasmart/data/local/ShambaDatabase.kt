package com.shambasmart.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shambasmart.data.local.dao.*
import com.shambasmart.data.local.dao.maarifa.KnowledgeChunkDao
import com.shambasmart.data.local.dao.maarifa.OperationalRuleDao
import com.shambasmart.data.local.entity.*
import com.shambasmart.data.local.entity.maarifa.KnowledgeChunk
import com.shambasmart.data.local.entity.maarifa.OperationalRule
import com.shambasmart.data.local.converter.Converters
import com.shambasmart.data.local.view.DashboardView
import com.shambasmart.data.local.view.PlotAnalyticsView
import com.shambasmart.data.local.view.LivestockDashboardView

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
        MaintenanceTask::class,
        // Maarifa Knowledge Engine entities
        KnowledgeChunk::class,
        OperationalRule::class,
        // GPS Boundary entities
        FarmBoundary::class,
        BoundaryPointEntity::class,
        // Map entities
        MapMarkerEntity::class,
        MapLayerEntity::class,
        MapTileCacheEntity::class,
        // Pest Scouting entities
        ScoutingReport::class,
        // Dashboard KPI entities
        DashboardView::class,
        PlotAnalyticsView::class,
        LivestockDashboardView::class
    ],
    version = 10,
    exportSchema = false
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
    abstract fun loanDao(): LoanDao
    abstract fun workerDao(): WorkerDao
    abstract fun taskDao(): TaskDao
    abstract fun calendarDao(): CalendarDao
    abstract fun syncDao(): SyncDao
    abstract fun audioEventDao(): AudioEventDao
    abstract fun maintenanceTaskDao(): MaintenanceTaskDao
    // Maarifa DAOs
    abstract fun knowledgeChunkDao(): KnowledgeChunkDao
    abstract fun operationalRuleDao(): OperationalRuleDao
    // GPS Boundary DAO
    abstract fun boundaryDao(): BoundaryDao
    // Map DAOs
    abstract fun mapMarkerDao(): MapMarkerDao
    abstract fun mapLayerDao(): MapLayerDao
    abstract fun mapTileCacheDao(): MapTileCacheDao
    // Scouting DAO
    abstract fun scoutingReportDao(): ScoutingReportDao
    // Dashboard View DAO
    abstract fun dashboardViewDao(): DashboardViewDao
}
