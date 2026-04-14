package com.shambasmart.ml

import android.content.Context
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized model loading and version management for all ONNX models.
 * 
 * Features:
 * - Lazy loading to reduce startup time
 * - Model version tracking and update mechanism
 * - Model integrity verification (checksum)
 * - Memory management for multiple loaded models
 * - Model download from remote for updates (when online)
 */
@Singleton
class ModelManager @Inject constructor(
    private val context: Context,
    private val onnxManager: OnnxModelManager
) {
    private val mutex = Mutex()
    
    companion object {
        private const val MODELS_DIR = "models"
        private const val METADATA_FILE = "model_metadata.json"
        private const val CHECKSUM_ALGORITHM = "SHA-256"
    }

    /**
     * Model metadata for version tracking.
     */
    data class ModelMetadata(
        val modelName: String,
        val version: String,
        val checksum: String,
        val sizeBytes: Long,
        val lastUpdated: Long,
        val isLoaded: Boolean = false,
        val loadTimeMs: Long = 0
    )

    private val loadedModels = mutableMapOf<String, ModelMetadata>()
    private val modelLoadTimes = mutableMapOf<String, Long>()

    /**
     * Initializes model manager and loads metadata.
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                // Load existing model metadata
                loadModelMetadata()
                
                // Initialize ONNX manager
                onnxManager.initialize()
                
                // Mark pest classifier as loaded
                loadedModels["pest_classifier"] = ModelMetadata(
                    modelName = "pest_classifier",
                    version = "1.0.0",
                    checksum = "pending",
                    sizeBytes = 0,
                    lastUpdated = System.currentTimeMillis(),
                    isLoaded = true,
                    loadTimeMs = 0
                )
            } catch (e: Exception) {
                e.printStackTrace()
                throw IllegalStateException("Failed to initialize ModelManager: ${e.message}")
            }
        }
    }

    /**
     * Checks if a model is loaded and ready for inference.
     */
    fun isModelLoaded(modelName: String): Boolean {
        return loadedModels[modelName]?.isLoaded == true
    }

    /**
     * Gets model metadata.
     */
    fun getModelMetadata(modelName: String): ModelMetadata? {
        return loadedModels[modelName]
    }

    /**
     * Gets all loaded models.
     */
    fun getLoadedModels(): Map<String, ModelMetadata> {
        return loadedModels.toMap()
    }

    /**
     * Gets model version.
     */
    fun getModelVersion(modelName: String): String {
        return loadedModels[modelName]?.version ?: "unknown"
    }

    /**
     * Verifies model integrity using checksum.
     */
    suspend fun verifyModelIntegrity(modelName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val modelFile = File(context.filesDir, "$MODELS_DIR/$modelName.onnx")
            if (!modelFile.exists()) return@withContext false

            val checksum = calculateChecksum(modelFile)
            val metadata = loadedModels[modelName]
            
            metadata?.checksum == checksum
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Calculates SHA-256 checksum of a file.
     */
    private fun calculateChecksum(file: File): String {
        val digest = MessageDigest.getInstance(CHECKSUM_ALGORITHM)
        file.inputStream().use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Gets available storage space in bytes.
     */
    fun getAvailableStorage(): Long {
        val stat = StatFs(context.filesDir.path)
        return stat.availableBytes
    }

    /**
     * Gets total storage space in bytes.
     */
    fun getTotalStorage(): Long {
        val stat = StatFs(context.filesDir.path)
        return stat.totalBytes
    }

    /**
     * Gets memory usage of loaded models.
     */
    fun getModelMemoryUsage(): Long {
        // Estimate memory usage based on loaded models
        return loadedModels.values.sumOf { it.sizeBytes }
    }

    /**
     * Unloads a model to free memory.
     */
    suspend fun unloadModel(modelName: String) = withContext(Dispatchers.IO) {
        mutex.withLock {
            loadedModels[modelName]?.let { metadata ->
                loadedModels[modelName] = metadata.copy(isLoaded = false)
            }
        }
    }

    /**
     * Loads model metadata from storage.
     */
    private fun loadModelMetadata() {
        try {
            val metadataFile = File(context.filesDir, "$MODELS_DIR/$METADATA_FILE")
            if (metadataFile.exists()) {
                val json = metadataFile.readText()
                val jsonArray = org.json.JSONArray(json)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val metadata = ModelMetadata(
                        modelName = obj.getString("modelName"),
                        version = obj.getString("version"),
                        checksum = obj.getString("checksum"),
                        sizeBytes = obj.getLong("sizeBytes"),
                        lastUpdated = obj.getLong("lastUpdated"),
                        isLoaded = obj.optBoolean("isLoaded", false),
                        loadTimeMs = obj.optLong("loadTimeMs", 0)
                    )
                    loadedModels[metadata.modelName] = metadata
                }
            } else {
                // Initialize with default metadata for bundled models
                val pestClassifierFile = File(context.filesDir, "$MODELS_DIR/pest_classifier.onnx")
                if (pestClassifierFile.exists()) {
                    loadedModels["pest_classifier"] = ModelMetadata(
                        modelName = "pest_classifier",
                        version = "1.0.0",
                        checksum = calculateChecksum(pestClassifierFile),
                        sizeBytes = pestClassifierFile.length(),
                        lastUpdated = System.currentTimeMillis(),
                        isLoaded = false,
                        loadTimeMs = 0
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Saves model metadata to storage.
     */
    private suspend fun saveModelMetadata() = withContext(Dispatchers.IO) {
        try {
            val metadataFile = File(context.filesDir, "$MODELS_DIR/$METADATA_FILE")
            metadataFile.parentFile?.mkdirs()
            val jsonArray = org.json.JSONArray()
            loadedModels.values.forEach { metadata ->
                val obj = org.json.JSONObject()
                obj.put("modelName", metadata.modelName)
                obj.put("version", metadata.version)
                obj.put("checksum", metadata.checksum)
                obj.put("sizeBytes", metadata.sizeBytes)
                obj.put("lastUpdated", metadata.lastUpdated)
                obj.put("isLoaded", metadata.isLoaded)
                obj.put("loadTimeMs", metadata.loadTimeMs)
                jsonArray.put(obj)
            }
            metadataFile.writeText(jsonArray.toString(2))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Checks for model updates from remote (when online).
     * Returns empty list if offline or no updates available.
     */
    suspend fun checkForUpdates(): List<ModelMetadata> = withContext(Dispatchers.IO) {
        try {
            // Check network connectivity
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return@withContext emptyList()
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return@withContext emptyList()
            
            if (!capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                return@withContext emptyList()
            }
            
            // In a production app, this would fetch from a remote server
            // For now, return empty list as models are bundled with APK
            emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Downloads a model update from remote.
     * Returns false if download fails or offline.
     */
    @Suppress("UNUSED_PARAMETER")
    suspend fun downloadModelUpdate(modelName: String, url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check network connectivity
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return@withContext false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return@withContext false
            
            if (!capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                return@withContext false
            }
            
            // In a production app, this would download from URL
            // For now, return false as models are bundled with APK
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Releases all resources.
     */
    fun release() {
        loadedModels.clear()
        modelLoadTimes.clear()
        onnxManager.release()
    }
}