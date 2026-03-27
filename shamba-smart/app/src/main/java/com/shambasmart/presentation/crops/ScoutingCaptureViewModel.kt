package com.shambasmart.presentation.crops

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.dao.PlotDao
import com.shambasmart.data.local.entity.InferenceResult
import com.shambasmart.data.local.entity.ScoutingReport
import com.shambasmart.data.local.entity.SeverityLevel
import com.shambasmart.data.repository.ScoutingRepository
import com.shambasmart.ml.PestClassifier
import com.shambasmart.maarifa.retrieval.PestKnowledgeMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScoutingCaptureUiState(
    val isLoading: Boolean = false,
    val capturedBitmap: Bitmap? = null,
    val inferenceResult: InferenceResult? = null,
    val selectedDetectionIndex: Int = 0,
    val plotId: Long? = null,
    val plotName: String = "",
    val gpsLatitude: Double = 0.0,
    val gpsLongitude: Double = 0.0,
    val notes: String = "",
    val isSaving: Boolean = false,
    val savedReportId: Long? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ScoutingCaptureViewModel @Inject constructor(
    private val pestClassifier: PestClassifier,
    private val scoutingRepository: ScoutingRepository,
    private val plotDao: PlotDao,
    private val pestKnowledgeMapper: PestKnowledgeMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScoutingCaptureUiState())
    val uiState: StateFlow<ScoutingCaptureUiState> = _uiState.asStateFlow()

    fun setCapturedImage(bitmap: Bitmap, latitude: Double, longitude: Double) {
        _uiState.update { it.copy(
            capturedBitmap = bitmap,
            gpsLatitude = latitude,
            gpsLongitude = longitude
        ) }
        
        // Auto-detect plot from GPS coordinates
        detectPlot(latitude, longitude)
        
        // Run inference
        runInference(bitmap)
    }

    private fun detectPlot(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                val plots = plotDao.getAllPlotsSync()
                val nearestPlot = plots.minByOrNull { plot ->
                    val plotLat = plot.latitude ?: 0.0
                    val plotLng = plot.longitude ?: 0.0
                    val distance = Math.sqrt(
                        Math.pow(latitude - plotLat, 2.0) + Math.pow(longitude - plotLng, 2.0)
                    )
                    distance
                }
                
                nearestPlot?.let { plot ->
                    _uiState.update { it.copy(
                        plotId = plot.id,
                        plotName = plot.name
                    ) }
                }
            } catch (e: Exception) {
                // Could not detect plot, user will select manually
            }
        }
    }

    private fun runInference(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = pestClassifier.classify(bitmap)
                _uiState.update { it.copy(
                    isLoading = false,
                    inferenceResult = result,
                    selectedDetectionIndex = if (result.detections.isNotEmpty()) 0 else -1
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Inference failed: ${e.message}"
                ) }
            }
        }
    }

    fun selectDetection(index: Int) {
        _uiState.update { it.copy(selectedDetectionIndex = index) }
    }

    fun setPlotId(plotId: Long) {
        _uiState.update { it.copy(plotId = plotId) }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun saveReport() {
        val state = _uiState.value
        val result = state.inferenceResult
        val detection = result?.detections?.getOrNull(state.selectedDetectionIndex)
        
        if (state.plotId == null) {
            _uiState.update { it.copy(errorMessage = "Please select a plot") }
            return
        }
        
        if (detection == null) {
            _uiState.update { it.copy(errorMessage = "No pest detected") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                val report = ScoutingReport(
                    plotId = state.plotId!!,
                    pestType = detection.pestClass,
                    severityScore = detection.severityLevel.score,
                    gpsLatitude = state.gpsLatitude,
                    gpsLongitude = state.gpsLongitude,
                    notes = state.notes.ifBlank { null },
                    detectedAt = System.currentTimeMillis()
                )
                
                val reportId = scoutingRepository.insertReport(report)
                
                _uiState.update { it.copy(
                    isSaving = false,
                    savedReportId = reportId,
                    successMessage = "Scouting report saved successfully"
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isSaving = false,
                    errorMessage = "Failed to save report: ${e.message}"
                ) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun reset() {
        _uiState.value = ScoutingCaptureUiState()
    }
}