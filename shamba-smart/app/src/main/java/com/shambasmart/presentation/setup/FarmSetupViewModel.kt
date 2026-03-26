package com.shambasmart.presentation.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.dao.PlotDao
import com.shambasmart.data.local.dao.AnimalDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FarmSetupViewModel @Inject constructor(
    private val plotDao: PlotDao,
    private val animalDao: AnimalDao
) : ViewModel() {

    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _setupProgress = MutableStateFlow(0f)
    val setupProgress: StateFlow<Float> = _setupProgress.asStateFlow()

    private val _farmName = MutableStateFlow("")
    val farmName: StateFlow<String> = _farmName.asStateFlow()

    private val _farmLocation = MutableStateFlow("Korogwe, Tanga")
    val farmLocation: StateFlow<String> = _farmLocation.asStateFlow()

    private val _totalAcres = MutableStateFlow("16")
    val totalAcres: StateFlow<String> = _totalAcres.asStateFlow()

    private val _plannedBuildings = MutableStateFlow<List<PlannedBuilding>>(emptyList())
    val plannedBuildings: StateFlow<List<PlannedBuilding>> = _plannedBuildings.asStateFlow()

    private val _boundaryPoints = MutableStateFlow<List<GeoPoint>>(emptyList())
    val boundaryPoints: StateFlow<List<GeoPoint>> = _boundaryPoints.asStateFlow()

    private val _plots = MutableStateFlow<List<PlotSetup>>(emptyList())
    val plots: StateFlow<List<PlotSetup>> = _plots.asStateFlow()

    init {
        updateProgress()
    }

    fun setFarmName(name: String) {
        _farmName.value = name
    }

    fun setFarmLocation(location: String) {
        _farmLocation.value = location
    }

    fun setTotalAcres(acres: String) {
        _totalAcres.value = acres
    }

    fun nextStep() {
        if (_currentStep.value < 8) {
            _currentStep.value++
            updateProgress()
        }
    }

    fun previousStep() {
        if (_currentStep.value > 1) {
            _currentStep.value--
            updateProgress()
        }
    }

    fun addBuilding(building: PlannedBuilding) {
        _plannedBuildings.value = _plannedBuildings.value + building
    }

    fun removeBuilding(building: PlannedBuilding) {
        _plannedBuildings.value = _plannedBuildings.value - building
    }

    fun addBoundaryPoint(point: GeoPoint) {
        _boundaryPoints.value = _boundaryPoints.value + point
    }

    fun clearBoundaryPoints() {
        _boundaryPoints.value = emptyList()
    }

    fun addPlot(plot: PlotSetup) {
        _plots.value = _plots.value + plot
    }

    fun removePlot(plot: PlotSetup) {
        _plots.value = _plots.value - plot
    }

    fun completeSetup() {
        viewModelScope.launch {
            // Save farm configuration
            // Create plots in database
            // Create initial tasks
            // Navigate to dashboard
        }
    }

    private fun updateProgress() {
        _setupProgress.value = _currentStep.value / 8f
    }
}

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

data class PlotSetup(
    val name: String,
    val sizeAcres: Double,
    val cropType: String? = null,
    val soilType: String? = null
)