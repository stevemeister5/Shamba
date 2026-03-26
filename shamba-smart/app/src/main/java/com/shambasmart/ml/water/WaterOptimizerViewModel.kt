package com.shambasmart.ml.water

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.*

@HiltViewModel
class WaterOptimizerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(WaterUiState())
    val uiState: StateFlow<WaterUiState> = _uiState.asStateFlow()

    fun calculateET0(
        maxTemp: Double,
        minTemp: Double,
        windLevel: String,
        soilMoisture: Double
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCalculating = true) }

            // Simplified Penman-Monteith ET0 calculation
            val avgTemp = (maxTemp + minTemp) / 2
            val windSpeed = when (windLevel) {
                "low" -> 1.0
                "medium" -> 2.5
                "high" -> 4.0
                else -> 2.5
            }

            // Simplified ET0 calculation (mm/day)
            // In production, use full FAO-56 Penman-Monteith equation
            val et0 = 0.0023 * (avgTemp + 17.8) * sqrt(maxTemp - minTemp) * 2.45
            
            // Adjust for wind
            val windFactor = 1.0 + (windSpeed * 0.1)
            val adjustedET0 = et0 * windFactor

            // Calculate water lost from soil
            val waterLost = adjustedET0 // mm/day

            // Check if irrigation needed (soil moisture < 40%)
            val shouldIrrigate = soilMoisture < 40.0
            val irrigationNeeded = if (shouldIrrigate) {
                (40.0 - soilMoisture) * 2.5 // Convert % to mm
            } else 0.0

            _uiState.update {
                it.copy(
                    isCalculating = false,
                    et0 = adjustedET0,
                    waterLost = waterLost,
                    shouldIrrigate = shouldIrrigate,
                    irrigationNeeded = irrigationNeeded
                )
            }
        }
    }
}

data class WaterUiState(
    val isCalculating: Boolean = false,
    val et0: Double = 0.0,
    val waterLost: Double = 0.0,
    val shouldIrrigate: Boolean = false,
    val irrigationNeeded: Double = 0.0
)