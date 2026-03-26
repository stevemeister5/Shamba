package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.MilkProduction
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface MilkProductionDao {
    @Query("SELECT * FROM milk_production WHERE animalId = :animalId ORDER BY date DESC")
    fun getRecordsByAnimalId(animalId: Long): Flow<List<MilkProduction>>

    @Query("SELECT * FROM milk_production WHERE date = :date")
    suspend fun getRecordsByDate(date: LocalDate): List<MilkProduction>

    @Query("SELECT SUM(totalYield) FROM milk_production WHERE date = :date")
    suspend fun getTotalYieldByDate(date: LocalDate): Double?

    @Query("SELECT SUM(totalYield) FROM milk_production WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalYieldBetweenDates(startDate: LocalDate, endDate: LocalDate): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MilkProduction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<MilkProduction>)

    @Update
    suspend fun update(record: MilkProduction)

    @Delete
    suspend fun delete(record: MilkProduction)

    @Query("UPDATE milk_production SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM milk_production WHERE isSynced = 0")
    suspend fun getUnsyncedRecords(): List<MilkProduction>
}