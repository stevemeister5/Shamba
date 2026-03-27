package com.shambasmart.ml.vision

import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.dao.CropDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@HiltViewModel
class VisionGradingViewModel @Inject constructor(
    private val cameraManager: CameraManager,
    private val colorimetricGrader: ColorimetricGrader,
    private val cropDao: CropDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisionUiState())
    val uiState: StateFlow<VisionUiState> = _uiState.asStateFlow()

    // Map UI product types to grader product types
    private val productTypeMap = mapOf(
        ProductType.TOMATO to ColorimetricGrader.ProductType.TOMATO,
        ProductType.MAIZE to ColorimetricGrader.ProductType.MAIZE,
        ProductType.BEANS to ColorimetricGrader.ProductType.BEANS,
        ProductType.KALE to ColorimetricGrader.ProductType.KALE,
        ProductType.ONION to ColorimetricGrader.ProductType.ONION,
        ProductType.CHEESE_FRESH to ColorimetricGrader.ProductType.CHEESE_FRESH,
        ProductType.CHEESE_AGED to ColorimetricGrader.ProductType.CHEESE_AGED
    )

    fun setProductType(type: ProductType) {
        _uiState.update { it.copy(selectedProduct = type) }
    }

    fun setupCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCameraReady = false, cameraError = null) }

            val result = cameraManager.setupCamera(
                lifecycleOwner = lifecycleOwner,
                previewView = previewView
            )

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isCameraReady = true) }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(cameraError = "Camera setup failed: ${error.message}") 
                    }
                }
            )
        }
    }

    fun captureAndAnalyze() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true) }

            val productType = uiState.value.selectedProduct
            val graderProductType = productTypeMap[productType]
                ?: ColorimetricGrader.ProductType.TOMATO

            // Check if this product uses HSV or time-based grading
            val usesHSV = productType in listOf(
                ProductType.TOMATO, ProductType.ONION, ProductType.KALE,
                ProductType.CHEESE_FRESH, ProductType.CHEESE_AGED
            )

            if (usesHSV) {
                // HSV-based grading - capture image from camera
                val captureResult = cameraManager.captureImage()

                captureResult.fold(
                    onSuccess = { bitmap ->
                        val result = colorimetricGrader.gradeByHSV(bitmap, graderProductType)
                        updateStateFromResult(result)
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isAnalyzing = false,
                                cameraError = "Image capture failed: ${error.message}"
                            )
                        }
                    }
                )
            } else {
                // Time-based grading - calculate from planting date
                val plantingDate = fetchPlantingDate(productType)
                if (plantingDate != null) {
                    val result = colorimetricGrader.gradeByTime(graderProductType, plantingDate)
                    updateStateFromResult(result)
                } else {
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            analysis = "No planting date found for ${productType.name.replace("_", " ")}. Please log a planting record first."
                        )
                    }
                }
            }
        }
    }

    private fun updateStateFromResult(result: ColorimetricGrader.GradingResult) {
        _uiState.update {
            it.copy(
                isAnalyzing = false,
                hue = result.hsvValues?.hue ?: 0.0,
                saturation = result.hsvValues?.saturation ?: 0.0,
                value = result.hsvValues?.value ?: 0.0,
                maturityScore = result.maturityScore,
                grade = result.grade,
                harvestStatus = result.harvestStatus,
                isInHarvestWindow = result.isInHarvestWindow,
                daysUntilHarvest = result.daysUntilHarvest,
                analysis = result.analysis,
                premiumMultiplier = result.premiumMultiplier,
                gradingMethod = result.gradingMethod.name
            )
        }
    }

    private suspend fun fetchPlantingDate(productType: ProductType): LocalDate? {
        // Query the most recent planting for this crop type
        val cropName = when (productType) {
            ProductType.MAIZE -> "maize"
            ProductType.BEANS -> "beans"
            else -> productType.name.lowercase()
        }
        
        // This is a simplified lookup - in production you'd query the actual crop plantings
        // For now, return null to indicate no planting date available
        return null
    }

    fun setPlantingDateForTimeBasedGrading(date: LocalDate) {
        _uiState.update { it.copy(manualPlantingDate = date) }
    }

    fun generateQRInvoice() {
        viewModelScope.launch {
            val state = uiState.value
            
            val qrData = buildString {
                append("SHAMBA_SMART_GRADE|")
                append("Version:2.0|")
                append("Product:${state.selectedProduct.name}|")
                append("Grade:${state.grade}|")
                append("MaturityScore:${String.format("%.1f", state.maturityScore)}|")
                append("Method:${state.gradingMethod ?: "HSV"}|")
                if (state.hue > 0 || state.saturation > 0 || state.value > 0) {
                    append("Hue:${String.format("%.0f", state.hue)}|")
                    append("Sat:${String.format("%.2f", state.saturation)}|")
                    append("Val:${String.format("%.2f", state.value)}|")
                }
                append("HarvestWindow:${state.isInHarvestWindow}|")
                append("DaysToHarvest:${state.daysUntilHarvest}|")
                append("Premium:${String.format("%.2f", state.premiumMultiplier)}|")
                append("Timestamp:${System.currentTimeMillis()}|")
                append("Device:Xiaomi_Pad7|")
                append("Signature:${generateSignature(state)}")
            }

            _uiState.update { it.copy(qrData = qrData) }
        }
    }

    private fun generateSignature(state: VisionUiState): String {
        val data = "${state.grade}${state.maturityScore}${state.hue}${state.saturation}${state.value}${state.gradingMethod}"
        return data.hashCode().toString(16).take(8)
    }

    fun clearResults() {
        _uiState.update { VisionUiState(selectedProduct = it.selectedProduct) }
    }

    fun clearCameraError() {
        _uiState.update { it.copy(cameraError = null) }
    }

    override fun onCleared() {
        super.onCleared()
        cameraManager.release()
    }
}

enum class ProductType {
    TOMATO, MAIZE, BEANS, KALE, ONION, CHEESE_FRESH, CHEESE_AGED
}

data class VisionUiState(
    val isAnalyzing: Boolean = false,
    val isCameraReady: Boolean = false,
    val cameraError: String? = null,
    val selectedProduct: ProductType = ProductType.TOMATO,
    val hue: Double = 0.0,
    val saturation: Double = 0.0,
    val value: Double = 0.0,
    val maturityScore: Double = 0.0,
    val grade: String? = null,
    val harvestStatus: String = "",
    val isInHarvestWindow: Boolean = false,
    val daysUntilHarvest: Int = 0,
    val analysis: String = "",
    val premiumMultiplier: Double = 1.0,
    val qrData: String? = null,
    val gradingMethod: String? = null,
    val manualPlantingDate: LocalDate? = null
)