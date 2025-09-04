package com.thirdeyetimer.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Size
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.core.ImageCaptureException
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.*
import kotlin.math.*
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SimpleHeartRateActivity : AppCompatActivity() {
    
    private lateinit var statusText: TextView
    private lateinit var heartRateText: TextView
    private lateinit var skipButton: Button
    private lateinit var startButton: Button
    private lateinit var previewView: PreviewView
    
    // Advanced PPG signal processing
    private val ppgProcessor = PPGProcessor()
    
    private var measurementStartTime = 0L
    private var isMeasuring = false
    private var measurementJob: Job? = null
    
    private var measuredHeartRate = 0
    
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    
    companion object {
        private const val TAG = "AdvancedHeartRate"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val MEASUREMENT_DURATION = 30000L // 30 seconds for better accuracy with new algorithm
        private const val DEBUG_MODE = true
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_heart_rate)
        
        statusText = findViewById(R.id.status_text)
        heartRateText = findViewById(R.id.heart_rate_text)
        skipButton = findViewById(R.id.skip_button)
        startButton = findViewById(R.id.start_button)
        previewView = findViewById(R.id.preview_view)
        
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        // Check if this is an end measurement
        val isEndMeasurement = intent.getBooleanExtra("is_end_measurement", false)
        if (isEndMeasurement) {
            statusText.text = "Great! Now let's see how much you've relaxed"
            startButton.text = "Measure Heart Rate"
        }
        
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            try {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            } catch (e: Exception) {
                Log.e(TAG, "Error requesting camera permissions: ${e.message}", e)
                statusText.text = "📷 Camera access needed to measure your heart rate"
                heartRateText.text = "Heart Rate: 0 BPM"
            }
        }
        
        startButton.setOnClickListener {
            if (!isMeasuring) {
                val algorithmStatus = if (imageCapture != null && camera != null) {
                    "📷 Real Camera PPG Algorithm Ready"
                } else {
                    "❌ No Camera Available"
                }
                Log.d(TAG, algorithmStatus)
                startMeasurement()
            }
        }
        
        skipButton.setOnClickListener {
            skipMeasurement()
        }
    }
    
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun startCamera() {
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
            
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    
                    // Optimized ImageCapture for PPG
                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetResolution(Size(640, 480))
                        .build()
                    
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    
                    cameraProvider.unbindAll()
                    
                    camera = cameraProvider.bindToLifecycle(
                        this as LifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                    
                    // Enable torch for PPG measurement
                    try {
                        camera?.cameraControl?.enableTorch(true)
                        Log.d(TAG, "Torch enabled successfully")
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not enable torch: ${e.message}")
                    }

                    // Set exposure compensation to balanced value
                    try {
                        val exposureState = camera?.cameraInfo?.exposureState
                        if (exposureState?.isExposureCompensationSupported == true) {
                            val range = exposureState.exposureCompensationRange
                            // Set to lower value to avoid saturation
                            val lowExposure = range.lower + (range.upper - range.lower) / 4
                            camera?.cameraControl?.setExposureCompensationIndex(lowExposure)
                            Log.d(TAG, "Exposure set to low: $lowExposure (range: ${range.lower} to ${range.upper})")
                        } else {
                            Log.w(TAG, "Exposure compensation not supported")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not set exposure: ${e.message}")
                    }
                    
                    statusText.text = "📷 Ready! Gently place your finger over the camera lens"
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting camera: ${e.message}", e)
                    statusText.text = "📱 Camera not available on this device"
                    heartRateText.text = "Heart Rate: 0 BPM"
                }
            }, ContextCompat.getMainExecutor(this))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting camera provider: ${e.message}", e)
            statusText.text = "📱 Camera not available on this device"
            heartRateText.text = "Heart Rate: 0 BPM"
        }
    }
    
    private fun startMeasurement() {
        isMeasuring = true
        measurementStartTime = System.currentTimeMillis()
        
        // Clear all signal data
        ppgProcessor.clear()
        
                    statusText.text = "💓 Measuring your heart rate... Keep your finger still"
        startButton.visibility = View.GONE
        skipButton.visibility = View.GONE
        
        measurementJob = CoroutineScope(Dispatchers.Default).launch {
            while (isMeasuring && System.currentTimeMillis() - measurementStartTime < MEASUREMENT_DURATION) {
                if (imageCapture != null && camera != null) {
                    captureImageForHeartRate()
                } else {
                    Log.w(TAG, "Camera not available - cannot measure heart rate")
                    // Don't simulate - just wait and show zero
                }
                kotlinx.coroutines.delay(100) // 10 Hz sampling
                
                withContext(Dispatchers.Main) {
                    updateHeartRateDisplay()
                }
            }
            
            if (isMeasuring) {
                withContext(Dispatchers.Main) {
                    finishMeasurement()
                }
            }
        }
    }
    
    private fun captureImageForHeartRate() {
        val imageCapture = imageCapture ?: return
        
        try {
            imageCapture.takePicture(
                cameraExecutor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        try {
                            processImageForAdvancedHeartRate(image)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing image: ${e.message}")
                        } finally {
                            image.close()
                        }
                    }
                    
                    override fun onError(exception: ImageCaptureException) {
                        Log.e(TAG, "Image capture failed: ${exception.message}")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error taking picture: ${e.message}")
        }
    }
    
    private fun processImageForAdvancedHeartRate(image: ImageProxy) {
        try {
            val bitmap = imageProxyToBitmap(image)
            val rgbValues = calculateRGBValues(bitmap)
            
            // Process with improved PPG processor using green channel focus
            val result = ppgProcessor.addSample(rgbValues.first, rgbValues.second, rgbValues.third)
            
            Log.d(TAG, "Advanced PPG: R=${String.format("%.1f", rgbValues.first)}, G=${String.format("%.1f", rgbValues.second)}, B=${String.format("%.1f", rgbValues.third)}, quality=${String.format("%.2f", result.quality)}, samples=${result.samples}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image: ${e.message}")
        }
    }
    
    private fun calculateRGBValues(bitmap: Bitmap): Triple<Double, Double, Double> {
        try {
            // Multi-region RGB analysis for better signal quality
            val centerX = bitmap.width / 2
            val centerY = bitmap.height / 2
            val regionSize = minOf(bitmap.width, bitmap.height) / 3
            
            val regions = listOf(
                Triple(centerX - regionSize/2, centerY - regionSize/2, 0.5),
                // Top region
                Triple(centerX - regionSize/2, centerY - regionSize, 0.2),
                // Bottom region
                Triple(centerX - regionSize/2, centerY + regionSize/2, 0.2),
                // Left region
                Triple(centerX - regionSize, centerY - regionSize/2, 0.05),
                // Right region
                Triple(centerX + regionSize/2, centerY - regionSize/2, 0.05)
            )
            
            var totalRed = 0.0
            var totalGreen = 0.0
            var totalBlue = 0.0
            var totalWeight = 0.0
            
            for ((startX, startY, weight) in regions) {
                val endX = minOf(bitmap.width, startX + regionSize)
                val endY = minOf(bitmap.height, startY + regionSize)
                val actualStartX = maxOf(0, startX)
                val actualStartY = maxOf(0, startY)
                
                var regionRed = 0.0
                var regionGreen = 0.0
                var regionBlue = 0.0
                var pixelCount = 0
                
                for (y in actualStartY until endY) {
                    for (x in actualStartX until endX) {
                        val pixel = bitmap.getPixel(x, y)
                        val red = (pixel shr 16) and 0xFF
                        val green = (pixel shr 8) and 0xFF
                        val blue = pixel and 0xFF
                        
                        regionRed += red
                        regionGreen += green
                        regionBlue += blue
                        pixelCount++
                    }
                }
                
                if (pixelCount > 0) {
                    totalRed += (regionRed / pixelCount) * weight
                    totalGreen += (regionGreen / pixelCount) * weight
                    totalBlue += (regionBlue / pixelCount) * weight
                    totalWeight += weight
                }
            }
            
            return if (totalWeight > 0) {
                Triple(
                    totalRed / totalWeight,
                    totalGreen / totalWeight,
                    totalBlue / totalWeight
                )
            } else {
                Triple(0.0, 0.0, 0.0)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating RGB values: ${e.message}")
            return Triple(0.0, 0.0, 0.0)
        }
    }
    
    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        try {
            when (image.format) {
                ImageFormat.YUV_420_888 -> {
                    val yBuffer = image.planes[0].buffer
                    val uBuffer = image.planes[1].buffer
                    val vBuffer = image.planes[2].buffer
                    
                    val ySize = yBuffer.remaining()
                    val uSize = uBuffer.remaining()
                    val vSize = vBuffer.remaining()
                    
                    val nv21 = ByteArray(ySize + uSize + vSize)
                    
                    yBuffer.get(nv21, 0, ySize)
                    vBuffer.get(nv21, ySize, vSize)
                    uBuffer.get(nv21, ySize + vSize, uSize)
                    
                    val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
                    val out = ByteArrayOutputStream()
                    yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
                    val imageBytes = out.toByteArray()
                    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                }
                ImageFormat.JPEG -> {
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
                else -> {
                    Log.w(TAG, "Unsupported image format: ${image.format}")
                    return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting image to bitmap: ${e.message}")
            return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        }
    }
    
    private fun updateHeartRateDisplay() {
        val elapsed = System.currentTimeMillis() - measurementStartTime
        val progress = (elapsed / MEASUREMENT_DURATION.toDouble() * 100).toInt()
        
        // Get current results from PPG processor
        val stats = ppgProcessor.getSignalStats()
        val currentBPM = if (stats.heartRateHistory.isNotEmpty()) stats.heartRateHistory.average().toInt() else 0
        
        heartRateText.text = "Heart Rate: $currentBPM BPM"
        
        val mode = if (imageCapture != null && camera != null) "📷 Efficient PPG" else "❌ No Camera Available"
        val qualityInfo = if (DEBUG_MODE && imageCapture != null && camera != null) {
            " | Signal: ${String.format("%.1f", stats.quality * 100)}%"
        } else ""
        
                    val statusMessage = when {
            currentBPM > 0 -> "💓 Found heartbeat! Keep still ($progress%)$qualityInfo"
            stats.quality > 0.3 -> "💓 Signal improving... Stay still ($progress%)$qualityInfo"
            else -> "💓 Measuring... Keep finger still ($progress%)$qualityInfo"
        }
        statusText.text = statusMessage
    }
    

    
    private fun finishMeasurement() {
        isMeasuring = false
        measurementJob?.cancel()
        
        // Calculate final heart rate only if we have real data
        val stats = ppgProcessor.getSignalStats()
        if (stats.samples > 0 && imageCapture != null && camera != null) {
            measuredHeartRate = if (stats.heartRateHistory.isNotEmpty()) stats.heartRateHistory.average().toInt() else 0
        } else {
            // No real camera data available
            measuredHeartRate = 0
        }
        
        statusText.text = if (imageCapture != null && camera != null) "✅ Measurement complete!" else "📱 Camera not available"
        heartRateText.text = "Heart Rate: $measuredHeartRate BPM"
        
        val intent = Intent().apply {
            putExtra("start_heart_rate", measuredHeartRate)
            putExtra("start_hrv", 0.0) // Keep for compatibility but set to 0
            putExtra("measurement_completed", true)
            putExtra("is_end_measurement", intent.getBooleanExtra("is_end_measurement", false))
        }
        setResult(RESULT_OK, intent)
        
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 2000)
    }
    
    private fun skipMeasurement() {
        val intent = Intent().apply {
            putExtra("measurement_completed", false)
            putExtra("is_end_measurement", this@SimpleHeartRateActivity.intent.getBooleanExtra("is_end_measurement", false))
        }
        setResult(RESULT_OK, intent)
        finish()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Log.e(TAG, "Permissions not granted by the user.")
                statusText.text = "📷 Please allow camera access to measure heart rate"
                heartRateText.text = "Heart Rate: 0 BPM"
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        measurementJob?.cancel()
        cameraExecutor.shutdown()
        camera?.cameraControl?.enableTorch(false)
    }
} 