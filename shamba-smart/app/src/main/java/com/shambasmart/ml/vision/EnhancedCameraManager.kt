package com.shambasmart.ml.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.YuvImage
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Enhanced CameraX manager optimized for ML inference.
 * Features:
 * - Frame throttling (1-5 FPS for inference)
 * - Flash control for consistent lighting
 * - Image stabilization hints
 * - Manual focus for close-up pest shots
 * - YUV to Bitmap conversion for ONNX
 */
@Singleton
class EnhancedCameraManager @Inject constructor(
    private val context: Context
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    // Frame throttling
    private val isProcessing = AtomicBoolean(false)
    private var targetFps = 2 // Default 2 FPS for inference
    private var lastFrameTime = 0L

    // Frame flow for inference
    private val _frameFlow = MutableSharedFlow<Bitmap>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val frameFlow: SharedFlow<Bitmap> = _frameFlow

    data class CameraConfig(
        val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
        val targetResolution: android.util.Size = android.util.Size(640, 480),
        val inferenceFps: Int = 2,
        val enableFlash: Boolean = false,
        val enableStabilization: Boolean = true,
        val manualFocusEnabled: Boolean = true
    )

    /**
     * Sets up CameraX with enhanced configuration for ML inference.
     */
    suspend fun setupCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        config: CameraConfig = CameraConfig()
    ): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProvider = suspendCancellableCoroutine { continuation ->
                cameraProviderFuture.addListener({
                    continuation.resume(cameraProviderFuture.get())
                }, ContextCompat.getMainExecutor(context))
            }

            targetFps = config.inferenceFps

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setFlashMode(
                    if (config.enableFlash) ImageCapture.FLASH_MODE_ON
                    else ImageCapture.FLASH_MODE_OFF
                )
                .build()

            imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(config.targetResolution)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        processFrameWithThrottling(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(config.lensFacing)
                .build()

            cameraProvider?.unbindAll()
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalysis
            )

            // Enable image stabilization if supported
            if (config.enableStabilization) {
                enableImageStabilization()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Processes frames with throttling to maintain target FPS.
     */
    private fun processFrameWithThrottling(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        val frameInterval = 1000L / targetFps

        if (currentTime - lastFrameTime >= frameInterval && isProcessing.compareAndSet(false, true)) {
            lastFrameTime = currentTime
            try {
                val bitmap = imageProxyToBitmap(imageProxy)
                _frameFlow.tryEmit(bitmap)
            } finally {
                isProcessing.set(false)
                imageProxy.close()
            }
        } else {
            imageProxy.close()
        }
    }

    /**
     * Captures a single image for analysis.
     */
    suspend fun captureImage(): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            val capture = imageCapture ?: return@withContext Result.failure(
                IllegalStateException("Camera not initialized")
            )

            suspendCancellableCoroutine { continuation ->
                capture.takePicture(
                    cameraExecutor,
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            try {
                                val bitmap = imageProxyToBitmap(image)
                                continuation.resume(Result.success(bitmap))
                            } catch (e: Exception) {
                                continuation.resume(Result.failure(e))
                            } finally {
                                image.close()
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            continuation.resume(Result.failure(exception))
                        }
                    }
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sets manual focus point for close-up pest shots.
     */
    fun setManualFocus(x: Float, y: Float) {
        camera?.let { cam ->
            val factory = cam.cameraControl.createMeteringPointFactory(
                camera!!.cameraInfo
            )
            val point = factory.createPoint(x, y)
            val action = FocusMeteringAction.Builder(point)
                .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            cam.cameraControl.startFocusAndMetering(action)
        }
    }

    /**
     * Toggles flash mode.
     */
    fun toggleFlash(enable: Boolean) {
        imageCapture?.flashMode = if (enable) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
    }

    /**
     * Sets target inference FPS.
     */
    fun setInferenceFps(fps: Int) {
        targetFps = fps.coerceIn(1, 5)
    }

    /**
     * Enables image stabilization if camera supports it.
     */
    private fun enableImageStabilization() {
        camera?.cameraInfo?.let { cameraInfo ->
            if (cameraInfo.isImageStabilizationSupported) {
                camera?.cameraControl?.enableImageStabilization(true)
            }
        }
    }

    /**
     * Converts YUV ImageProxy to Bitmap for ONNX inference.
     */
    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val nv21 = yuv420888ToNv21(image)
        val yuvImage = YuvImage(
            nv21,
            ImageFormat.NV21,
            image.width,
            image.height,
            null
        )

        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            android.graphics.Rect(0, 0, image.width, image.height),
            90,
            out
        )

        val imageBytes = out.toByteArray()
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        // Rotate bitmap based on image rotation
        val rotationDegrees = image.imageInfo.rotationDegrees
        return if (rotationDegrees != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }

    /**
     * Converts YUV_420_888 to NV21 format.
     */
    private fun yuv420888ToNv21(image: ImageProxy): ByteArray {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // Y plane
        yBuffer.get(nv21, 0, ySize)

        // VU plane (NV21 format: Y + VU interleaved)
        val uvPixelStride = image.planes[1].pixelStride
        val uvRowStride = image.planes[1].rowStride
        val uvWidth = image.width / 2
        val uvHeight = image.height / 2

        var pos = ySize
        if (uvPixelStride == 2) {
            // UV planes are interleaved
            for (row in 0 until uvHeight) {
                for (col in 0 until uvWidth) {
                    val vuIndex = row * uvRowStride + col * uvPixelStride
                    nv21[pos++] = vBuffer.get(vuIndex)
                    nv21[pos++] = uBuffer.get(vuIndex)
                }
            }
        } else {
            // UV planes are separate
            vBuffer.position(0)
            uBuffer.position(0)
            for (i in 0 until vSize) {
                nv21[pos++] = vBuffer.get()
            }
            for (i in 0 until uSize) {
                nv21[pos++] = uBuffer.get()
            }
        }

        return nv21
    }

    /**
     * Releases all camera resources.
     */
    fun release() {
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
    }
}