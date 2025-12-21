package com.thirdeyetimer.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.thirdeyetimer.app.domain.AuraSystem
import com.thirdeyetimer.app.ui.components.*
import com.thirdeyetimer.app.ui.theme.CosmicColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

/**
 * AuraSelfieScreen
 * 
 * The main "Vibe Check" experience - capture a selfie and overlay a glowing aura.
 * Designed for maximum shareability and viral potential.
 */
@Composable
fun AuraSelfieScreen(
    onBackClick: () -> Unit,
    onShareClick: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    // State
    var hasPermission by remember { mutableStateOf(checkCameraPermission(context)) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var auraReading by remember { mutableStateOf<AuraSystem.AuraReading?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var processedImageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Camera setup
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }
    
    // Request permission on launch
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(CosmicColors.BackgroundGradient)
            )
    ) {
        // Background particles
        ParticleBackground(particleCount = 30)
        
        when {
            errorMessage != null -> {
                // Error state
                ErrorContent(
                    message = errorMessage!!,
                    onRetryClick = {
                        errorMessage = null
                        isCapturing = false
                        isProcessing = false
                        showResult = false
                    },
                    onBackClick = onBackClick
                )
            }
            !hasPermission -> {
                // Permission denied state
                PermissionDeniedContent(
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onBackClick = onBackClick
                )
            }
            showResult && processedImageUri != null && auraReading != null -> {
                // Show result with aura overlay
                AuraResultContent(
                    imageUri = processedImageUri!!,
                    auraReading = auraReading!!,
                    onRetakeClick = {
                        showResult = false
                        capturedImageUri = null
                        capturedBitmap = null
                        auraReading = null
                        processedImageUri = null
                    },
                    onShareClick = { onShareClick(processedImageUri!!) },
                    onBackClick = onBackClick
                )
            }
            isProcessing -> {
                // Processing state with animation
                ProcessingContent(auraReading = auraReading)
            }
            else -> {
                // Camera capture mode
                CameraCaptureContent(
                    onImageCapture = { capture -> imageCapture = capture },
                    onCaptureClick = {
                        isCapturing = true
                        errorMessage = null
                        scope.launch {
                            try {
                                captureImage(
                                    context = context,
                                    imageCapture = imageCapture,
                                    onImageCaptured = { uri, bitmap ->
                                        capturedImageUri = uri
                                        capturedBitmap = bitmap
                                        isCapturing = false
                                        isProcessing = true
                                        
                                        // Generate aura reading
                                        auraReading = AuraSystem.generateAuraReading()
                                        
                                        // Process image with aura overlay
                                        scope.launch {
                                            try {
                                                delay(1500) // Show processing animation
                                                val processed = processImageWithAuraSafe(
                                                    context = context,
                                                    originalBitmap = bitmap,
                                                    auraReading = auraReading!!
                                                )
                                                if (processed != null) {
                                                    processedImageUri = processed
                                                    isProcessing = false
                                                    showResult = true
                                                } else {
                                                    isProcessing = false
                                                    errorMessage = "Failed to process image. Please try again."
                                                }
                                            } catch (e: Exception) {
                                                Log.e("AuraSelfie", "Processing failed", e)
                                                isProcessing = false
                                                errorMessage = "Processing failed: ${e.message}"
                                            }
                                        }
                                    },
                                    onError = { error ->
                                        isCapturing = false
                                        errorMessage = "Capture failed: $error"
                                        Log.e("AuraSelfie", "Capture failed: $error")
                                    }
                                )
                            } catch (e: Exception) {
                                isCapturing = false
                                errorMessage = "Camera error: ${e.message}"
                                Log.e("AuraSelfie", "Camera error", e)
                            }
                        }
                    },
                    isCapturing = isCapturing,
                    onBackClick = onBackClick
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = CosmicColors.Error,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.headlineSmall,
            color = CosmicColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = CosmicColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        GlassmorphicButton(
            onClick = onRetryClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = CosmicColors.TextPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Again", color = CosmicColors.TextPrimary)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onBackClick) {
            Text("Go Back", color = CosmicColors.TextTertiary)
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    onRequestPermission: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = CosmicColors.TextMuted,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Camera Access Needed",
            style = MaterialTheme.typography.headlineSmall,
            color = CosmicColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "To check your aura, we need access to your camera for the selfie capture.",
            style = MaterialTheme.typography.bodyMedium,
            color = CosmicColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        GlassmorphicButton(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grant Permission", color = CosmicColors.TextPrimary)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onBackClick) {
            Text("Go Back", color = CosmicColors.TextTertiary)
        }
    }
}

@Composable
private fun CameraCaptureContent(
    onImageCapture: (ImageCapture) -> Unit,
    onCaptureClick: () -> Unit,
    isCapturing: Boolean,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Pulsing animation for capture button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Scanning animation
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan"
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    
                    val imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    
                    onImageCapture(imageCapture)
                    
                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                    
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        Log.e("AuraSelfie", "Camera binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Aura scanning effect overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val maxRadius = size.minDimension * 0.4f
            
            // Draw scanning ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        CosmicColors.Primary.copy(alpha = 0.3f * (1 - scanProgress)),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = maxRadius * (0.5f + scanProgress * 0.5f)
                ),
                radius = maxRadius * (0.5f + scanProgress * 0.5f),
                center = Offset(centerX, centerY)
            )
        }
        
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(CosmicColors.GlassBackground, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = CosmicColors.TextPrimary
                )
            }
            
            Text(
                text = "✨ Vibe Check ✨",
                style = MaterialTheme.typography.titleMedium,
                color = CosmicColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.size(48.dp))
        }
        
        // Bottom controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Position your face in the circle",
                style = MaterialTheme.typography.bodyMedium,
                color = CosmicColors.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Capture button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(if (isCapturing) 0.9f else pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                CosmicColors.Primary,
                                CosmicColors.Secondary
                            )
                        )
                    )
                    .border(
                        width = 4.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                CosmicColors.Accent,
                                CosmicColors.Primary
                            )
                        ),
                        shape = CircleShape
                    )
                    .clickable(enabled = !isCapturing) { onCaptureClick() },
                contentAlignment = Alignment.Center
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        color = CosmicColors.TextPrimary,
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Capture",
                        tint = CosmicColors.TextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (isCapturing) "Reading your energy..." else "Tap to check your aura",
                style = MaterialTheme.typography.labelMedium,
                color = CosmicColors.TextTertiary
            )
        }
    }
}

