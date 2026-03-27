package com.shambasmart.ml.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.YuvImage
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class CameraManager @Inject constructor(
    private val context: Context
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    data class CameraConfig(
        val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
        val targetResolution: android.util.Size = android.util.Size(640, 480)
    )

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

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(config.targetResolution)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(config.lensFacing)
                .build()

            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalysis
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

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

    fun release() {
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
    }
}