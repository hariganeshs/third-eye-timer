package com.thirdeyetimer.app.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.thirdeyetimer.app.domain.QuestManager
import com.thirdeyetimer.app.utils.AudioUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.pow
import kotlin.math.sqrt
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import kotlin.random.Random

// ============================================================================
// VIRAL SHATTERED SCREEN DATA CLASSES
// ============================================================================

/**
 * Glass shard with physics properties for explosive animation
 */
private data class GlassShard(
    var x: Float,
    var y: Float,
    var rotation: Float,
    var velocityX: Float,
    var velocityY: Float,
    var rotationSpeed: Float,
    var scale: Float,
    var alpha: Float,
    val color: Color,
    val vertices: List<Offset> // Triangle/polygon vertices relative to center
)

/**
 * Energy particle for burst effect
 */
private data class EnergyParticle(
    var x: Float,
    var y: Float,
    var radius: Float,
    var velocityX: Float,
    var velocityY: Float,
    var alpha: Float,
    val color: Color,
    val trail: MutableList<Offset> = mutableListOf()
)

/**
 * Zen ripple expanding from center
 */
private data class ZenRipple(
    var radius: Float,
    var alpha: Float,
    val maxRadius: Float,
    val speed: Float
)

// ============================================================================
// SHARD & PARTICLE GENERATION
// ============================================================================

/**
 * Creates a random glass shard with crystalline shape
 */
private fun createGlassShard(centerX: Float, centerY: Float): GlassShard {
    val angle = Random.nextFloat() * 360f
    val distance = Random.nextFloat() * 50f + 10f
    val radians = Math.toRadians(angle.toDouble())
    
    // Shard starts near center
    val startX = centerX + cos(radians).toFloat() * distance
    val startY = centerY + sin(radians).toFloat() * distance
    
    // Velocity outward from center
    val speed = Random.nextFloat() * 15f + 8f
    val velocityX = cos(radians).toFloat() * speed
    val velocityY = sin(radians).toFloat() * speed
    
    // Create crystalline triangle or quad vertices
    val numVertices = if (Random.nextBoolean()) 3 else 4
    val size = Random.nextFloat() * 40f + 20f
    val vertices = List(numVertices) { i ->
        val vertexAngle = (360f / numVertices) * i + Random.nextFloat() * 30f
        val vertexRadius = size * (0.7f + Random.nextFloat() * 0.3f)
        val rad = Math.toRadians(vertexAngle.toDouble())
        Offset(
            cos(rad).toFloat() * vertexRadius,
            sin(rad).toFloat() * vertexRadius
        )
    }
    
    // Glass colors - teal, cyan, white highlights
    val colors = listOf(
        Color(0xFF00E5FF), // Cyan
        Color(0xFF18FFFF), // Bright Cyan
        Color(0xFF64FFDA), // Teal
        Color(0xFFE0F7FA), // Light Ice
        Color(0xFFB2EBF2), // Pale Cyan
        Color(0xFFFFFFFF).copy(alpha = 0.9f) // White highlight
    )
    
    return GlassShard(
        x = startX,
        y = startY,
        rotation = Random.nextFloat() * 360f,
        velocityX = velocityX,
        velocityY = velocityY,
        rotationSpeed = (Random.nextFloat() - 0.5f) * 20f,
        scale = Random.nextFloat() * 0.5f + 0.5f,
        alpha = 1f,
        color = colors.random(),
        vertices = vertices
    )
}

/**
 * Creates an energy particle for the burst effect
 */
private fun createEnergyParticle(centerX: Float, centerY: Float): EnergyParticle {
    val angle = Random.nextFloat() * 360f
    val radians = Math.toRadians(angle.toDouble())
    val speed = Random.nextFloat() * 12f + 3f
    
    // Gradient colors: purple -> magenta -> cyan -> white
    val colors = listOf(
        Color(0xFFDD22FF), // Purple
        Color(0xFFFF22DD), // Magenta
        Color(0xFF22DDFF), // Cyan
        Color(0xFFFFFFFF), // White
        Color(0xFF9B59B6), // Deep Purple
        Color(0xFFE91E63), // Pink
        Color(0xFF00BCD4)  // Teal
    )
    
    return EnergyParticle(
        x = centerX,
        y = centerY,
        radius = Random.nextFloat() * 4f + 2f,
        velocityX = cos(radians).toFloat() * speed,
        velocityY = sin(radians).toFloat() * speed,
        alpha = 1f,
        color = colors.random()
    )
}

