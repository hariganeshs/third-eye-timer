package com.thirdeyetimer.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thirdeyetimer.app.ui.theme.CosmicColors
import com.thirdeyetimer.app.ui.theme.TimerTypography
import kotlin.math.cos
import kotlin.math.sin

/**
 * BreathingCircle
 * 
 * An animated breathing indicator with pulsing glow effect.
 * The circle expands and contracts in a calming rhythm to guide breathing.
 */
@Composable
fun BreathingCircle(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    breathInDuration: Int = 4000,
    breathOutDuration: Int = 4000,
    ringColor: Color = CosmicColors.Primary,
    glowColor: Color = CosmicColors.GlowIndigo,
    size: Dp = 280.dp,
    disintegrationLevel: Float = 0f // 0f (perfect) to 1f (shattered)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    
    // Disintegration animations
    val glitchTrigger by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glitchTrigger"
    )

    // Using disintegration level to determine intensities
    val density = LocalDensity.current
    val jitterIntensity = with(density) { 8.dp.toPx() } * disintegrationLevel
    val flickerChance = 0.1f + (disintegrationLevel * 0.4f)
    val isFlickering = disintegrationLevel > 0.1f && glitchTrigger < flickerChance
    
    val jitterX = if (disintegrationLevel > 0.2f && glitchTrigger < 0.3f) 
        (Math.random() * 2 - 1).toFloat() * jitterIntensity else 0f
    val jitterY = if (disintegrationLevel > 0.2f && glitchTrigger < 0.3f) 
        (Math.random() * 2 - 1).toFloat() * jitterIntensity else 0f
    
    // Breathing scale animation
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = breathInDuration,
                easing = EaseInOutCubic
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )
    
    // Glow intensity animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = breathInDuration,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    // Rotation animation for the ring
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 20000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val currentScale = if (isActive) scale else 0.9f
    val currentGlowAlpha = if (isActive) glowAlpha else 0.3f
    
    Canvas(
        modifier = modifier
            .size(size)
            .offset(x = jitterX.dp, y = jitterY.dp)
    ) {
        val center = Offset(size.toPx() / 2, size.toPx() / 2)
        val baseRadius = (size.toPx() / 2) * 0.7f
        val scaledRadius = baseRadius * currentScale
        
        val alphaMultiplier = if (isFlickering) 0.3f else 1.0f
        val finalGlowColor = if (disintegrationLevel > 0.7f && glitchTrigger < 0.2f) 
            Color(0xFFFF3B3B) else glowColor // Flash red at high ego
        
        // Outer glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor.copy(alpha = currentGlowAlpha * 0.5f),
                    glowColor.copy(alpha = currentGlowAlpha * 0.2f),
                    Color.Transparent
                ),
                center = center,
                radius = scaledRadius * 1.5f
            ),
            radius = scaledRadius * 1.5f,
            center = center
        )
        
        // Inner glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor.copy(alpha = currentGlowAlpha),
                    Color.Transparent
                ),
                center = center,
                radius = scaledRadius * 1.2f
            ),
            radius = scaledRadius * 1.2f,
            center = center
        )
        
        // Main breathing ring
        rotate(rotation, center) {
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = if (disintegrationLevel > 0.8f && glitchTrigger < 0.15f) {
                        listOf(Color.White, Color.Red, Color.White)
                    } else {
                        listOf(
                            CosmicColors.Primary,
                            CosmicColors.Secondary,
                            CosmicColors.PrimaryLight,
                            CosmicColors.Primary
                        )
                    },
                    center = center
                ),
                radius = scaledRadius,
                center = center,
                alpha = alphaMultiplier,
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
        
        // Inner ring
        drawCircle(
            color = CosmicColors.GlassBackground,
            radius = scaledRadius - 20.dp.toPx(),
            center = center
        )
        
        // Decorative particles around the ring
        if (isActive) {
            for (i in 0 until 8) {
                val angle = (rotation + i * 45f) * (Math.PI / 180f)
                val particleX = center.x + cos(angle).toFloat() * scaledRadius * 1.15f
                val particleY = center.y + sin(angle).toFloat() * scaledRadius * 1.15f
                
                drawCircle(
                    color = CosmicColors.TextSecondary.copy(alpha = 0.6f),
                    radius = 2.dp.toPx(),
                    center = Offset(particleX, particleY)
                )
            }
        }
    }
}

