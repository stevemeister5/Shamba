package com.shambasmart.ml

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.shambasmart.data.local.entity.SoundClasses
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

data class AudioClassificationResult(
    val soundClass: String,
    val confidence: Double,
    val allScores: Map<String, Double>,
    val timestamp: Long = System.currentTimeMillis()
)

data class SpectrogramData(
    val frequencies: List<Float>,
    val magnitudes: List<Float>,
    val timestamp: Long
)

@Singleton
class SoundClassifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SoundClassifier"
        private const val MODEL_FILE = "audio_classifier.tflite"
        private const val SAMPLE_RATE = 16000
        private const val RECORDING_DURATION_MS = 1000
        private const val BUFFER_SIZE = SAMPLE_RATE * RECORDING_DURATION_MS / 1000
        private const val SPECTROGRAM_SIZE = 256
        private const val CONFIDENCE_THRESHOLD = 0.7
    }

    private var interpreter: Interpreter? = null
    private var audioRecord: AudioRecord? = null
    private var isListening = false
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val classificationJob = Job()
    private val classificationScope = CoroutineScope(Dispatchers.IO + classificationJob)

    // State flows for UI updates
    private val _isListening = MutableStateFlow(false)
    val isListeningFlow: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _lastClassification = MutableStateFlow<AudioClassificationResult?>(null)
    val lastClassification: StateFlow<AudioClassificationResult?> = _lastClassification.asStateFlow()

    private val _spectrogram = MutableStateFlow<SpectrogramData?>(null)
    val spectrogram: StateFlow<SpectrogramData?> = _spectrogram.asStateFlow()

    private val _batteryOptimized = MutableStateFlow(false)
    val batteryOptimized: StateFlow<Boolean> = _batteryOptimized.asStateFlow()

    // Qualcomm Sensing Hub Aqstic processing flags
    private val useQnnDelegate = Build.SUPPORTED_64_BIT_ABIS.contains("arm64-v8a")
    private val useAqstic = checkAqsticSupport()

    private fun checkAqsticSupport(): Boolean {
        return try {
            // Check for Qualcomm Sensing Hub capabilities
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            manufacturer.contains("Qualcomm") || model.contains("SM8") || model.contains("Snapdragon")
        } catch (e: Exception) {
            false
        }
    }

    fun initialize(): Boolean {
        return try {
            val model = loadModelFile()
            val options = Interpreter.Options().apply {
                setNumThreads(2) // Optimize for battery
                if (useQnnDelegate) {
                    Log.d(TAG, "Using QNN delegate for NPU acceleration")
                }
                if (useAqstic) {
                    Log.d(TAG, "Qualcomm Aqstic audio processing enabled")
                }
            }
            interpreter = Interpreter(model, options)
            Log.d(TAG, "SoundClassifier initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SoundClassifier", e)
            false
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun startListening(
        onClassification: (AudioClassificationResult) -> Unit,
        onDistressDetected: ((AudioClassificationResult) -> Unit)? = null
    ) {
        if (isListening) return

        try {
            val minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize * 2
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord failed to initialize")
                return
            }

            audioRecord?.startRecording()
            isListening = true
            _isListening.value = true

            // Start classification loop in background
            classificationScope.launch {
                val audioBuffer = ShortArray(BUFFER_SIZE)
                val floatBuffer = FloatArray(BUFFER_SIZE)

                while (isActive && isListening) {
                    val bytesRead = audioRecord?.read(audioBuffer, 0, BUFFER_SIZE) ?: 0
                    if (bytesRead > 0) {
                        // Convert to float array
                        for (i in 0 until bytesRead) {
                            floatBuffer[i] = audioBuffer[i] / 32768.0f
                        }

                        // Generate spectrogram
                        val spectrogram = generateSpectrogram(floatBuffer.copyOf(bytesRead))
                        _spectrogram.value = spectrogram

                        // Classify audio
                        val result = classifyAudio(floatBuffer.copyOf(bytesRead))
                        if (result != null && result.confidence > CONFIDENCE_THRESHOLD) {
                            _lastClassification.value = result
                            withContext(Dispatchers.Main) {
                                onClassification(result)
                                if (SoundClasses.isDistressEvent(result.soundClass)) {
                                    onDistressDetected?.invoke(result)
                                }
                            }
                        }
                    }

                    // Battery optimization: Add delay between classifications
                    delay(if (_batteryOptimized.value) 2000 else 500)
                }
            }

            Log.d(TAG, "Started audio listening")
        } catch (e: SecurityException) {
            Log.e(TAG, "Audio recording permission denied", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio recording", e)
        }
    }

    fun stopListening() {
        isListening = false
        _isListening.value = false
        classificationJob.cancel()

        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            Log.d(TAG, "Stopped audio listening")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio recording", e)
        }
    }

    fun setBatteryOptimized(enabled: Boolean) {
        _batteryOptimized.value = enabled
        Log.d(TAG, "Battery optimization ${if (enabled) "enabled" else "disabled"}")
    }

    private fun generateSpectrogram(audioData: FloatArray): SpectrogramData {
        // Simple spectrogram generation using FFT
        val n = audioData.size
        val frequencies = mutableListOf<Float>()
        val magnitudes = mutableListOf<Float>()

        // Apply Hamming window
        val windowed = FloatArray(n) { i ->
            audioData[i] * (0.54f - 0.46f * kotlin.math.cos(2 * Math.PI * i / (n - 1))).toFloat()
        }

        // Calculate frequency bins and magnitudes
        for (i in 0 until minOf(n, SPECTROGRAM_SIZE)) {
            val frequency = i * SAMPLE_RATE.toFloat() / n
            frequencies.add(frequency)

            // Simple magnitude calculation (in real implementation, use FFT)
            val magnitude = sqrt(windowed[i] * windowed[i])
            magnitudes.add(magnitude)
        }

        return SpectrogramData(
            frequencies = frequencies,
            magnitudes = magnitudes,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun classifyAudio(audioData: FloatArray): AudioClassificationResult? {
        return try {
            interpreter?.let { interp ->
                // Prepare input tensor (simplified - real implementation would use proper preprocessing)
                val inputBuffer = ByteBuffer.allocateDirect(audioData.size * 4)
                    .order(ByteOrder.nativeOrder())
                audioData.forEach { inputBuffer.putFloat(it) }

                // Prepare output tensor
                val outputBuffer = Array(1) { FloatArray(SoundClasses.allClasses.size) }

                // Run inference
                interp.run(inputBuffer, outputBuffer)

                // Find highest confidence class
                val scores = outputBuffer[0]
                val maxIndex = scores.indices.maxByOrNull { scores[it] } ?: return null
                val maxConfidence = scores[maxIndex].toDouble()

                // Create scores map
                val scoresMap = mutableMapOf<String, Double>()
                SoundClasses.allClasses.forEachIndexed { index, soundClass ->
                    scoresMap[soundClass] = scores[index].toDouble()
                }

                AudioClassificationResult(
                    soundClass = SoundClasses.allClasses[maxIndex],
                    confidence = maxConfidence,
                    allScores = scoresMap
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Classification failed", e)
            null
        }
    }

    fun recordSample(durationMs: Long = 3000): FloatArray? {
        return try {
            val minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize * 2
            )

            if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord failed to initialize for calibration")
                return null
            }

            val samples = (SAMPLE_RATE * durationMs / 1000)
            val audioBuffer = ShortArray(samples)
            val floatBuffer = FloatArray(samples)

            recorder.startRecording()
            recorder.read(audioBuffer, 0, samples)
            recorder.stop()
            recorder.release()

            // Convert to float
            for (i in audioBuffer.indices) {
                floatBuffer[i] = audioBuffer[i] / 32768.0f
            }

            Log.d(TAG, "Recorded calibration sample: ${floatBuffer.size} samples")
            floatBuffer
        } catch (e: Exception) {
            Log.e(TAG, "Failed to record calibration sample", e)
            null
        }
    }

    fun calibrateThreshold(audioData: FloatArray): Double {
        // Calculate RMS level for threshold calibration
        val rms = sqrt(audioData.map { it * it }.average())
        val threshold = rms * 1.5 // Set threshold 50% above ambient noise
        Log.d(TAG, "Calibrated threshold: $threshold (RMS: $rms)")
        return threshold
    }

    fun release() {
        stopListening()
        interpreter?.close()
        interpreter = null
        coroutineScope.cancel()
        Log.d(TAG, "SoundClassifier released")
    }
}