@Composable
fun ScreamJarScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val questManager = remember { QuestManager(context) }
    val audioUtils = remember { AudioUtils(context) }
    val scope = rememberCoroutineScope()
    
    // State
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var isRecording by remember { mutableStateOf(false) }
    var currentVolume by remember { mutableFloatStateOf(0f) } // 0 to 1
    var crackLevel by remember { mutableFloatStateOf(0f) } // 0 to 1 (shattered)
    var isShattered by remember { mutableStateOf(false) }
    
    // ========== VIRAL SHATTERED ANIMATION STATE ==========
    var glassShards by remember { mutableStateOf<List<GlassShard>>(emptyList()) }
    var energyParticles by remember { mutableStateOf<List<EnergyParticle>>(emptyList()) }
    var zenRipples by remember { mutableStateOf<List<ZenRipple>>(emptyList()) }
    var animationTime by remember { mutableFloatStateOf(0f) }
    var canvasCenter by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    var showPermissionDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasPermission = isGranted
        if (isGranted) {
            // Start automatically if just granted? Or wait for user action
        } else {
            showPermissionDialog = true
        }
    }
    
    // Crack paths for visualization
    val cracks = remember { mutableStateListOf<Path>() }
    
    // Logic loop for monitoring volume
    LaunchedEffect(isRecording) {
        if (isRecording) {
            if (audioUtils.startListening()) {
                // Small delay to let MediaRecorder initialize
                delay(100)
                
                while (isRecording && !isShattered) {
                    delay(50) // Update rate
                    
                    // Real devices produce amplitude values from 0-32767
                    // Normal talking: 500-2000, Loud voice: 2000-8000, Scream: 8000-30000+
                    val amplitude = audioUtils.getAmplitude()
                    
                    // Normalize for visual feedback (log scale for better UX)
                    val normalized = if (amplitude > 100) {
                        (kotlin.math.ln(amplitude.toDouble()) / kotlin.math.ln(32767.0)).coerceIn(0.0, 1.0).toFloat()
                    } else {
                        0f
                    }
                    
                    // Smooth visual volume
                    currentVolume = (currentVolume * 0.7f + normalized * 0.3f)
                    
                    // Production thresholds - require actual loud sounds
                    // Only add cracks if amplitude is above speaking volume
                    if (amplitude > 500) {
                        // Progress rate based on loudness
                        val progressRate = when {
                            amplitude > 15000 -> 0.025f  // Loud scream - shatter in ~2 seconds
                            amplitude > 8000 -> 0.015f   // Strong yell - shatter in ~3.5 seconds
                            amplitude > 3000 -> 0.008f   // Raised voice - shatter in ~6 seconds
                            amplitude > 1000 -> 0.004f   // Loud talking - shatter in ~12 seconds
                            else -> 0.002f               // Moderate sound - shatter in ~25 seconds
                        }
                        crackLevel += progressRate
                    }
                    
                    if (crackLevel >= 1.0f) {
                        isShattered = true
                        isRecording = false
                        audioUtils.stopListening()
                    }
                }
            } else {
                isRecording = false // Failed to start
                errorMessage = "Microphone unavailable. Please check settings."
            }
        } else {
            audioUtils.stopListening()
            currentVolume = 0f
        }
    }
    
    // ========== SHATTERED ANIMATION LOOP ==========
    LaunchedEffect(isShattered) {
        if (isShattered && canvasCenter != Offset.Zero) {
            // Initialize explosion!
            glassShards = List(45) { createGlassShard(canvasCenter.x, canvasCenter.y) }
            energyParticles = List(120) { createEnergyParticle(canvasCenter.x, canvasCenter.y) }
            zenRipples = listOf(
                ZenRipple(0f, 0.8f, canvasSize.minDimension * 0.8f, 4f),
                ZenRipple(0f, 0.6f, canvasSize.minDimension * 1.2f, 3f),
                ZenRipple(0f, 0.4f, canvasSize.minDimension * 1.5f, 2f)
            )
            animationTime = 0f
            
            // Animation loop
            while (isShattered) {
                delay(16) // ~60 FPS
                animationTime += 0.016f
                
                // Update glass shards with physics
                glassShards = glassShards.map { shard ->
                    val friction = 0.98f
                    val gravity = 0.15f
                    shard.copy(
                        x = shard.x + shard.velocityX,
                        y = shard.y + shard.velocityY + gravity * animationTime,
                        velocityX = shard.velocityX * friction,
                        velocityY = shard.velocityY * friction + gravity * 0.5f,
                        rotation = shard.rotation + shard.rotationSpeed,
                        rotationSpeed = shard.rotationSpeed * 0.99f,
                        alpha = (shard.alpha - 0.003f).coerceAtLeast(0.15f)
                    )
                }
                
                // Update energy particles
                energyParticles = energyParticles.map { particle ->
                    val friction = 0.96f
                    particle.copy(
                        x = particle.x + particle.velocityX,
                        y = particle.y + particle.velocityY,
                        velocityX = particle.velocityX * friction,
                        velocityY = particle.velocityY * friction,
                        alpha = (particle.alpha - 0.008f).coerceAtLeast(0f),
                        radius = particle.radius * 0.995f
                    )
                }
                
                // Update zen ripples
                zenRipples = zenRipples.map { ripple ->
                    ripple.copy(
                        radius = ripple.radius + ripple.speed,
                        alpha = if (ripple.radius < ripple.maxRadius) ripple.alpha * 0.995f else ripple.alpha * 0.98f
                    )
                }
            }
        }
    }
    
    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            audioUtils.stopListening()
        }
    }
    
    // Shatter Reset Logic or Persistence could be here
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Glass Visualization
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Capture canvas center and size for animation system
            if (canvasCenter == Offset.Zero) {
                canvasCenter = center
                canvasSize = size
            }
            
            val center = center
            
            // Draw "Glass"
            // If shattered, maybe draw fragments. 
            // If not, draw a pane that gets more opaque/cracked.
            
            if (!isShattered) {
                // Base Glass
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE0F7FA).copy(alpha = 0.3f),
                            Color(0xFFB2EBF2).copy(alpha = 0.1f)
                        ),
                        center = center,
                        radius = size.minDimension / 1.5f
                    )
                )
                
                // Stress/Volume Glow
                if (currentVolume > 0.1f) {
                     drawCircle(
                        color = Color(0xFFFF5252).copy(alpha = currentVolume * 0.6f),
                        radius = size.minDimension / 2 * currentVolume,
                        center = center
                    )
                }
                
                // Draw Cracks based on crackLevel
                // Procedural cracks: lines radiating from center or random spots
                val numCracks = (crackLevel * 50).toInt()
                for (i in 0 until numCracks) {
                    val angle = (i * 137.5f) // Golden angle for distribution
                    val startR = Random.nextFloat() * (size.minDimension / 4)
                    val endR = startR + (Random.nextFloat() * (size.minDimension / 3))
                    
                    val startX = center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * startR
                    val startY = center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * startR
                    
                    val endX = center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * endR
                    val endY = center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * endR
                    
                    drawLine(
                        color = Color.White.copy(alpha = 0.8f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 2f
                    )
                }
                
                // Border
                drawRect(
                    color = Color.White.copy(alpha = 0.5f),
                    style = Stroke(width = 8.dp.toPx())
                )
            } else {
                // ========== VIRAL SHATTERED STATE ==========
                
                // Deep space gradient background
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1A0030), // Deep purple center
                            Color(0xFF0D0015), // Near black
                            Color.Black
                        ),
                        center = center,
                        radius = size.maxDimension
                    )
                )
                
                // Zen ripples (calm after the storm)
                zenRipples.forEach { ripple ->
                    if (ripple.alpha > 0.01f) {
                        drawCircle(
                            color = Color(0xFF9B59B6).copy(alpha = ripple.alpha * 0.4f),
                            radius = ripple.radius,
                            center = center,
                            style = Stroke(width = 3.dp.toPx())
                        )
                        // Inner glow
                        drawCircle(
                            color = Color(0xFFDD22FF).copy(alpha = ripple.alpha * 0.2f),
                            radius = ripple.radius * 0.95f,
                            center = center,
                            style = Stroke(width = 6.dp.toPx())
                        )
                    }
                }
                
                // Energy particles with glow
                energyParticles.forEach { particle ->
                    if (particle.alpha > 0.01f) {
                        // Outer glow
                        drawCircle(
                            color = particle.color.copy(alpha = particle.alpha * 0.3f),
                            radius = particle.radius * 3f,
                            center = Offset(particle.x, particle.y)
                        )
                        // Core
                        drawCircle(
                            color = particle.color.copy(alpha = particle.alpha),
                            radius = particle.radius,
                            center = Offset(particle.x, particle.y)
                        )
                    }
                }
                
                // Glass shards with rotation and glow
                glassShards.forEach { shard ->
                    if (shard.alpha > 0.01f) {
                        translate(left = shard.x, top = shard.y) {
                            rotate(degrees = shard.rotation) {
                                scale(scale = shard.scale) {
                                    val path = Path().apply {
                                        if (shard.vertices.isNotEmpty()) {
                                            moveTo(shard.vertices[0].x, shard.vertices[0].y)
                                            for (i in 1 until shard.vertices.size) {
                                                lineTo(shard.vertices[i].x, shard.vertices[i].y)
                                            }
                                            close()
                                        }
                                    }
                                    
                                    // Shard glow
                                    drawPath(
                                        path = path,
                                        color = shard.color.copy(alpha = shard.alpha * 0.3f),
                                        style = Stroke(width = 8.dp.toPx())
                                    )
                                    
                                    // Shard fill with gradient-like effect
                                    drawPath(
                                        path = path,
                                        color = shard.color.copy(alpha = shard.alpha * 0.6f)
                                    )
                                    
                                    // Shard edge highlight
                                    drawPath(
                                        path = path,
                                        color = Color.White.copy(alpha = shard.alpha * 0.8f),
                                        style = Stroke(width = 1.5.dp.toPx())
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Central energy burst glow
                val burstAlpha = (1f - animationTime * 0.3f).coerceIn(0f, 1f)
                if (burstAlpha > 0f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFFFFF).copy(alpha = burstAlpha),
                                Color(0xFFDD22FF).copy(alpha = burstAlpha * 0.6f),
                                Color(0xFF22DDFF).copy(alpha = burstAlpha * 0.3f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension / 4 * (1 + animationTime * 0.5f)
                        ),
                        radius = size.minDimension / 4 * (1 + animationTime * 0.5f),
                        center = center
                    )
                }
            }
        }
        
        // UI Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Central Info / Action
            if (isShattered) {
                // Pulsing glow animation for text
                val infiniteTransition = rememberInfiniteTransition(label = "textGlow")
                val glowAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glow"
                )
                
                // ========== VIRAL GRADIENT TEXT ==========
                Text(
                    text = "SHATTERED!",
                    style = TextStyle(
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFDD22FF), // Purple
                                Color(0xFFFF22DD), // Magenta  
                                Color(0xFF22DDFF)  // Cyan
                            )
                        ),
                        shadow = Shadow(
                            color = Color(0xFFDD22FF).copy(alpha = glowAlpha * 0.8f),
                            offset = Offset(0f, 0f),
                            blurRadius = 30f
                        )
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Subtitle with softer styling
                Text(
                    text = "\u2728 You released your stress \u2728",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE0E0E0),
                        shadow = Shadow(
                            color = Color(0xFF9B59B6).copy(alpha = 0.5f),
                            offset = Offset(0f, 2f),
                            blurRadius = 10f
                        )
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // ========== VIRAL SHARE BUTTON ==========
                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "I just shattered the Scream Jar in Third Eye Timer! \uD83D\uDE31\uD83D\uDCA5\u2728 My stress is OBLITERATED! #ScreamJar #StressRelief #MentalHealth")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share your scream"))
                        
                        if (questManager.isScreamJarAvailable()) {
                            questManager.recordScreamJarShared()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFDD22FF),
                                    Color(0xFF9B59B6),
                                    Color(0xFF6366F1)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                ) {
                    Icon(
                        Icons.Filled.Share, 
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Share Result (+${QuestManager.SCREAM_JAR_KARMA} Karma)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Reset button with subtle styling
                TextButton(
                    onClick = {
                        // Reset all state
                        isShattered = false
                        crackLevel = 0f
                        isRecording = false
                        glassShards = emptyList()
                        energyParticles = emptyList()
                        zenRipples = emptyList()
                        animationTime = 0f
                    }
                ) {
                    Text(
                        "\u2728 Assemble New Jar \u2728", 
                        color = Color(0xFFB0B0B0),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
            } else {
                // Active Game State
                if (!hasPermission) {
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                    ) {
                        Text("Enable Microphone to Scream")
                    }
                } else {
                    // Hold to Scream Button
                    
                    // Visual Meter
                    LinearProgressIndicator(
                        progress = { crackLevel },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(8.dp),
                        color = Color(0xFFFF5252),
                        trackColor = Color.White.copy(alpha = 0.2f),
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .pointerInput(Unit) {

                                awaitPointerEventScope {
                                    while (true) {
                                        val down = awaitFirstDown(requireUnconsumed = false)
                                        isRecording = true
                                        errorMessage = null // Clear error on new attempt
                                        
                                        // Wait for up or cancellation
                                        val upOrCancel = waitForUpOrCancellation()
                                        if (upOrCancel == null) {
                                             // Cancelled
                                        }
                                        isRecording = false
                                    }
                                }
                            }
                            .background(
                                color = if (isRecording) Color(0xFFFF5252) else Color.White.copy(alpha = 0.1f),
                                shape = androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Filled.Mic else Icons.Filled.MicOff,
                            contentDescription = "Scream",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (isRecording) "SCREAM!" else "Hold & Scream",
                        color = Color.White,
                        fontSize = 20.sp,

                        fontWeight = FontWeight.Bold
                    )
                    
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFFF5252),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
    
    // Permission Explanation Logic (Simple Alert)
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permission Required") },
            text = { Text("The Scream Jar needs microphone access to detect your scream volume. No audio is recorded or saved.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
