package com.shambasmart.ml.vision

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.*
import kotlin.random.Random

data class ProductMaturity(
    val productType: ProductType,
    val optimalHueRange: ClosedRange<Double>,
    val optimalSaturationRange: ClosedRange<Double>,
    val optimalValueRange: ClosedRange<Double>,
    val harvestWindowDays: Int,
    val premiumMultiplier: Double
)

enum class ProductType {
    TOMATO, MAIZE, BEANS, KALE, ONION, CHEESE_FRESH, CHEESE_AGED
}

@HiltViewModel
class VisionGradingViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(VisionUiState())
    val uiState: StateFlow<VisionUiState> = _uiState.asStateFlow()

    // Maturity profiles for different products
    private val maturityProfiles = mapOf(
        ProductType.TOMATO to ProductMaturity(
            productType = ProductType.TOMATO,
            optimalHueRange = 0.0..30.0, // Red hues
            optimalSaturationRange = 0.7..1.0,
            optimalValueRange = 0.6..0.9,
            harvestWindowDays = 7,
            premiumMultiplier = 1.3
        ),
        ProductType.MAIZE to ProductMaturity(
            productType = ProductType.MAIZE,
            optimalHueRange = 40.0..60.0, // Yellow hues
            optimalSaturationRange = 0.6..0.9,
            optimalValueRange = 0.7..1.0,
            harvestWindowDays = 14,
            premiumMultiplier = 1.2
        ),
        ProductType.BEANS to ProductMaturity(
            productType = ProductType.BEANS,
            optimalHueRange = 90.0..130.0, // Green hues
            optimalSaturationRange = 0.5..0.8,
            optimalValueRange = 0.5..0.8,
            harvestWindowDays = 10,
            premiumMultiplier = 1.15
        ),
        ProductType.KALE to ProductMaturity(
            productType = ProductType.KALE,
            optimalHueRange = 100.0..140.0, // Deep green
            optimalSaturationRange = 0.6..0.9,
            optimalValueRange = 0.4..0.7,
            harvestWindowDays = 5,
            premiumMultiplier = 1.25
        ),
        ProductType.ONION to ProductMaturity(
            productType = ProductType.ONION,
            optimalHueRange = 20.0..40.0, // Golden hues
            optimalSaturationRange = 0.5..0.8,
            optimalValueRange = 0.6..0.9,
            harvestWindowDays = 21,
            premiumMultiplier = 1.1
        ),
        ProductType.CHEESE_FRESH to ProductMaturity(
            productType = ProductType.CHEESE_FRESH,
            optimalHueRange = 40.0..60.0, // Creamy white
            optimalSaturationRange = 0.1..0.3,
            optimalValueRange = 0.85..0.95,
            harvestWindowDays = 3,
            premiumMultiplier = 1.4
        ),
        ProductType.CHEESE_AGED to ProductMaturity(
            productType = ProductType.CHEESE_AGED,
            optimalHueRange = 30.0..50.0, // Aged yellow
            optimalSaturationRange = 0.3..0.6,
            optimalValueRange = 0.6..0.8,
            harvestWindowDays = 30,
            premiumMultiplier = 1.5
        )
    )

    fun setProductType(type: ProductType) {
        _uiState.update { it.copy(selectedProduct = type) }
    }

    fun captureAndAnalyze() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true) }

            // Simulate camera capture and HSV analysis
            // In production, this would use OpenCV for real analysis
            kotlinx.coroutines.delay(1500) // Simulate processing time

            // Simulated HSV values (would come from camera)
            val hue = Random.nextDouble(0.0, 360.0)
            val saturation = Random.nextDouble(0.3, 1.0)
            val value = Random.nextDouble(0.4, 1.0)

            val productType = uiState.value.selectedProduct
            val profile = maturityProfiles[productType] ?: maturityProfiles[ProductType.TOMATO]!!

            // Calculate maturity score
            val hueScore = if (hue in profile.optimalHueRange) 1.0 else 
                1.0 - (min(abs(hue - profile.optimalHueRange.start), abs(hue - profile.optimalHueRange.endInclusive)) / 180.0)
            
            val satScore = if (saturation in profile.optimalSaturationRange) 1.0 else
                1.0 - (min(abs(saturation - profile.optimalSaturationRange.start), abs(saturation - profile.optimalSaturationRange.endInclusive)))
            
            val valScore = if (value in profile.optimalValueRange) 1.0 else
                1.0 - (min(abs(value - profile.optimalValueRange.start), abs(value - profile.optimalValueRange.endInclusive)))

            val maturityScore = (hueScore * 0.4 + satScore * 0.3 + valScore * 0.3) * 100

            // Grade based on maturity score
            val grade = when {
                maturityScore >= 85 -> "A"
                maturityScore >= 70 -> "B"
                maturityScore >= 50 -> "C"
                else -> "D"
            }

            // Determine harvest window status
            val harvestStatus = when {
                maturityScore >= 85 -> "PEAK - Harvest immediately for best quality"
                maturityScore >= 70 -> "OPTIMAL - Harvest within ${profile.harvestWindowDays} days"
                maturityScore >= 50 -> "APPROACHING - Monitor closely"
                else -> "PREMATURE - Not ready for harvest"
            }

            // Check if in harvest window
            val isInHarvestWindow = maturityScore >= 70
            val daysUntilHarvest = if (maturityScore >= 85) 0 else 
                ((100 - maturityScore) / 100.0 * profile.harvestWindowDays).toInt()

            // Generate analysis
            val analysis = buildString {
                appendLine("Product: ${productType.name.replace("_", " ")}")
                appendLine("Maturity Score: ${String.format("%.1f", maturityScore)}%")
                appendLine("Grade: $grade")
                appendLine()
                appendLine("HSV Analysis:")
                appendLine("• Hue: ${String.format("%.0f", hue)}° (Optimal: ${profile.optimalHueRange})")
                appendLine("• Saturation: ${String.format("%.0f", saturation * 100)}% (Optimal: ${profile.optimalSaturationRange})")
                appendLine("• Value: ${String.format("%.0f", value * 100)}% (Optimal: ${profile.optimalValueRange})")
                appendLine()
                appendLine("Harvest Window: $harvestStatus")
                if (daysUntilHarvest > 0) {
                    appendLine("Estimated days to peak: $daysUntilHarvest")
                }
                appendLine()
                appendLine("Quality Assessment:")
                when (grade) {
                    "A" -> append("Excellent quality. Peak maturity detected. Commands ${((profile.premiumMultiplier - 1) * 100).toInt()}% price premium.")
                    "B" -> append("Good quality. Minor variations from optimal. Suitable for standard market.")
                    "C" -> append("Acceptable quality. Consider processing or local market sale.")
                    else -> append("Below standard. Not recommended for harvest yet.")
                }
            }

            _uiState.update {
                it.copy(
                    isAnalyzing = false,
                    hue = hue,
                    saturation = saturation,
                    value = value,
                    maturityScore = maturityScore,
                    grade = grade,
                    harvestStatus = harvestStatus,
                    isInHarvestWindow = isInHarvestWindow,
                    daysUntilHarvest = daysUntilHarvest,
                    analysis = analysis,
                    premiumMultiplier = profile.premiumMultiplier
                )
            }
        }
    }

    fun generateQRInvoice() {
        viewModelScope.launch {
            val state = uiState.value
            
            // Generate QR code with comprehensive grading metadata
            val qrData = buildString {
                append("SHAMBA_SMART_GRADE|")
                append("Version:2.0|")
                append("Product:${state.selectedProduct.name}|")
                append("Grade:${state.grade}|")
                append("MaturityScore:${String.format("%.1f", state.maturityScore)}|")
                append("Hue:${String.format("%.0f", state.hue)}|")
                append("Sat:${String.format("%.2f", state.saturation)}|")
                append("Val:${String.format("%.2f", state.value)}|")
                append("HarvestWindow:${state.isInHarvestWindow}|")
                append("DaysToHarvest:${state.daysUntilHarvest}|")
                append("Premium:${String.format("%.2f", state.premiumMultiplier)}|")
                append("Timestamp:${System.currentTimeMillis()}|")
                append("Device:Xiaomi_Pad7|")
                append("Signature:${generateSignature(state)}")
            }

            _uiState.update {
                it.copy(qrData = qrData)
            }
        }
    }

    private fun generateSignature(state: VisionUiState): String {
        // Simple signature for data integrity
        val data = "${state.grade}${state.maturityScore}${state.hue}${state.saturation}${state.value}"
        return data.hashCode().toString(16).take(8)
    }

    fun clearResults() {
        _uiState.update { VisionUiState(selectedProduct = it.selectedProduct) }
    }
}

data class VisionUiState(
    val isAnalyzing: Boolean = false,
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
    val qrData: String? = null
)