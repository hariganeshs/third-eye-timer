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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import kotlin.random.Random

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
                    
                    // Use amplitude directly - emulators produce very low values (3-10)
                    // Real devices produce much higher values (100-32767)
                    val amplitude = audioUtils.getAmplitude()
                    
                    // For emulator: any amplitude > 0 should register
                    // Normalize using log scale to handle both emulator (low) and real device (high) values
                    val normalized = if (amplitude > 0) {
                        // Log scale: amplitude 1 -> 0.0, amplitude 100 -> 0.5, amplitude 10000 -> 1.0
                        (kotlin.math.ln(amplitude.toDouble() + 1) / kotlin.math.ln(10000.0)).coerceIn(0.0, 1.0).toFloat()
                    } else {
                        0f
                    }
                    
                    // Smooth visual volume
                    currentVolume = (currentVolume * 0.7f + normalized * 0.3f)
                    
                    // Any sound at all should add cracks (for emulator compatibility)
                    if (amplitude > 0) {
                        // Progress based on amplitude - even tiny values add up
                        val progressRate = if (amplitude > 100) {
                            0.02f // Real device scream
                        } else if (amplitude > 10) {
                            0.01f // Moderate sound
                        } else {
                            0.005f // Emulator low values
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
                // SHATTERED STATE
                // Draw many small shards flying out (static for now)
                 drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black
                        )
                    )
                )
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
                Text(
                    text = "SHATTERED!",
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "You released your stress.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "I just shattered the Scream Jar in Third Eye Timer! \uD83D\uDE31\uD83D\uDCA5 #ScreamJar #StressRelief")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share your scream"))
                        
                        // Award Karma logic - ideally this happens after returning from share, 
                        // but Android doesn't guarantee callback for simple share intents easily.
                        // We'll award it here for simplicity or assume user shares.
                        if (questManager.isScreamJarAvailable()) {
                            questManager.recordScreamJarShared()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B59B6)),
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Result (+${QuestManager.SCREAM_JAR_KARMA} Karma)")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(onClick = {
                    // Reset
                    isShattered = false
                    crackLevel = 0f
                    isRecording = false
                }) {
                    Text("Assemble New Jar", color = Color.White)
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