/**
 * TimerDisplay
 * 
 * Large animated timer display with glow effect.
 */
@Composable
fun TimerDisplay(
    timeText: String,
    modifier: Modifier = Modifier,
    isRunning: Boolean = false,
    textColor: Color = CosmicColors.TextPrimary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "timerGlow")
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Glow text (behind)
        if (isRunning) {
            Text(
                text = timeText,
                style = TimerTypography.timerLarge.copy(
                    color = CosmicColors.Primary.copy(alpha = glowAlpha * 0.3f)
                ),
                modifier = Modifier.offset(y = 2.dp)
            )
        }
        
        // Main timer text
        Text(
            text = timeText,
            style = TimerTypography.timerLarge.copy(
                color = textColor
            )
        )
    }
}

/**
 * ProgressRing
 * 
 * A circular progress indicator around the timer.
 */
@Composable
fun ProgressRing(
    progress: Float, // 0f to 1f
    modifier: Modifier = Modifier,
    size: Dp = 300.dp,
    strokeWidth: Dp = 8.dp,
    trackColor: Color = CosmicColors.GlassBackground,
    progressColor: Color = CosmicColors.Secondary,
    glowColor: Color = CosmicColors.GlowTeal,
    disintegrationLevel: Float = 0f
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300, easing = EaseInOutCubic),
        label = "progressAnimation"
    )

    // Glitch for progress ring
    val infiniteTransition = rememberInfiniteTransition(label = "progressGlitch")
    val glitchTrigger by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(150),
            repeatMode = RepeatMode.Restart
        ),
        label = "glitch"
    )
    
    val isFlickering = disintegrationLevel > 0.4f && glitchTrigger < (disintegrationLevel * 0.3f)
    val alphaMultiplier = if (isFlickering) 0.5f else 1.0f
    
    Canvas(
        modifier = modifier.size(size)
    ) {
        val center = Offset(size.toPx() / 2, size.toPx() / 2)
        val radius = (size.toPx() - strokeWidth.toPx()) / 2
        val sweepAngle = 360f * animatedProgress
        
        // Glow for progress
        if (animatedProgress > 0) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.4f),
                        glowColor.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    center = center
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth.toPx() * 3,
                    cap = StrokeCap.Round
                ),
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
        }
        
        // Track
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round
            ),
            topLeft = Offset(
                center.x - radius,
                center.y - radius
            ),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
        )
        
        // Progress arc
        if (animatedProgress > 0) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        CosmicColors.Secondary,
                        CosmicColors.SecondaryLight,
                        CosmicColors.Primary,
                        CosmicColors.Secondary
                    ),
                    center = center
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                alpha = alphaMultiplier,
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                ),
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
        }
    }
}

/**
 * MeditationTimer
 * 
 * Combined breathing circle with timer display and progress ring.
 */
@Composable
fun MeditationTimer(
    timeText: String,
    progress: Float,
    isRunning: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 320.dp,
    disintegrationLevel: Float = 0f
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Breathing circle (background)
        BreathingCircle(
            isActive = isRunning,
            size = size,
            disintegrationLevel = disintegrationLevel,
            modifier = Modifier.align(Alignment.Center)
        )
        
        // Progress ring
        ProgressRing(
            progress = progress,
            size = size - 20.dp,
            disintegrationLevel = disintegrationLevel,
            modifier = Modifier.align(Alignment.Center)
        )
        
        // Timer display
        TimerDisplay(
            timeText = timeText,
            isRunning = isRunning,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