@Composable
private fun ProcessingContent(auraReading: AuraSystem.AuraReading?) {
    val infiniteTransition = rememberInfiniteTransition(label = "processing")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated aura ring
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val auraColors = auraReading?.let { 
                    AuraSystem.getAuraGradient(it.auraType) 
                } ?: listOf(CosmicColors.Primary, CosmicColors.Secondary, Color.Transparent)
                
                // Draw rotating aura
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = auraColors + auraColors.first(),
                    ),
                    radius = size.minDimension / 2,
                    alpha = pulseAlpha
                )
            }
            
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = CosmicColors.TextPrimary,
                modifier = Modifier.size(60.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Analyzing Your Energy...",
            style = MaterialTheme.typography.headlineSmall,
            color = CosmicColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Detecting aura frequencies",
            style = MaterialTheme.typography.bodyMedium,
            color = CosmicColors.TextSecondary.copy(alpha = pulseAlpha)
        )
    }
}

@Composable
private fun AuraResultContent(
    imageUri: Uri,
    auraReading: AuraSystem.AuraReading,
    onRetakeClick: () -> Unit,
    onShareClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "auraGlow")
    
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = CosmicColors.TextPrimary
                )
            }
            
            Text(
                text = "Your Aura ✨",
                style = MaterialTheme.typography.titleMedium,
                color = CosmicColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.size(48.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Aura selfie with glow
        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Outer glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                auraReading.auraType.glowColor.copy(alpha = 0.3f * glowPulse),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            // The processed image
            AsyncImage(
                model = imageUri,
                contentDescription = "Your aura selfie",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Aura info card
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Aura type badge
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    auraReading.auraType.primaryColor,
                                    auraReading.auraType.secondaryColor
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = auraReading.auraType.displayName,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Energy level
                Text(
                    text = "Energy: ${auraReading.energyPercent}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = auraReading.auraType.primaryColor,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Meaning
                Text(
                    text = auraReading.auraType.meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CosmicColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Viral caption
                Text(
                    text = auraReading.caption,
                    style = MaterialTheme.typography.headlineSmall,
                    color = CosmicColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = auraReading.subCaption,
                    style = MaterialTheme.typography.bodySmall,
                    color = CosmicColors.TextTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Retake button
            OutlinedButton(
                onClick = onRetakeClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CosmicColors.TextSecondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retake")
            }
            
            // Share button (prominent)
            Button(
                onClick = onShareClick,
                modifier = Modifier.weight(2f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = auraReading.auraType.primaryColor
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Your Aura ✨", fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Helper functions

private fun checkCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

private fun captureImage(
    context: Context,
    imageCapture: ImageCapture?,
    onImageCaptured: (Uri, Bitmap) -> Unit,
    onError: (String) -> Unit
) {
    if (imageCapture == null) {
        onError("Camera not ready")
        return
    }
    
    val photoFile = File(
        context.cacheDir,
        "aura_selfie_${System.currentTimeMillis()}.jpg"
    )
    
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    
    imageCapture.takePicture(
        outputOptions,
        Executors.newSingleThreadExecutor(),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val uri = Uri.fromFile(photoFile)
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                if (bitmap != null) {
                    // Mirror the image for front camera
                    val matrix = Matrix().apply { preScale(-1f, 1f) }
                    val mirroredBitmap = Bitmap.createBitmap(
                        bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                    )
                    onImageCaptured(uri, mirroredBitmap)
                } else {
                    onError("Failed to decode image")
                }
            }
            
            override fun onError(exception: ImageCaptureException) {
                onError(exception.message ?: "Unknown error")
            }
        }
    )
}

private fun processImageWithAura(
    context: Context,
    originalBitmap: Bitmap,
    auraReading: AuraSystem.AuraReading
): Uri {
    // Create a square bitmap for better social sharing
    val size = minOf(originalBitmap.width, originalBitmap.height)
    val xOffset = (originalBitmap.width - size) / 2
    val yOffset = (originalBitmap.height - size) / 2
    
    val squareBitmap = Bitmap.createBitmap(originalBitmap, xOffset, yOffset, size, size)
    
    // Create result bitmap
    val resultBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(resultBitmap)
    
    // Draw original image
    canvas.drawBitmap(squareBitmap, 0f, 0f, null)
    
    // Draw aura overlay
    val centerX = size / 2f
    val centerY = size / 2f
    val radius = size * 0.8f
    
    // Inner aura glow
    val innerPaint = Paint().apply {
        shader = RadialGradient(
            centerX, centerY, radius * 0.6f,
            intArrayOf(
                auraReading.auraType.primaryColor.copy(alpha = 0.4f).toArgb(),
                auraReading.auraType.secondaryColor.copy(alpha = 0.2f).toArgb(),
                android.graphics.Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawCircle(centerX, centerY, radius * 0.6f, innerPaint)
    
    // Outer aura glow
    val outerPaint = Paint().apply {
        shader = RadialGradient(
            centerX, centerY, radius,
            intArrayOf(
                android.graphics.Color.TRANSPARENT,
                auraReading.auraType.glowColor.copy(alpha = 0.3f).toArgb(),
                auraReading.auraType.primaryColor.copy(alpha = 0.5f).toArgb()
            ),
            floatArrayOf(0.3f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawCircle(centerX, centerY, radius, outerPaint)
    
    // Add caption overlay at bottom
    val captionPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = size / 18f
        textAlign = Paint.Align.CENTER
        setShadowLayer(8f, 0f, 2f, android.graphics.Color.BLACK)
        isFakeBoldText = true
    }
    
    // Draw caption background
    val bgPaint = Paint().apply {
        color = android.graphics.Color.argb(150, 0, 0, 0)
    }
    canvas.drawRect(0f, size - size / 6f, size.toFloat(), size.toFloat(), bgPaint)
    
    // Draw caption text
    canvas.drawText(
        auraReading.caption,
        centerX,
        size - size / 12f,
        captionPaint
    )
    
    // Draw app watermark
    val watermarkPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = size / 30f
        textAlign = Paint.Align.CENTER
        alpha = 180
    }
    canvas.drawText(
        "Third Eye Timer ✨",
        centerX,
        size - size / 25f,
        watermarkPaint
    )
    
    // Save to file
    val outputFile = File(
        context.cacheDir,
        "aura_result_${System.currentTimeMillis()}.jpg"
    )
    FileOutputStream(outputFile).use { out ->
        resultBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
    }
    
    // Return content URI using FileProvider
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        outputFile
    )
}

/**
 * Safe version of processImageWithAura that catches exceptions and returns null on failure
 */
private fun processImageWithAuraSafe(
    context: Context,
    originalBitmap: Bitmap,
    auraReading: AuraSystem.AuraReading
): Uri? {
    return try {
        // Create a square bitmap for better social sharing
        val size = minOf(originalBitmap.width, originalBitmap.height)
        if (size <= 0) {
            Log.e("AuraSelfie", "Invalid bitmap size: $size")
            return null
        }
        
        val xOffset = (originalBitmap.width - size) / 2
        val yOffset = (originalBitmap.height - size) / 2
        
        val squareBitmap = Bitmap.createBitmap(originalBitmap, xOffset, yOffset, size, size)
        
        // Create result bitmap
        val resultBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        
        // Draw original image
        canvas.drawBitmap(squareBitmap, 0f, 0f, null)
        
        // Draw aura overlay
        val centerX = size / 2f
        val centerY = size / 2f
        val radius = size * 0.8f
        
        // Inner aura glow
        val innerPaint = Paint().apply {
            shader = RadialGradient(
                centerX, centerY, radius * 0.6f,
                intArrayOf(
                    auraReading.auraType.primaryColor.copy(alpha = 0.4f).toArgb(),
                    auraReading.auraType.secondaryColor.copy(alpha = 0.2f).toArgb(),
                    android.graphics.Color.TRANSPARENT
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(centerX, centerY, radius * 0.6f, innerPaint)
        
        // Outer aura glow
        val outerPaint = Paint().apply {
            shader = RadialGradient(
                centerX, centerY, radius,
                intArrayOf(
                    android.graphics.Color.TRANSPARENT,
                    auraReading.auraType.glowColor.copy(alpha = 0.3f).toArgb(),
                    auraReading.auraType.primaryColor.copy(alpha = 0.5f).toArgb()
                ),
                floatArrayOf(0.3f, 0.7f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(centerX, centerY, radius, outerPaint)
        
        // Add caption overlay at bottom
        val captionPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = size / 18f
            textAlign = Paint.Align.CENTER
            setShadowLayer(8f, 0f, 2f, android.graphics.Color.BLACK)
            isFakeBoldText = true
        }
        
        // Draw caption background
        val bgPaint = Paint().apply {
            color = android.graphics.Color.argb(150, 0, 0, 0)
        }
        canvas.drawRect(0f, size - size / 6f, size.toFloat(), size.toFloat(), bgPaint)
        
        // Draw caption text
        canvas.drawText(
            auraReading.caption,
            centerX,
            size - size / 12f,
            captionPaint
        )
        
        // Draw app watermark
        val watermarkPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = size / 30f
            textAlign = Paint.Align.CENTER
            alpha = 180
        }
        canvas.drawText(
            "Third Eye Timer",
            centerX,
            size - size / 25f,
            watermarkPaint
        )
        
        // Save to file
        val outputFile = File(
            context.cacheDir,
            "aura_result_${System.currentTimeMillis()}.jpg"
        )
        FileOutputStream(outputFile).use { out ->
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        
        // Return content URI using FileProvider
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            outputFile
        )
    } catch (e: Exception) {
        Log.e("AuraSelfie", "processImageWithAuraSafe failed", e)
        null
    }
}
