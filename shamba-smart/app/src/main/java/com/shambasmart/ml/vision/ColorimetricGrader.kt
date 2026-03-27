package com.shambasmart.ml.vision

import com.shambasmart.data.local.entity.CropPlanting
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Colorimetric Grader for produce maturity assessment
 * 
 * Supports two grading methods:
 * 1. HSV Color Analysis - for color-indicated products (tomatoes, onions, kale, cheese)
 * 2. Time-Based Maturity - for non-color-indicated products (maize, beans, cassava)
 */
@Singleton
class ColorimetricGrader @Inject constructor(
    private val hsvAnalyzer: HSVAnalyzer
) {

    data class GradingResult(
        val grade: String,              // A, B, C, D
        val maturityScore: Double,      // 0-100
        val gradingMethod: GradingMethod,
        val analysis: String,
        val isInHarvestWindow: Boolean,
        val harvestStatus: String,
        val daysUntilHarvest: Int,
        val premiumMultiplier: Double,
        val hsvValues: HSVAnalyzer.HSVValues? = null
    )

    enum class GradingMethod {
        HSV_ANALYSIS,       // Color-based grading
        TIME_BASED          // Days-since-planting based grading
    }

    data class ProductMaturityProfile(
        val productType: ProductType,
        val gradingMethod: GradingMethod,
        // HSV ranges (for color-based products)
        val optimalHueRange: ClosedRange<Double> = 0.0..0.0,
        val optimalSaturationRange: ClosedRange<Double> = 0.0..0.0,
        val optimalValueRange: ClosedRange<Double> = 0.0..0.0,
        // Time-based parameters
        val daysToMaturity: Int = 0,
        val harvestWindowDays: Int = 7,
        val premiumMultiplier: Double = 1.0
    )

    enum class ProductType {
        // Color-based products
        TOMATO, ONION, KALE, CHEESE_FRESH, CHEESE_AGED,
        // Time-based products
        MAIZE, BEANS, CASSAVA, SWEET_POTATO
    }

    // Product maturity profiles
    private val maturityProfiles = mapOf(
        // COLOR-BASED PRODUCTS
        ProductType.TOMATO to ProductMaturityProfile(
            productType = ProductType.TOMATO,
            gradingMethod = GradingMethod.HSV_ANALYSIS,
            optimalHueRange = 0.0..30.0,        // Red hues
            optimalSaturationRange = 0.7..1.0,
            optimalValueRange = 0.6..0.9,
            harvestWindowDays = 7,
            premiumMultiplier = 1.3
        ),
        ProductType.ONION to ProductMaturityProfile(
            productType = ProductType.ONION,
            gradingMethod = GradingMethod.HSV_ANALYSIS,
            optimalHueRange = 20.0..40.0,       // Golden hues
            optimalSaturationRange = 0.5..0.8,
            optimalValueRange = 0.6..0.9,
            harvestWindowDays = 21,
            premiumMultiplier = 1.1
        ),
        ProductType.KALE to ProductMaturityProfile(
            productType = ProductType.KALE,
            gradingMethod = GradingMethod.HSV_ANALYSIS,
            optimalHueRange = 100.0..140.0,     // Deep green
            optimalSaturationRange = 0.6..0.9,
            optimalValueRange = 0.4..0.7,
            harvestWindowDays = 5,
            premiumMultiplier = 1.25
        ),
        ProductType.CHEESE_FRESH to ProductMaturityProfile(
            productType = ProductType.CHEESE_FRESH,
            gradingMethod = GradingMethod.HSV_ANALYSIS,
            optimalHueRange = 40.0..60.0,       // Creamy white
            optimalSaturationRange = 0.1..0.3,
            optimalValueRange = 0.85..0.95,
            harvestWindowDays = 3,
            premiumMultiplier = 1.4
        ),
        ProductType.CHEESE_AGED to ProductMaturityProfile(
            productType = ProductType.CHEESE_AGED,
            gradingMethod = GradingMethod.HSV_ANALYSIS,
            optimalHueRange = 30.0..50.0,       // Aged yellow
            optimalSaturationRange = 0.3..0.6,
            optimalValueRange = 0.6..0.8,
            harvestWindowDays = 30,
            premiumMultiplier = 1.5
        ),
        
        // TIME-BASED PRODUCTS
        ProductType.MAIZE to ProductMaturityProfile(
            productType = ProductType.MAIZE,
            gradingMethod = GradingMethod.TIME_BASED,
            daysToMaturity = 105,               // Average 90-120 days
            harvestWindowDays = 14,
            premiumMultiplier = 1.2
        ),
        ProductType.BEANS to ProductMaturityProfile(
            productType = ProductType.BEANS,
            gradingMethod = GradingMethod.TIME_BASED,
            daysToMaturity = 75,                // Average 60-90 days
            harvestWindowDays = 10,
            premiumMultiplier = 1.15
        ),
        ProductType.CASSAVA to ProductMaturityProfile(
            productType = ProductType.CASSAVA,
            gradingMethod = GradingMethod.TIME_BASED,
            daysToMaturity = 315,               // Average 270-365 days
            harvestWindowDays = 30,
            premiumMultiplier = 1.1
        ),
        ProductType.SWEET_POTATO to ProductMaturityProfile(
            productType = ProductType.SWEET_POTATO,
            gradingMethod = GradingMethod.TIME_BASED,
            daysToMaturity = 120,               // Average 90-150 days
            harvestWindowDays = 14,
            premiumMultiplier = 1.15
        )
    )

    /**
     * Grade product using HSV color analysis
     */
    fun gradeByHSV(
        bitmap: android.graphics.Bitmap,
        productType: ProductType
    ): GradingResult {
        val profile = maturityProfiles[productType]
            ?: throw IllegalArgumentException("Unknown product type: $productType")

        if (profile.gradingMethod != GradingMethod.HSV_ANALYSIS) {
            throw IllegalArgumentException("Product $productType uses time-based grading, not HSV")
        }

        // Analyze HSV values
        val hsvResult = hsvAnalyzer.analyzeRegion(bitmap)
        val hsv = hsvResult.averageHSV

        // Calculate maturity score
        val maturityScore = hsvAnalyzer.calculateMaturityScore(
            hsv,
            profile.optimalHueRange,
            profile.optimalSaturationRange,
            profile.optimalValueRange
        )

        // Assign grade
        val grade = assignGrade(maturityScore)

        // Determine harvest window
        val isInHarvestWindow = maturityScore >= 70
        val harvestStatus = getHarvestStatus(maturityScore, profile.harvestWindowDays)
        val daysUntilHarvest = if (maturityScore >= 85) 0 else
            ((100 - maturityScore) / 100.0 * profile.harvestWindowDays).toInt()

        // Generate analysis
        val analysis = buildHSVAnalysis(productType, hsv, hsvResult, maturityScore, grade, profile)

        return GradingResult(
            grade = grade,
            maturityScore = maturityScore,
            gradingMethod = GradingMethod.HSV_ANALYSIS,
            analysis = analysis,
            isInHarvestWindow = isInHarvestWindow,
            harvestStatus = harvestStatus,
            daysUntilHarvest = daysUntilHarvest,
            premiumMultiplier = if (grade == "A") profile.premiumMultiplier else 1.0,
            hsvValues = hsv
        )
    }

    /**
     * Grade product using time-based maturity calculation
     */
    fun gradeByTime(
        productType: ProductType,
        plantingDate: LocalDate
    ): GradingResult {
        val profile = maturityProfiles[productType]
            ?: throw IllegalArgumentException("Unknown product type: $productType")

        if (profile.gradingMethod != GradingMethod.TIME_BASED) {
            throw IllegalArgumentException("Product $productType uses HSV grading, not time-based")
        }

        // Calculate days since planting
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val daysSincePlanting = plantingDate.daysUntil(today)

        // Calculate maturity percentage
        val maturityPercent = (daysSincePlanting.toDouble() / profile.daysToMaturity * 100)
            .coerceIn(0.0, 100.0)

        // Assign grade based on maturity
        val grade = when {
            maturityPercent >= 95 -> "A"  // Peak maturity
            maturityPercent >= 80 -> "B"  // Good maturity
            maturityPercent >= 60 -> "C"  // Acceptable
            else -> "D"                    // Premature
        }

        val maturityScore = maturityPercent

        // Determine harvest window
        val daysUntilMaturity = (profile.daysToMaturity - daysSincePlanting).coerceAtLeast(0)
        val isInHarvestWindow = maturityPercent >= 80
        val harvestStatus = when {
            maturityPercent >= 100 -> "OVERDUE - Harvest immediately"
            maturityPercent >= 95 -> "PEAK - Harvest now for best quality"
            maturityPercent >= 80 -> "OPTIMAL - Harvest within ${profile.harvestWindowDays} days"
            maturityPercent >= 60 -> "APPROACHING - Monitor closely"
            else -> "PREMATURE - Not ready for harvest"
        }

        // Generate analysis
        val analysis = buildTimeAnalysis(productType, daysSincePlanting, maturityPercent, grade, profile)

        return GradingResult(
            grade = grade,
            maturityScore = maturityScore,
            gradingMethod = GradingMethod.TIME_BASED,
            analysis = analysis,
            isInHarvestWindow = isInHarvestWindow,
            harvestStatus = harvestStatus,
            daysUntilHarvest = daysUntilMaturity,
            premiumMultiplier = if (grade == "A") profile.premiumMultiplier else 1.0,
            hsvValues = null
        )
    }

    /**
     * Grade a crop planting (auto-selects method based on product type)
     */
    fun gradeCropPlanting(
        bitmap: android.graphics.Bitmap? = null,
        productType: ProductType,
        plantingDate: LocalDate? = null
    ): GradingResult {
        val profile = maturityProfiles[productType]
            ?: throw IllegalArgumentException("Unknown product type: $productType")

        return when (profile.gradingMethod) {
            GradingMethod.HSV_ANALYSIS -> {
                if (bitmap == null) {
                    throw IllegalArgumentException("Bitmap required for HSV analysis")
                }
                gradeByHSV(bitmap, productType)
            }
            GradingMethod.TIME_BASED -> {
                if (plantingDate == null) {
                    throw IllegalArgumentException("Planting date required for time-based grading")
                }
                gradeByTime(productType, plantingDate)
            }
        }
    }

    private fun assignGrade(score: Double): String {
        return when {
            score >= 85 -> "A"
            score >= 70 -> "B"
            score >= 50 -> "C"
            else -> "D"
        }
    }

    private fun getHarvestStatus(score: Double, harvestWindowDays: Int): String {
        return when {
            score >= 85 -> "PEAK - Harvest immediately for best quality"
            score >= 70 -> "OPTIMAL - Harvest within $harvestWindowDays days"
            score >= 50 -> "APPROACHING - Monitor closely"
            else -> "PREMATURE - Not ready for harvest"
        }
    }

    private fun buildHSVAnalysis(
        productType: ProductType,
        hsv: HSVAnalyzer.HSVValues,
        hsvResult: HSVAnalyzer.HSVAnalysisResult,
        score: Double,
        grade: String,
        profile: ProductMaturityProfile
    ): String {
        return buildString {
            appendLine("Product: ${productType.name.replace("_", " ")}")
            appendLine("Grading Method: HSV Color Analysis")
            appendLine("Maturity Score: ${String.format("%.1f", score)}%")
            appendLine("Grade: $grade")
            appendLine()
            appendLine("HSV Analysis:")
            appendLine("• Hue: ${String.format("%.0f", hsv.hue)}° (Optimal: ${profile.optimalHueRange})")
            appendLine("• Saturation: ${String.format("%.0f", hsv.saturation * 100)}% (Optimal: ${profile.optimalSaturationRange})")
            appendLine("• Value: ${String.format("%.0f", hsv.value * 100)}% (Optimal: ${profile.optimalValueRange})")
            appendLine("• Dominant Color: ${hsvResult.dominantColorRegion}")
            appendLine("• Color Uniformity: ${String.format("%.0f", hsvResult.colorUniformity * 100)}%")
            appendLine()
            appendLine("Quality Assessment:")
            when (grade) {
                "A" -> append("Excellent quality. Peak maturity detected. Commands ${((profile.premiumMultiplier - 1) * 100).toInt()}% price premium.")
                "B" -> append("Good quality. Minor variations from optimal. Suitable for standard market.")
                "C" -> append("Acceptable quality. Consider processing or local market sale.")
                else -> append("Below standard. Not recommended for harvest yet.")
            }
        }
    }

    private fun buildTimeAnalysis(
        productType: ProductType,
        daysSincePlanting: Int,
        maturityPercent: Double,
        grade: String,
        profile: ProductMaturityProfile
    ): String {
        return buildString {
            appendLine("Product: ${productType.name.replace("_", " ")}")
            appendLine("Grading Method: Time-Based Maturity")
            appendLine("Days Since Planting: $daysSincePlanting")
            appendLine("Days to Maturity: ${profile.daysToMaturity}")
            appendLine("Maturity: ${String.format("%.1f", maturityPercent)}%")
            appendLine("Grade: $grade")
            appendLine()
            appendLine("Harvest Timeline:")
            val daysRemaining = (profile.daysToMaturity - daysSincePlanting).coerceAtLeast(0)
            if (daysRemaining > 0) {
                appendLine("• Days to peak maturity: $daysRemaining")
                appendLine("• Expected harvest window: ${profile.harvestWindowDays} days")
            } else {
                appendLine("• OVERDUE by ${abs(daysRemaining)} days")
            }
            appendLine()
            appendLine("Quality Assessment:")
            when (grade) {
                "A" -> append("Peak maturity reached. Harvest immediately for best quality and ${((profile.premiumMultiplier - 1) * 100).toInt()}% price premium.")
                "B" -> append("Good maturity. Ready for harvest within the optimal window.")
                "C" -> append("Approaching maturity. Monitor and prepare for harvest.")
                else -> append("Still developing. Harvesting now would result in lower quality and yield.")
            }
        }
    }

    fun getProductType(name: String): ProductType? {
        return try {
            ProductType.valueOf(name.uppercase())
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}