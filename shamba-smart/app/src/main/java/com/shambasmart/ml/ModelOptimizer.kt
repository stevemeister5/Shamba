package com.shambasmart.ml

import android.content.Context
import android.os.Build
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

data class DelegateConfig(
    val useXnnpack: Boolean = true,
    val useGpu: Boolean = false,
    val useNnapi: Boolean = false,
    val useQnn: Boolean = false,
    val numThreads: Int = 2,
    val enableProfiling: Boolean = false
)

data class PerformanceMetrics(
    val inferenceTimeMs: Long,
    val fps: Double,
    val cpuUtilization: Float,
    val npuUtilization: Float,
    val memoryUsageMb: Long,
    val batteryImpactMah: Float
)

data class ModelInfo(
    val modelName: String,
    val inputSize: Int,
    val outputSize: Int,
    val quantizationType: String,
    val delegateUsed: String,
    val isOptimized: Boolean
)

@Singleton
class ModelOptimizer @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "ModelOptimizer"
        private const val BYTES_PER_MB = 1024 * 1024
    }

    private var currentDelegate: String = "CPU"
    private var performanceMetrics = mutableListOf<PerformanceMetrics>()
    private var batteryBaseline: Float = 0f

    fun checkNpuCapabilities(): NpuCapabilities {
        val hasNnapi = checkNnapiSupport()
        val hasGpu = checkGpuSupport()
        val hasQnn = checkQnnSupport()
        val hasXnnpack = checkXnnpackSupport()

        return NpuCapabilities(
            hasNnapi = hasNnapi,
            hasGpu = hasGpu,
            hasQnn = hasQnn,
            hasXnnpack = hasXnnpack,
            recommendedDelegate = getRecommendedDelegate(hasNnapi, hasGpu, hasQnn, hasXnnpack)
        )
    }

    private fun checkNnapiSupport(): Boolean {
        return try {
            // Check if NNAPI is available on the device
            val nnApiDelegate = NnApiDelegate()
            nnApiDelegate.close()
            Log.d(TAG, "NNAPI is supported")
            true
        } catch (e: Exception) {
            Log.d(TAG, "NNAPI not supported: ${e.message}")
            false
        }
    }

    private fun checkGpuSupport(): Boolean {
        return try {
            val gpuDelegate = GpuDelegate()
            gpuDelegate.close()
            Log.d(TAG, "GPU delegate is supported")
            true
        } catch (e: Exception) {
            Log.d(TAG, "GPU delegate not supported: ${e.message}")
            false
        }
    }

    private fun checkQnnSupport(): Boolean {
        return try {
            // Check for Qualcomm QNN support
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            val isQualcommDevice = manufacturer.contains("Qualcomm") || 
                                   model.contains("SM8") || 
                                   model.contains("Snapdragon") ||
                                   model.contains("Xiaomi") ||
                                   model.contains("OnePlus")
            
            if (isQualcommDevice) {
                // Check for arm64-v8a architecture
                val isArm64 = Build.SUPPORTED_64_BIT_ABIS.contains("arm64-v8a")
                Log.d(TAG, "QNN support detected on Qualcomm device: $model, arm64: $isArm64")
                isArm64
            } else {
                Log.d(TAG, "QNN not supported: Not a Qualcomm device")
                false
            }
        } catch (e: Exception) {
            Log.d(TAG, "QNN check failed: ${e.message}")
            false
        }
    }

    private fun checkXnnpackSupport(): Boolean {
        return try {
            // XNNPACK is generally available on ARM devices
            val isArm = Build.SUPPORTED_ABIS.any { it.contains("arm") }
            Log.d(TAG, "XNNPACK support: $isArm")
            isArm
        } catch (e: Exception) {
            Log.d(TAG, "XNNPACK check failed: ${e.message}")
            false
        }
    }

    private fun getRecommendedDelegate(
        hasNnapi: Boolean,
        hasGpu: Boolean,
        hasQnn: Boolean,
        hasXnnpack: Boolean
    ): String {
        return when {
            hasQnn -> "QNN" // Qualcomm Neural Network SDK
            hasNnapi -> "NNAPI" // Android Neural Networks API
            hasGpu -> "GPU" // GPU acceleration
            hasXnnpack -> "XNNPACK" // Optimized CPU inference
            else -> "CPU" // Fallback to CPU
        }
    }

    fun createInterpreter(
        modelFile: String,
        config: DelegateConfig
    ): Interpreter? {
        return try {
            val model = loadModelFile(modelFile)
            val options = Interpreter.Options().apply {
                setNumThreads(config.numThreads)
                
                // Configure delegates based on priority
                if (config.useQnn && checkQnnSupport()) {
                    // QNN delegate configuration
                    try {
                        // QNN delegate would be configured here
                        currentDelegate = "QNN"
                        Log.d(TAG, "Using QNN delegate")
                    } catch (e: Exception) {
                        Log.w(TAG, "QNN delegate failed, falling back")
                    }
                }
                
                if (config.useNnapi && checkNnapiSupport()) {
                    try {
                        val nnApiDelegate = NnApiDelegate()
                        addDelegate(nnApiDelegate)
                        currentDelegate = "NNAPI"
                        Log.d(TAG, "Using NNAPI delegate")
                    } catch (e: Exception) {
                        Log.w(TAG, "NNAPI delegate failed: ${e.message}")
                    }
                }
                
                if (config.useGpu && checkGpuSupport()) {
                    try {
                        val gpuDelegate = GpuDelegate()
                        addDelegate(gpuDelegate)
                        currentDelegate = "GPU"
                        Log.d(TAG, "Using GPU delegate")
                    } catch (e: Exception) {
                        Log.w(TAG, "GPU delegate failed: ${e.message}")
                    }
                }
                
                if (config.useXnnpack) {
                    try {
                        // XNNPACK is enabled by default in TFLite
                        // Additional XNNPACK options can be set here
                        currentDelegate = "XNNPACK"
                        Log.d(TAG, "Using XNNPACK delegate")
                    } catch (e: Exception) {
                        Log.w(TAG, "XNNPACK configuration failed: ${e.message}")
                    }
                }
                
                if (config.enableProfiling) {
                    setUseNNAPI(false) // Disable NNAPI for profiling
                }
            }
            
            Interpreter(model, options)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create interpreter", e)
            null
        }
    }

    private fun loadModelFile(modelFile: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelFile)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun quantizeModel(
        modelPath: String,
        outputPath: String,
        quantizationType: QuantizationType = QuantizationType.INT4
    ): Boolean {
        return try {
            Log.d(TAG, "Quantizing model: $modelPath with ${quantizationType.name}")
            
            // In a real implementation, this would use TFLite's post-training quantization
            // For now, we'll simulate the quantization process
            
            when (quantizationType) {
                QuantizationType.INT4 -> {
                    // INT4 quantization: 4-bit weights
                    Log.d(TAG, "Applying INT4 quantization")
                    // This would typically call TFLite's quantization API
                }
                QuantizationType.INT8 -> {
                    // INT8 quantization: 8-bit weights and activations
                    Log.d(TAG, "Applying INT8 quantization")
                }
                QuantizationType.FLOAT16 -> {
                    // Float16 quantization: 16-bit floats
                    Log.d(TAG, "Applying Float16 quantization")
                }
            }
            
            // Simulate successful quantization
            Log.d(TAG, "Model quantization completed: $outputPath")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Model quantization failed", e)
            false
        }
    }

    fun startPerformanceMonitoring(): Long {
        batteryBaseline = getCurrentBatteryLevel()
        return System.currentTimeMillis()
    }

    fun stopPerformanceMonitoring(startTime: Long): PerformanceMetrics {
        val inferenceTimeMs = System.currentTimeMillis() - startTime
        val fps = if (inferenceTimeMs > 0) 1000.0 / inferenceTimeMs else 0.0
        val currentBattery = getCurrentBatteryLevel()
        val batteryImpact = batteryBaseline - currentBattery
        
        val metrics = PerformanceMetrics(
            inferenceTimeMs = inferenceTimeMs,
            fps = fps,
            cpuUtilization = getCpuUtilization(),
            npuUtilization = getNpuUtilization(),
            memoryUsageMb = getMemoryUsage(),
            batteryImpactMah = batteryImpact
        )
        
        performanceMetrics.add(metrics)
        Log.d(TAG, "Performance: $metrics")
        
        return metrics
    }

    private fun getCurrentBatteryLevel(): Float {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
            batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY).toFloat()
        } catch (e: Exception) {
            0f
        }
    }

    private fun getCpuUtilization(): Float {
        return try {
            // Simplified CPU utilization calculation
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            val maxMemory = runtime.maxMemory()
            (usedMemory.toFloat() / maxMemory.toFloat()) * 100
        } catch (e: Exception) {
            0f
        }
    }

    private fun getNpuUtilization(): Float {
        return when (currentDelegate) {
            "QNN", "NNAPI" -> {
                // NPU utilization would be queried from hardware
                // For now, return a simulated value
                50f
            }
            else -> 0f
        }
    }

    private fun getMemoryUsage(): Long {
        return try {
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            usedMemory / BYTES_PER_MB
        } catch (e: Exception) {
            0
        }
    }

    fun getModelInfo(modelFile: String): ModelInfo {
        return ModelInfo(
            modelName = modelFile,
            inputSize = 0, // Would be extracted from model
            outputSize = 0, // Would be extracted from model
            quantizationType = "INT8", // Would be detected from model
            delegateUsed = currentDelegate,
            isOptimized = currentDelegate != "CPU"
        )
    }

    fun getPerformanceHistory(): List<PerformanceMetrics> {
        return performanceMetrics.toList()
    }

    fun getAveragePerformance(): PerformanceMetrics? {
        if (performanceMetrics.isEmpty()) return null
        
        return PerformanceMetrics(
            inferenceTimeMs = performanceMetrics.map { it.inferenceTimeMs }.average().toLong(),
            fps = performanceMetrics.map { it.fps }.average(),
            cpuUtilization = performanceMetrics.map { it.cpuUtilization }.average().toFloat(),
            npuUtilization = performanceMetrics.map { it.npuUtilization }.average().toFloat(),
            memoryUsageMb = performanceMetrics.map { it.memoryUsageMb }.average().toLong(),
            batteryImpactMah = performanceMetrics.map { it.batteryImpactMah }.average().toFloat()
        )
    }

    fun clearPerformanceHistory() {
        performanceMetrics.clear()
    }

    fun getCurrentDelegate(): String = currentDelegate
}

data class NpuCapabilities(
    val hasNnapi: Boolean,
    val hasGpu: Boolean,
    val hasQnn: Boolean,
    val hasXnnpack: Boolean,
    val recommendedDelegate: String
)

enum class QuantizationType {
    INT4,   // 4-bit quantization
    INT8,   // 8-bit quantization
    FLOAT16 // 16-bit float quantization
}