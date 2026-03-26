package com.shambasmart.data.local.dao

import androidx.room.*
import com.shambasmart.data.local.entity.Animal
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimalDao {
    @Query("SELECT * FROM animals WHERE status = 'active' ORDER BY tagId ASC")
    fun getAllActiveAnimals(): Flow<List<Animal>>

    @Query("SELECT * FROM animals WHERE id = :id")
    suspend fun getAnimalById(id: Long): Animal?

    @Query("SELECT * FROM animals WHERE tagId = :tagId")
    suspend fun getAnimalByTagId(tagId: String): Animal?

    @Query("SELECT * FROM animals WHERE species = :species AND status = 'active'")
    fun getAnimalsBySpecies(species: String): Flow<List<Animal>>

    @Query("SELECT COUNT(*) FROM animals WHERE status = 'active'")
    fun getActiveAnimalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM animals WHERE species = :species AND status = 'active'")
    fun getCountBySpecies(species: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: Animal): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimals(animals: List<Animal>)

    @Update
    suspend fun updateAnimal(animal: Animal)

    @Delete
    suspend fun deleteAnimal(animal: Animal)

    @Query("UPDATE animals SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE animals SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM animals WHERE isSynced = 0")
    suspend fun getUnsyncedAnimals(): List<Animal>
}