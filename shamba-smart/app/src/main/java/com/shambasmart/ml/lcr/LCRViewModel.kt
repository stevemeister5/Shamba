package com.shambasmart.ml.lcr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LCRViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LCRUiState())
    val uiState: StateFlow<LCRUiState> = _uiState.asStateFlow()

    fun calculateOptimalRation(
        silageKg: Double,
        napierKg: Double,
        concentrateKg: Double,
        silageCost: Double,
        napierCost: Double,
        concentrateCost: Double,
        targetDM: Double,
        targetCP: Double
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCalculating = true) }

            // Simplified Least-Cost Ration calculation
            // In production, this would use Linear Programming (Simplex method)
            val silageDM = silageKg * 0.35 // 35% dry matter
            val napierDM = napierKg * 0.25 // 25% dry matter
            val concentrateDM = concentrateKg * 0.90 // 90% dry matter
            
            val totalDM = silageDM + napierDM + concentrateDM
            
            val silageCP = silageDM * 0.08 // 8% crude protein
            val napierCP = napierDM * 0.10 // 10% crude protein
            val concentrateCP = concentrateDM * 0.20 // 20% crude protein
            
            val totalCP = silageCP + napierCP + concentrateCP
            
            val totalCost = (silageKg * silageCost) + (napierKg * napierCost) + (concentrateKg * concentrateCost)
            
            _uiState.update {
                it.copy(
                    isCalculating = false,
                    totalDM = totalDM,
                    totalCP = totalCP,
                    totalCost = totalCost,
                    costPerKgDM = if (totalDM > 0) totalCost / totalDM else 0.0,
                    meetsRequirements = totalDM >= targetDM && totalCP >= targetCP,
                    recommendation = generateRecommendation(totalDM, targetDM, totalCP, targetCP)
                )
            }
        }
    }

    private fun generateRecommendation(actualDM: Double, targetDM: Double, actualCP: Double, targetCP: Double): String {
        return when {
            actualDM < targetDM -> "Increase feed quantity to meet dry matter requirements"
            actualCP < targetCP -> "Add more concentrate to increase protein content"
            else -> "Ration meets requirements. Good balance!"
        }
    }
}

data class LCRUiState(
    val isCalculating: Boolean = false,
    val totalDM: Double = 0.0,
    val totalCP: Double = 0.0,
    val totalCost: Double = 0.0,
    val costPerKgDM: Double = 0.0,
    val meetsRequirements: Boolean = false,
    val recommendation: String = ""
)