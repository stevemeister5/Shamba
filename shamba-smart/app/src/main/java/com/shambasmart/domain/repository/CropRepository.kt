package com.shambasmart.domain.repository

import com.shambasmart.data.local.entity.CropPlanting
import kotlinx.coroutines.flow.Flow

interface CropRepository {
    fun getAllCrops(): Flow<List<CropPlanting>>
    fun getActiveCrops(): Flow<List<CropPlanting>>
    fun getCropsByStatus(status: String): Flow<List<CropPlanting>>
    fun getCropsByPlot(plotId: Long): Flow<List<CropPlanting>>
    suspend fun getCropById(id: Long): CropPlanting?
    fun getCropCount(): Flow<Int>
    suspend fun insertCrop(crop: CropPlanting): Long
    suspend fun updateCrop(crop: CropPlanting)
    suspend fun deleteCrop(crop: CropPlanting)
    suspend fun updateStatus(id: Long, status: String)
    suspend fun getUnsyncedCrops(): List<CropPlanting>
    suspend fun updateSyncStatus(id: Long, synced: Boolean)
}