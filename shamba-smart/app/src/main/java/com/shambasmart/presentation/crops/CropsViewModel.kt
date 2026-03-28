package com.shambasmart.presentation.crops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.dao.CropDao
import com.shambasmart.data.local.dao.HarvestDao
import com.shambasmart.data.local.dao.PlotDao
import com.shambasmart.data.local.entity.CropPlanting
import com.shambasmart.data.local.entity.HarvestRecord
import com.shambasmart.data.local.entity.Plot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CropsViewModel @Inject constructor(
    private val plotDao: PlotDao,
    private val harvestDao: HarvestDao,
    private val cropDao: CropDao
) : ViewModel() {

    val plots: StateFlow<List<Plot>> = plotDao.getAllPlots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlots: StateFlow<List<Plot>> = plots // Alias for backward compatibility

    val harvests: StateFlow<List<HarvestRecord>> = harvestDao.getAllHarvests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalAcres: StateFlow<Double?> = flow {
        emit(plotDao.getTotalAcres())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val underperformingPlots: StateFlow<List<Plot>> = plotDao.getUnderperformingPlots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addPlot(plot: Plot) {
        viewModelScope.launch {
            plotDao.insert(plot)
        }
    }

    fun updatePlot(plot: Plot) {
        viewModelScope.launch {
            plotDao.update(plot.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deletePlot(plot: Plot) {
        viewModelScope.launch {
            plotDao.delete(plot)
        }
    }

    // Digital Twin Benchmarking methods
    fun updatePerformanceIndex(plotId: Long, performanceIndex: Double) {
        viewModelScope.launch {
            plotDao.updatePerformanceIndex(plotId, performanceIndex)
        }
    }

    fun updateHealthScore(plotId: Long, healthScore: Double) {
        viewModelScope.launch {
            plotDao.updateHealthScore(plotId, healthScore)
        }
    }

    fun updateLastYield(plotId: Long, yieldKg: Double) {
        viewModelScope.launch {
            plotDao.updateLastYield(plotId, yieldKg)
        }
    }

    fun setBenchmarks(plotId: Long, baseline: Double, target: Double) {
        viewModelScope.launch {
            plotDao.setBenchmarks(plotId, baseline, target)
        }
    }

    fun updateBoundaryPoints(plotId: Long, boundaryPoints: String) {
        viewModelScope.launch {
            plotDao.updateBoundaryPoints(plotId, boundaryPoints)
        }
    }

    fun setSensorId(plotId: Long, sensorId: String) {
        viewModelScope.launch {
            plotDao.setSensorId(plotId, sensorId)
        }
    }

    // Crop Planting methods
    val allCropPlantings: StateFlow<List<CropPlanting>> = cropDao.getCropsByStatus("")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getCropPlantingsByPlot(plotId: Long): Flow<List<CropPlanting>> {
        return cropDao.getCropsByPlotId(plotId)
    }

    fun addCropPlanting(cropPlanting: CropPlanting) {
        viewModelScope.launch {
            cropDao.insert(cropPlanting)
        }
    }

    fun updateCropPlanting(cropPlanting: CropPlanting) {
        viewModelScope.launch {
            cropDao.update(cropPlanting.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteCropPlanting(cropPlanting: CropPlanting) {
        viewModelScope.launch {
            cropDao.delete(cropPlanting)
        }
    }

    // Harvest methods
    fun getHarvestRecordsByCropPlanting(cropPlantingId: Long): Flow<List<HarvestRecord>> {
        return harvestDao.getHarvestsByCropPlantingId(cropPlantingId)
    }

    fun addHarvest(harvest: HarvestRecord) {
        viewModelScope.launch {
            harvestDao.insert(harvest.copy(isSynced = false))
        }
    }

    fun updateHarvest(harvest: HarvestRecord) {
        viewModelScope.launch {
            harvestDao.update(harvest.copy(isSynced = false))
        }
    }

    fun deleteHarvest(harvest: HarvestRecord) {
        viewModelScope.launch {
            harvestDao.delete(harvest)
        }
    }
}
