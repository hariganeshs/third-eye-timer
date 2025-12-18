package com.thirdeyetimer.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thirdeyetimer.app.ui.components.*
import com.thirdeyetimer.app.ui.theme.CosmicColors
import com.thirdeyetimer.app.ui.theme.TimerTypography

/**
 * MeditationScreen
 * 
 * The immersive full-screen meditation experience.
 * Designed to be distraction-free with minimal UI and calming animations.
 */
@Composable
fun MeditationScreen(
    timerText: String,
    progress: Float, // 0f to 1f
    isRunning: Boolean,
    isPaused: Boolean,
    guidedMeditationName: String? = null,
    onPauseResumeClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "meditationAmbient")
    
    // Subtle background pulse
    val backgroundAlpha by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgPulse"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicColors.BackgroundStart)
    ) {
        // Particle background (slower when paused)
        ParticleBackground(
            particleCount = 30,
            enableAnimation = isRunning && !isPaused
        )
        
        // Ambient glow overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    CosmicColors.Primary.copy(alpha = backgroundAlpha)
                )
        )
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section - Guided meditation name (if any)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 48.dp)
            ) {
                if (guidedMeditationName != null) {
                    Text(
                        text = guidedMeditationName,
                        style = MaterialTheme.typography.titleMedium,
                        color = CosmicColors.TextTertiary,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = if (isPaused) "Paused" else "Breathe...",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Light,
                            letterSpacing = 2.sp
                        ),
                        color = CosmicColors.TextTertiary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Center section - Timer with breathing circle
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Large breathing circle
                BreathingCircle(
                    isActive = isRunning && !isPaused,
                    size = 340.dp,
                    breathInDuration = 4000,
                    breathOutDuration = 4000
                )
                
                // Progress ring
                ProgressRing(
                    progress = progress,
                    size = 320.dp,
                    strokeWidth = 6.dp
                )
                
                // Timer display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = timerText,
                        style = TimerTypography.timerLarge.copy(
                            color = CosmicColors.TextPrimary
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Progress percentage
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CosmicColors.TextTertiary
                    )
                }
            }
            
            // Bottom section - Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stop button
                GlassmorphicButton(
                    onClick = onStopClick,
                    cornerRadius = 50.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Stop",
                        tint = CosmicColors.Error,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Pause/Resume button (larger)
                Surface(
                    onClick = onPauseResumeClick,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    color = if (isPaused) CosmicColors.Secondary else CosmicColors.GlassBackground,
                    shape = CircleShape
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            tint = CosmicColors.TextPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                // Spacer to balance layout
                Box(modifier = Modifier.size(60.dp))
            }
        }
    }
}

/**
 * Minimized timer overlay for when app is in background
 */
@Composable
fun MinimizedTimerOverlay(
    timerText: String,
    isRunning: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier
            .padding(16.dp),
        cornerRadius = 24.dp,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pulsing indicator
            if (isRunning) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseAlpha"
                )
                
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(CosmicColors.Secondary.copy(alpha = alpha))
                )
            }
            
            Text(
                text = timerText,
                style = MaterialTheme.typography.titleLarge,
                color = CosmicColors.TextPrimary,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "Meditating",
                style = MaterialTheme.typography.bodyMedium,
                color = CosmicColors.TextTertiary
            )
        }
    }
}
