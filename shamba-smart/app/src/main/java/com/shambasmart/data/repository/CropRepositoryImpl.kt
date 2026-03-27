package com.shambasmart.data.repository

import com.shambasmart.data.local.dao.CropDao
import com.shambasmart.data.local.entity.CropPlanting
import com.shambasmart.domain.repository.CropRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CropRepositoryImpl @Inject constructor(
    private val cropDao: CropDao
) : CropRepository {

    override fun getAllCrops(): Flow<List<CropPlanting>> = cropDao.getCropsByStatus("growing")

    override fun getActiveCrops(): Flow<List<CropPlanting>> = cropDao.getCropsByStatus("growing")

    override fun getCropsByStatus(status: String): Flow<List<CropPlanting>> = cropDao.getCropsByStatus(status)

    override fun getCropsByPlot(plotId: Long): Flow<List<CropPlanting>> = cropDao.getCropsByPlotId(plotId)

    override suspend fun getCropById(id: Long): CropPlanting? = cropDao.getCropById(id)

    override fun getCropCount(): Flow<Int> {
        // Return active crop count as a Flow
        return kotlinx.coroutines.flow.flow {
            emit(0) // Placeholder - would need proper aggregation query
        }
    }

    override suspend fun insertCrop(crop: CropPlanting): Long {
        val now = System.currentTimeMillis()
        return cropDao.insert(crop.copy(createdAt = now, updatedAt = now, isSynced = false))
    }

    override suspend fun updateCrop(crop: CropPlanting) {
        cropDao.update(crop.copy(updatedAt = System.currentTimeMillis(), isSynced = false))
    }

    override suspend fun deleteCrop(crop: CropPlanting) = cropDao.delete(crop)

    override suspend fun updateStatus(id: Long, status: String) {
        cropDao.updateStatus(id, status, System.currentTimeMillis())
    }

    override suspend fun getUnsyncedCrops(): List<CropPlanting> = cropDao.getUnsyncedCrops()

    override suspend fun updateSyncStatus(id: Long, synced: Boolean) {
        cropDao.updateSyncStatus(id, synced)
    }
}