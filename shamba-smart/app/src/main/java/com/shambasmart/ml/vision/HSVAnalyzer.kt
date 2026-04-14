package com.shambasmart.ml.vision

import android.graphics.Bitmap
import android.graphics.Color
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * HSV Color Space Analyzer for produce maturity grading
 * 
 * Converts RGB images to HSV color space and analyzes
 * color distribution for maturity assessment.
 * 
 * Note: This implementation uses Android's built-in Color class
 * for HSV conversion. For production, consider using OpenCV
 * for more accurate color space conversion.
 */
@Singleton
class HSVAnalyzer @Inject constructor() {

    data class HSVValues(
        val hue: Double,        // 0-360 degrees
        val saturation: Double, // 0-1
        val value: Double       // 0-1
    )

    data class HSVAnalysisResult(
        val averageHSV: HSVValues,
        val hueDistribution: Map<Int, Int>,     // Hue range (10° bins) -> pixel count
        val saturationAvg: Double,
        val valueAvg: Double,
        val dominantColorRegion: String,        // "red", "green", "yellow", etc.
        val colorUniformity: Double             // 0-1, how uniform the color is
    )

    /**
     * Analyze a bitmap image and return HSV analysis
     */
    fun analyzeImage(bitmap: Bitmap): HSVAnalysisResult {
        val width = bitmap.width
        val height = bitmap.height

        // Downsample for analysis if the bitmap is too large to save memory and time
        val sampleSize = if (width * height > 640 * 480) 4 else 2
        val sampledWidth = width / sampleSize
        val sampledHeight = height / sampleSize

        val pixels = IntArray(sampledWidth * sampledHeight)

        // Use a temporary downsampled bitmap if needed
        val analysisBitmap = if (sampleSize > 1) {
            Bitmap.createScaledBitmap(bitmap, sampledWidth, sampledHeight, false)
        } else {
            bitmap
        }

        analysisBitmap.getPixels(pixels, 0, sampledWidth, 0, 0, sampledWidth, sampledHeight)

        // Clean up temporary bitmap if created
        if (analysisBitmap != bitmap) {
            analysisBitmap.recycle()
        }

        val hsvValues = mutableListOf<HSVValues>()
        val hueDistribution = mutableMapOf<Int, Int>()

        // Initialize hue distribution bins (0-360 in 10° bins)
        for (i in 0 until 36) {
            hueDistribution[i * 10] = 0
        }

        // Process sampled pixels
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)

            val hsv = rgbToHSV(r, g, b)
            hsvValues.add(hsv)

            // Add to hue distribution
            val hueBin = (hsv.hue.toInt() / 10) * 10
            hueDistribution[hueBin] = (hueDistribution[hueBin] ?: 0) + 1
        }

        // Calculate averages
        val avgHue = hsvValues.map { it.hue }.average()
        val avgSaturation = hsvValues.map { it.saturation }.average()
        val avgValue = hsvValues.map { it.value }.average()

        // Determine dominant color region
        val dominantColor = getDominantColor(avgHue)

        // Calculate color uniformity (based on standard deviation of hue)
        val hueStdDev = if (hsvValues.size > 1) {
            val mean = avgHue
            val variance = hsvValues.map { (it.hue - mean) * (it.hue - mean) }.average()
            kotlin.math.sqrt(variance)
        } else {
            0.0
        }
        
        // Uniformity is inversely proportional to standard deviation
        // Lower stdDev = higher uniformity
        val uniformity = max(0.0, 1.0 - (hueStdDev / 180.0))

        return HSVAnalysisResult(
            averageHSV = HSVValues(avgHue, avgSaturation, avgValue),
            hueDistribution = hueDistribution,
            saturationAvg = avgSaturation,
            valueAvg = avgValue,
            dominantColorRegion = dominantColor,
            colorUniformity = uniformity
        )
    }

    /**
     * Convert RGB to HSV color space
     */
    private fun rgbToHSV(r: Int, g: Int, b: Int): HSVValues {
        val rf = r / 255.0
        val gf = g / 255.0
        val bf = b / 255.0

        val cmax = max(rf, max(gf, bf))
        val cmin = min(rf, min(gf, bf))
        val diff = cmax - cmin

        // Hue calculation
        val hue = when {
            diff == 0.0 -> 0.0
            cmax == rf -> 60.0 * (((gf - bf) / diff) % 6)
            cmax == gf -> 60.0 * (((bf - rf) / diff) + 2)
            else -> 60.0 * (((rf - gf) / diff) + 4)
        }.let { if (it < 0) it + 360 else it }

        // Saturation calculation
        val saturation = if (cmax == 0.0) 0.0 else diff / cmax

        // Value calculation
        val value = cmax

        return HSVValues(hue, saturation, value)
    }

    /**
     * Get dominant color name based on hue
     */
    private fun getDominantColor(hue: Double): String {
        return when {
            hue < 15 || hue >= 345 -> "red"
            hue < 45 -> "orange"
            hue < 75 -> "yellow"
            hue < 150 -> "green"
            hue < 210 -> "cyan"
            hue < 270 -> "blue"
            hue < 315 -> "purple"
            else -> "pink"
        }
    }

    /**
     * Analyze a specific region of the bitmap (center crop)
     */
    fun analyzeRegion(bitmap: Bitmap, regionFraction: Double = 0.5): HSVAnalysisResult {
        val width = bitmap.width
        val height = bitmap.height
        
        // Calculate center crop region
        val regionWidth = (width * regionFraction).toInt()
        val regionHeight = (height * regionFraction).toInt()
        val startX = (width - regionWidth) / 2
        val startY = (height - regionHeight) / 2

        // Extract region
        val regionBitmap = Bitmap.createBitmap(bitmap, startX, startY, regionWidth, regionHeight)
        
        val result = analyzeImage(regionBitmap)

        // Free the cropped bitmap
        regionBitmap.recycle()

        return result
    }

    /**
     * Compare HSV values against optimal ranges
     */
    fun calculateMaturityScore(
        hsv: HSVValues,
        optimalHueRange: ClosedRange<Double>,
        optimalSaturationRange: ClosedRange<Double>,
        optimalValueRange: ClosedRange<Double>
    ): Double {
        val hueScore = if (hsv.hue in optimalHueRange) {
            1.0
        } else {
            val distToStart = abs(hsv.hue - optimalHueRange.start)
            val distToEnd = abs(hsv.hue - optimalHueRange.endInclusive)
            val minDist = min(distToStart, distToEnd)
            // Normalize by max possible distance (180 degrees)
            max(0.0, 1.0 - (minDist / 180.0))
        }

        val satScore = if (hsv.saturation in optimalSaturationRange) {
            1.0
        } else {
            val distToStart = abs(hsv.saturation - optimalSaturationRange.start)
            val distToEnd = abs(hsv.saturation - optimalSaturationRange.endInclusive)
            val minDist = min(distToStart, distToEnd)
            max(0.0, 1.0 - minDist)
        }

        val valScore = if (hsv.value in optimalValueRange) {
            1.0
        } else {
            val distToStart = abs(hsv.value - optimalValueRange.start)
            val distToEnd = abs(hsv.value - optimalValueRange.endInclusive)
            val minDist = min(distToStart, distToEnd)
            max(0.0, 1.0 - minDist)
        }

        // Weighted score: Hue is most important for color-based grading
        return (hueScore * 0.5 + satScore * 0.25 + valScore * 0.25) * 100
    }
}