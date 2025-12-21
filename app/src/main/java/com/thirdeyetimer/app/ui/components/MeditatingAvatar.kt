package com.thirdeyetimer.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thirdeyetimer.app.ui.theme.CosmicColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * MeditatingAvatar
 *
 * A high-quality procedural vector avatar that evolves based on the user's level.
 * 
 * Evolution Stages:
 * Level 1-20: The Sleeper (Wireframe/Outline)
 * Level 21-40: The Waking (Solid Fill, Simple)
 * Level 41-60: The Rising (Levitating, Glowing)
 * Level 61-80: The Radiant (Aura layers, Pulsing)
 * Level 81-100: The Void (Cosmic effects, Dissolving)
 */
@Composable
fun MeditatingAvatar(
    level: Int,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_animation")
    
    // Animations based on level
    val levitateOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (level >= 41) -10f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "levitate"
    )
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (level >= 61) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val auraRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (level >= 81) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aura_rotation"
    )

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(size.toPx() / 2, size.toPx() / 2)
        val avatarSize = size.toPx() * 0.8f
        
        // Apply global transformations
        scale(scale = pulseScale, pivot = center) {
            
            // Draw Aura (Level 61+)
            if (level >= 61) {
                drawAura(center, avatarSize, level, auraRotation)
            }
            
            // Draw Body with Levitation (Level 41+)
            val bodyCenter = center.copy(y = center.y + levitateOffset)
            drawBody(bodyCenter, avatarSize, level)
            
            // Draw Head
            val headCenter = bodyCenter.copy(y = bodyCenter.y - avatarSize * 0.3f)
            drawHead(headCenter, avatarSize, level)
            
            // Draw Third Eye (Level 41+)
            if (level >= 41) {
                drawThirdEye(headCenter, avatarSize, level)
            }
            
            // Draw Void Effects (Level 81+)
            if (level >= 81) {
                drawVoidParticles(center, avatarSize)
            }
        }
    }
}

private fun DrawScope.drawBody(center: Offset, size: Float, level: Int) {
    val color = when {
        level >= 81 -> CosmicColors.TextPrimary // White/Void
        level >= 61 -> CosmicColors.Accent      // Gold
        level >= 41 -> CosmicColors.Secondary   // Indigo
        level >= 21 -> CosmicColors.GlassBorder // Grey/Solid
        else -> Color.Gray.copy(alpha = 0.5f)   // Wireframe
    }
    
    val style = if (level >= 21) Fill else Stroke(width = 3.dp.toPx())
    
    // Lotus position triangle base
    val path = Path().apply {
        moveTo(center.x, center.y - size * 0.1f) // Neck
        lineTo(center.x + size * 0.35f, center.y + size * 0.4f) // Right Knee
        quadraticBezierTo(
            center.x, center.y + size * 0.5f, // Bottom curve
            center.x - size * 0.35f, center.y + size * 0.4f // Left Knee
        )
        close()
    }
    
    drawPath(path = path, color = color, style = style)
    
    // Add glow for high levels
    if (level >= 61) {
        drawPath(
            path = path,
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.5f), Color.Transparent),
                center = center,
                radius = size * 0.6f
            ),
            style = Fill
        )
    }
}

private fun DrawScope.drawHead(center: Offset, size: Float, level: Int) {
    val color = when {
        level >= 81 -> CosmicColors.TextPrimary
        level >= 61 -> CosmicColors.Accent
        level >= 41 -> CosmicColors.Secondary
        level >= 21 -> CosmicColors.GlassBorder
        else -> Color.Gray.copy(alpha = 0.5f)
    }
    
    val style = if (level >= 21) Fill else Stroke(width = 3.dp.toPx())
    
    drawCircle(
        color = color,
        radius = size * 0.12f,
        center = center,
        style = style
    )
}

private fun DrawScope.drawThirdEye(center: Offset, size: Float, level: Int) {
    val eyeColor = if (level >= 81) Color.Black else CosmicColors.Accent
    
    drawCircle(
        color = eyeColor,
        radius = size * 0.03f,
        center = center.copy(y = center.y - size * 0.02f)
    )
    
    if (level >= 61) {
        // glowing ring
        drawCircle(
            color = CosmicColors.Accent.copy(alpha = 0.5f),
            radius = size * 0.05f,
            center = center.copy(y = center.y - size * 0.02f),
            style = Stroke(width = 2f)
        )
    }
}

private fun DrawScope.drawAura(center: Offset, size: Float, level: Int, rotation: Float) {
    rotate(rotation, center) {
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color.Transparent,
                    CosmicColors.Accent.copy(alpha = 0.2f),
                    Color.Transparent
                )
            ),
            radius = size * 0.6f,
            center = center,
            style = Stroke(width = size * 0.1f)
        )
    }
    
    if (level >= 81) {
        rotate(-rotation * 0.5f, center) {
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        CosmicColors.Secondary.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                radius = size * 0.75f,
                center = center,
                style = Stroke(width = size * 0.05f)
            )
        }
    }
}

private fun DrawScope.drawVoidParticles(center: Offset, size: Float) {
    // Determine particle positions based on some pseudo-random logic or fixed pattern
    // visualizing "disintegration"
    val particleCount = 8
    val radius = size * 0.5f
    
    for (i in 0 until particleCount) {
        val angle = (i * (360f / particleCount)) * (Math.PI / 180f)
        val offset = Offset(
            x = center.x + (cos(angle) * radius).toFloat(),
            y = center.y + (sin(angle) * radius).toFloat()
        )
        
        drawCircle(
            color = Color.White.copy(alpha = 0.6f),
            radius = 2.dp.toPx(),
            center = offset
        )
    }
}
