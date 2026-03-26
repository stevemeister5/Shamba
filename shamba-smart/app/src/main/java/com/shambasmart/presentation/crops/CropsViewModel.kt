package com.shambasmart.presentation.crops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.dao.PlotDao
import com.shambasmart.data.local.entity.Plot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CropsViewModel @Inject constructor(
    private val plotDao: PlotDao
) : ViewModel() {

    val allPlots: StateFlow<List<Plot>> = plotDao.getAllPlots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalAcres: StateFlow<Double?> = flow {
        emit(plotDao.getTotalAcres())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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
}