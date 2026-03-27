package com.shambasmart.ml

import android.graphics.Bitmap
import android.graphics.Color
import com.shambasmart.data.local.entity.BoundingBox
import com.shambasmart.data.local.entity.InferenceResult
import com.shambasmart.data.local.entity.PestDetection
import com.shambasmart.data.local.entity.SeverityLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Multi-stage inference pipeline for pest detection.
 * 
 * Stage 1: Detect pests/symptoms with bounding boxes
 * Stage 2: Classify severity based on leaf area affected or pest density
 * 
 * Optimized for <50ms inference on NPU.
 */
@Singleton
class PestClassifier @Inject constructor(
    private val onnxManager: OnnxModelManager
) {

    companion object {
        private const val INPUT_SIZE = 640
        private const val MEAN_R = 0.485f
        private const val MEAN_G = 0.456f
        private const val MEAN_B = 0.406f
        private const val STD_R = 0.229f
        private const val STD_G = 0.224f
        private const val STD_B = 0.225f
    }

    /**
     * Runs the full pest detection pipeline on a bitmap image.
     * 
     * @param bitmap Input image bitmap
     * @return InferenceResult with detections and severity classifications
     */
    suspend fun classify(bitmap: Bitmap): InferenceResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        // Stage 1: Preprocess image
        val inputTensor = preprocessImage(bitmap)

        // Stage 2: Run ONNX inference
        val rawOutput = onnxManager.runInference(inputTensor)

        // Stage 3: Process detections
        val rawDetections = if (rawOutput != null) {
            onnxManager.processDetections(rawOutput, bitmap.width, bitmap.height)
        } else {
            emptyList()
        }

        // Stage 4: Classify severity for each detection
        val detections = rawDetections.map { raw ->
            val severity = classifySeverity(raw, bitmap)
            PestDetection(
                pestClass = raw.pestClass,
                confidence = raw.confidence,
                boundingBox = BoundingBox(
                    x = raw.x,
                    y = raw.y,
                    width = raw.width,
                    height = raw.height
                ),
                severityLevel = severity
            )
        }

        val processingTime = System.currentTimeMillis() - startTime

        InferenceResult(
            detections = detections,
            processingTimeMs = processingTime,
            modelVersion = onnxManager.getModelVersion(),
            imageSize = Pair(bitmap.width, bitmap.height)
        )
    }

    /**
     * Preprocesses a bitmap image for YOLOv8 inference.
     * Resizes to 640x640 and normalizes with ImageNet stats.
     */
    private fun preprocessImage(bitmap: Bitmap): FloatArray {
        val resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        val floatArray = FloatArray(3 * INPUT_SIZE * INPUT_SIZE)

        for (y in 0 until INPUT_SIZE) {
            for (x in 0 until INPUT_SIZE) {
                val pixel = resized.getPixel(x, y)
                val r = Color.red(pixel) / 255.0f
                val g = Color.green(pixel) / 255.0f
                val b = Color.blue(pixel) / 255.0f

                // Normalize with ImageNet stats and convert to NCHW format
                val idx = y * INPUT_SIZE + x
                floatArray[idx] = (r - MEAN_R) / STD_R // R channel
                floatArray[INPUT_SIZE * INPUT_SIZE + idx] = (g - MEAN_G) / STD_G // G channel
                floatArray[2 * INPUT_SIZE * INPUT_SIZE + idx] = (b - MEAN_B) / STD_B // B channel
            }
        }

        resized.recycle()
        return floatArray
    }

    /**
     * Stage 2: Classifies severity based on detection confidence and area.
     * 
     * Severity mapping:
     * - LOW (1): Small area, low confidence (<0.4)
     * - MINOR (2): Small area, moderate confidence (0.4-0.6)
     * - MODERATE (3): Medium area, moderate confidence
     * - SEVERE (4): Large area, high confidence (0.6-0.8)
     * - CRITICAL (5): Very large area, very high confidence (>0.8)
     */
    private fun classifySeverity(
        detection: PestDetectionResult,
        bitmap: Bitmap
    ): SeverityLevel {
        // Calculate bounding box area as percentage of image
        val boxArea = detection.width * detection.height
        val imageArea = bitmap.width * bitmap.height
        val areaPercentage = boxArea / imageArea

        return when {
            detection.confidence >= 0.8f && areaPercentage >= 0.15f -> SeverityLevel.CRITICAL
            detection.confidence >= 0.6f && areaPercentage >= 0.10f -> SeverityLevel.SEVERE
            detection.confidence >= 0.5f && areaPercentage >= 0.05f -> SeverityLevel.MODERATE
            detection.confidence >= 0.4f && areaPercentage >= 0.02f -> SeverityLevel.MINOR
            else -> SeverityLevel.LOW
        }
    }

    /**
     * Estimates pest density from multiple detections in the same area.
     * Used for group pests like aphids or locusts.
     */
    fun estimatePestDensity(detections: List<PestDetectionResult>): Int {
        if (detections.isEmpty()) return 0

        // Group detections by pest type
        val byType = detections.groupBy { it.pestClass }

        return byType.maxOf { (_, pests) ->
            when {
                pests.size >= 10 -> 5 // Critical density
                pests.size >= 5 -> 4  // High density
                pests.size >= 3 -> 3  // Moderate density
                pests.size >= 2 -> 2  // Low density
                else -> 1 // Single pest
            }
        }
    }

    /**
     * Calculates leaf area percentage affected by pest damage.
     * Uses the bounding box area and severity heuristics.
     */
    fun calculateLeafAreaAffected(detections: List<PestDetectionResult>, bitmap: Bitmap): Float {
        if (detections.isEmpty()) return 0f

        val totalBoxArea = detections.sumOf { (it.width * it.height).toDouble() }.toFloat()
        val imageArea = bitmap.width * bitmap.height

        return (totalBoxArea / imageArea) * 100f
    }
}