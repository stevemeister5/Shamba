package com.shambasmart.presentation.crops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.dao.HarvestDao
import com.shambasmart.data.local.dao.PlotDao
import com.shambasmart.data.local.entity.HarvestRecord
import com.shambasmart.data.local.entity.Plot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CropsViewModel @Inject constructor(
    private val plotDao: PlotDao,
    private val harvestDao: HarvestDao
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
}
