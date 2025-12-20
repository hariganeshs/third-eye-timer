package com.thirdeyetimer.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.thirdeyetimer.app.domain.TruthPunchManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * TruthPopupDialog
 * 
 * Modal dialog for "Reality Check" moments when truths unlock.
 * Features screen shake, haptic feedback, and the harsh Brutalist styling
 * that delivers the philosophical "punch".
 */

// Terminal/Brutalist color scheme
private val TerminalBlack = Color(0xFF0A0A0A)
private val TerminalDarkGray = Color(0xFF1A1A1A)
private val TerminalRed = Color(0xFFFF3B3B)
private val TerminalAmber = Color(0xFFFFB000)
private val TerminalWhite = Color(0xFFE0E0E0)
private val TerminalGray = Color(0xFF666666)

@Composable
fun TruthUnlockDialog(
    truth: TruthPunchManager.TruthPunch,
    onDismiss: () -> Unit,
    onSeen: () -> Unit
) {
    val context = LocalContext.current
    
    // Trigger haptic feedback on show
    LaunchedEffect(Unit) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Strong "punch" pattern: short burst, pause, longer impact
                val pattern = longArrayOf(0, 100, 50, 200)
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 100, 50, 200), -1)
            }
        } catch (e: Exception) {
            // Ignore vibration errors
        }
        
        // Mark as seen after showing
        onSeen()
    }
    
    // Screen shake animation
    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(40),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeOffset"
    )
    
    // Only shake for first 600ms
    var shouldShake by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(600)
        shouldShake = false
    }
    
    // Glitch effect
    val glitchAlpha by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glitchAlpha"
    )
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .clickable { onDismiss() }
                .offset(x = if (shouldShake) shakeOffset.dp else 0.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .alpha(if (shouldShake) glitchAlpha else 1f)
                    .background(TerminalBlack, RoundedCornerShape(4.dp))
                    .border(3.dp, TerminalRed, RoundedCornerShape(4.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "Reality Check" header
                Text(
                    text = "⚠ REALITY CHECK ⚠",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TerminalAmber,
                    letterSpacing = 3.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Truth number
                Text(
                    text = "TRUTH #${truth.rank} UNLOCKED",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = TerminalRed
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Title
                Text(
                    text = truth.title,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TerminalWhite,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Divider with glitch effect
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            if (shouldShake) TerminalRed.copy(alpha = glitchAlpha)
                            else TerminalRed.copy(alpha = 0.5f)
                        )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // The Truth itself - the main "punch"
                Text(
                    text = "\"${truth.truth}\"",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = TerminalWhite,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Tier info
                val tierInfo = when (truth.tier) {
                    5 -> "TIER 5: THE OPERATIONAL LIES"
                    4 -> "TIER 4: THE HOLLOW PURSUIT"
                    3 -> "TIER 3: THE TRANSACTIONAL HEART"
                    2 -> "TIER 2: THE SPIRITUAL SCAM"
                    1 -> "TIER 1: THE ANNIHILATION"
                    else -> "UNKNOWN TIER"
                }
                
                Text(
                    text = tierInfo,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TerminalGray
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Dismiss button styled as terminal command
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TerminalDarkGray,
                        contentColor = TerminalRed
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .border(1.dp, TerminalRed.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                ) {
                    Text(
                        text = "> ACKNOWLEDGE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "[ You cannot unsee this truth ]",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TerminalGray.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * Ironic popup shown after watching a rewarded ad
 * Mocks the player for their impatience
 */
@Composable
fun AdHypocrisyPopup(
    adType: String, // "speed", "karma", "double_spiritual_ego"
    onDismiss: () -> Unit
) {
    val message = when (adType) {
        "speed" -> "You traded 30 seconds of your life to a corporation to save virtual time. You are truly lost."
        "karma" -> "You watched propaganda for imaginary points. The system thanks you for your obedience."
        "double_spiritual_ego" -> "You doubled your Spiritual Ego by absorbing more Spiritual Ego. How fitting."
        else -> "You chose distraction over presence. As expected."
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalBlack, RoundedCornerShape(4.dp))
                .border(2.dp, TerminalAmber, RoundedCornerShape(4.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "THE SHORTCUT TO NOWHERE",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TerminalAmber,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = message,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = TerminalWhite,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Here is your reward anyway.",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = TerminalGray,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TerminalAmber.copy(alpha = 0.2f),
                    contentColor = TerminalAmber
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "> I ACCEPT MY WEAKNESS",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Narrative frame shown before interstitial ads
 * Frames the ad as "mind chatter"
 */
@Composable
fun MindChatterPrelude(
    onContinue: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* Cannot dismiss */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalBlack, RoundedCornerShape(4.dp))
                .border(1.dp, TerminalGray, RoundedCornerShape(4.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glitch animation
            val infiniteTransition = rememberInfiniteTransition(label = "chatter")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.7f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(150),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "chatterAlpha"
            )
            
            Text(
                text = "⚡ MIND CHATTER DETECTED ⚡",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TerminalRed,
                modifier = Modifier.alpha(alpha)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Your mind is wandering.\nIt demands entertainment.\nYou cannot stop it.",
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = TerminalGray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Auto-continue after 2 seconds
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                onContinue()
            }
            
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = TerminalRed,
                trackColor = TerminalGray.copy(alpha = 0.3f)
            )
        }
    }
}

/**
 * Aftermath shown after interstitial ad completes
 */
@Composable
fun MindChatterAftermath(
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1500)
        onDismiss()
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalBlack, RoundedCornerShape(4.dp))
                .border(1.dp, TerminalGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "The noise has subsided.",
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = TerminalGray,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "For now.",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = TerminalGray.copy(alpha = 0.5f)
            )
        }
    }
}
