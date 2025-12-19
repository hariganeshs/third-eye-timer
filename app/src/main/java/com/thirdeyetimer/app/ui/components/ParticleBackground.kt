package com.thirdeyetimer.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.thirdeyetimer.app.ui.theme.CosmicColors
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Particle data class for floating particles
 */
private data class Particle(
    var x: Float,
    var y: Float,
    val size: Float,
    val alpha: Float,
    val speed: Float,
    val xDrift: Float,
    val color: Color
)

/**
 * ParticleBackground
 * 
 * Creates a beautiful animated starfield background with slowly drifting particles.
 * This creates a calming cosmic atmosphere for meditation.
 */
@Composable
fun ParticleBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 50,
    baseColor: Color = CosmicColors.TextSecondary,
    enableAnimation: Boolean = true
) {
    var particles by remember { mutableStateOf<List<Particle>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(Pair(0f, 0f)) }
    
    // Initialize particles when canvas size is known
    LaunchedEffect(canvasSize) {
        if (canvasSize.first > 0 && canvasSize.second > 0) {
            particles = List(particleCount) {
                createRandomParticle(canvasSize.first, canvasSize.second, baseColor)
            }
        }
    }
    
    // Animation loop
    LaunchedEffect(enableAnimation, canvasSize) {
        if (enableAnimation && canvasSize.first > 0) {
            while (true) {
                delay(50) // ~20 FPS for smooth but efficient animation
                particles = particles.map { particle ->
                    var newY = particle.y - particle.speed
                    var newX = particle.x + particle.xDrift
                    
                    // Reset particle if it goes off screen
                    if (newY < -10) {
                        newY = canvasSize.second + 10
                        newX = Random.nextFloat() * canvasSize.first
                    }
                    
                    // Wrap X position
                    if (newX < 0) newX = canvasSize.first
                    if (newX > canvasSize.first) newX = 0f
                    
                    particle.copy(x = newX, y = newY)
                }
            }
        }
    }
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                val newWidth = size.width.toFloat()
                val newHeight = size.height.toFloat()
                if (canvasSize.first != newWidth || canvasSize.second != newHeight) {
                    canvasSize = Pair(newWidth, newHeight)
                }
            }
    ) {
        // Draw gradient background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    CosmicColors.BackgroundStart,
                    CosmicColors.BackgroundEnd
                )
            )
        )
        
        // Draw particles
        particles.forEach { particle ->
            // Outer glow
            drawCircle(
                color = particle.color.copy(alpha = particle.alpha * 0.3f),
                radius = particle.size * 3,
                center = Offset(particle.x, particle.y)
            )
            
            // Core particle
            drawCircle(
                color = particle.color.copy(alpha = particle.alpha),
                radius = particle.size,
                center = Offset(particle.x, particle.y)
            )
        }
    }
}

/**
 * Creates a random particle with varied properties
 */
private fun createRandomParticle(
    maxWidth: Float,
    maxHeight: Float,
    baseColor: Color
): Particle {
    val colorVariants = listOf(
        baseColor,
        CosmicColors.Primary.copy(alpha = 0.6f),
        CosmicColors.Secondary.copy(alpha = 0.4f),
        CosmicColors.Accent.copy(alpha = 0.3f),
        Color.White.copy(alpha = 0.5f)
    )
    
    return Particle(
        x = Random.nextFloat() * maxWidth,
        y = Random.nextFloat() * maxHeight,
        size = Random.nextFloat() * 2f + 0.5f,
        alpha = Random.nextFloat() * 0.6f + 0.2f,
        speed = Random.nextFloat() * 0.3f + 0.1f,
        xDrift = (Random.nextFloat() - 0.5f) * 0.2f,
        color = colorVariants.random()
    )
}

/**
 * CosmicBackground
 * 
 * Enhanced cosmic background with gradient and optional particles.
 */
@Composable
fun CosmicBackground(
    modifier: Modifier = Modifier,
    showParticles: Boolean = true,
    particleCount: Int = 40
) {
    if (showParticles) {
        ParticleBackground(
            modifier = modifier,
            particleCount = particleCount
        )
    } else {
        Canvas(
            modifier = modifier.fillMaxSize()
        ) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CosmicColors.BackgroundStart,
                        CosmicColors.BackgroundEnd
                    )
                )
            )
        }
    }
}

/**
 * AnimatedStars
 * 
 * Creates a twinkling star effect overlay.
 */
@Composable
fun AnimatedStars(
    modifier: Modifier = Modifier,
    starCount: Int = 30
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    
    // Create multiple twinkling animations
    val twinkleStates = List(starCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1000 + (index * 100) % 2000,
                    easing = EaseInOutSine
                ),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset((index * 200) % 2000)
            ),
            label = "twinkle_$index"
        )
    }
    
    val starPositions = remember {
        List(starCount) {
            Pair(Random.nextFloat(), Random.nextFloat())
        }
    }
    
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        starPositions.forEachIndexed { index, (xRatio, yRatio) ->
            val alpha = twinkleStates[index].value
            val x = xRatio * size.width
            val y = yRatio * size.height
            
            // Star glow
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.3f),
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
            
            // Star core
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.8f),
                radius = 1.5f.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
