package com.thirdeyetimer.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thirdeyetimer.app.ui.components.*
import com.thirdeyetimer.app.ui.theme.CosmicColors
import kotlin.random.Random

/**
 * Confetti particle data
 */
private data class ConfettiParticle(
    var x: Float,
    var y: Float,
    val size: Float,
    val color: Color,
    var velocityX: Float,
    var velocityY: Float,
    val rotation: Float,
    val rotationSpeed: Float
)

/**
 * CompletionScreen
 * 
 * Beautiful session completion celebration with confetti,
 * session stats, and achievement notifications.
 */
@Composable
fun CompletionScreen(
    sessionDuration: String,
    currentStreak: Int,
    totalTime: String,
    heartRateReduction: Int? = null,
    newAchievement: String? = null,
    onStartAnotherClick: () -> Unit,
    onShareClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confettiParticles by remember { mutableStateOf<List<ConfettiParticle>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(Pair(0f, 0f)) }
    
    // Initialize confetti
    LaunchedEffect(canvasSize) {
        if (canvasSize.first > 0 && confettiParticles.isEmpty()) {
            confettiParticles = List(60) {
                createConfettiParticle(canvasSize.first, canvasSize.second)
            }
        }
    }
    
    // Animate confetti
    LaunchedEffect(confettiParticles) {
        if (confettiParticles.isNotEmpty()) {
            kotlinx.coroutines.delay(30)
            confettiParticles = confettiParticles.map { particle ->
                particle.copy(
                    x = particle.x + particle.velocityX,
                    y = particle.y + particle.velocityY,
                    velocityY = particle.velocityY + 0.3f // gravity
                )
            }.filter { it.y < canvasSize.second + 50 }
        }
    }
    
    // Entry animation
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "entryScale"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicColors.BackgroundStart)
    ) {
        // Particle background
        ParticleBackground(
            particleCount = 20,
            enableAnimation = true
        )
        
        // Confetti layer
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            if (canvasSize.first != size.width || canvasSize.second != size.height) {
                canvasSize = Pair(size.width, size.height)
            }
            
            confettiParticles.forEach { particle ->
                drawCircle(
                    color = particle.color,
                    radius = particle.size,
                    center = Offset(particle.x, particle.y)
                )
            }
        }
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Success icon with glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                // Glow
                val infiniteTransition = rememberInfiniteTransition(label = "glow")
                val glowAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.7f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glowAlpha"
                )
                
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(50))
                        .background(CosmicColors.Accent.copy(alpha = glowAlpha * 0.3f))
                )
                
                Text(
                    text = "🪷",
                    fontSize = 64.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Congratulations text
            Text(
                text = "Session Complete!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = CosmicColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Well done on your meditation practice",
                style = MaterialTheme.typography.bodyLarge,
                color = CosmicColors.TextTertiary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Stats card
            GlassmorphicCardWithGlow(
                modifier = Modifier.fillMaxWidth(),
                glowColor = CosmicColors.GlowTeal,
                cornerRadius = 24.dp,
                contentPadding = PaddingValues(24.dp)
            ) {
                // Duration
                StatRow(
                    icon = "⏱️",
                    label = "Duration",
                    value = sessionDuration
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Streak
                StatRow(
                    icon = "🔥",
                    label = "Streak",
                    value = "$currentStreak Day${if (currentStreak != 1) "s" else ""}",
                    valueColor = CosmicColors.Accent
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Total time
                StatRow(
                    icon = "🧘",
                    label = "Total Time",
                    value = totalTime
                )
                
                // Heart rate (if measured)
                if (heartRateReduction != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    StatRow(
                        icon = "❤️",
                        label = "Heart Rate",
                        value = "${if (heartRateReduction > 0) "-" else "+"}${kotlin.math.abs(heartRateReduction)} bpm",
                        valueColor = if (heartRateReduction > 0) CosmicColors.Success else CosmicColors.Warning
                    )
                }
            }
            
            // Achievement notification (if new)
            if (newAchievement != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    backgroundColor = CosmicColors.Accent.copy(alpha = 0.15f),
                    borderColor = CosmicColors.Accent.copy(alpha = 0.5f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "🏆", fontSize = 28.sp)
                        Column {
                            Text(
                                text = "Achievement Unlocked!",
                                style = MaterialTheme.typography.labelMedium,
                                color = CosmicColors.Accent
                            )
                            Text(
                                text = newAchievement,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CosmicColors.TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Share button
                GlassmorphicButton(
                    onClick = onShareClick,
                    modifier = Modifier.weight(1f),
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = CosmicColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share",
                        style = MaterialTheme.typography.labelLarge,
                        color = CosmicColors.TextSecondary
                    )
                }
                
                // Start another button
                PrimaryGradientButton(
                    onClick = onStartAnotherClick,
                    modifier = Modifier.weight(1f),
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Start Another",
                        tint = CosmicColors.TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Again",
                        style = MaterialTheme.typography.labelLarge,
                        color = CosmicColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

/**
 * Single stat row component
 */
@Composable
private fun StatRow(
    icon: String,
    label: String,
    value: String,
    valueColor: Color = CosmicColors.TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 20.sp)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = CosmicColors.TextTertiary
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Creates a random confetti particle
 */
private fun createConfettiParticle(maxWidth: Float, maxHeight: Float): ConfettiParticle {
    return ConfettiParticle(
        x = Random.nextFloat() * maxWidth,
        y = -Random.nextFloat() * 200,
        size = Random.nextFloat() * 6 + 4,
        color = CosmicColors.ConfettiColors.random(),
        velocityX = (Random.nextFloat() - 0.5f) * 6,
        velocityY = Random.nextFloat() * 3 + 2,
        rotation = Random.nextFloat() * 360,
        rotationSpeed = (Random.nextFloat() - 0.5f) * 10
    )
}
