package com.thirdeyetimer.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thirdeyetimer.app.ui.theme.CosmicColors
import kotlinx.coroutines.delay

/**
 * PranaCounter
 * 
 * Animated counter component for displaying Prana (spiritual energy).
 * Features smooth counting animation and glow effects for that
 * satisfying idle game feel.
 */
@Composable
fun PranaCounter(
    currentPrana: Long,
    pranaPerSecond: Double = 0.0,
    showRate: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Animate the displayed value for smooth counting
    var displayedPrana by remember { mutableStateOf(currentPrana) }
    
    LaunchedEffect(currentPrana) {
        // Smooth animation to new value
        val startValue = displayedPrana
        val endValue = currentPrana
        val difference = endValue - startValue
        
        if (difference > 0) {
            val steps = minOf(20, difference.toInt().coerceAtLeast(1))
            val stepDelay = 300L / steps
            val stepSize = difference / steps
            
            repeat(steps) { i ->
                delay(stepDelay)
                displayedPrana = startValue + (stepSize * (i + 1))
            }
            displayedPrana = endValue
        } else {
            displayedPrana = endValue
        }
    }
    
    // Pulsing glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "pranaGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Glow background
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(20.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            CosmicColors.Accent.copy(alpha = glowAlpha * 0.5f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // Main content
        GlassmorphicCard(
            cornerRadius = 16.dp,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Prana icon (spiritual orb)
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = "Prana",
                    tint = CosmicColors.Accent,
                    modifier = Modifier.size(28.dp)
                )
                
                Column {
                    // Prana value
                    Text(
                        text = formatPrana(displayedPrana),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = CosmicColors.TextPrimary
                    )
                    
                    // Rate display
                    if (showRate && pranaPerSecond > 0) {
                        Text(
                            text = "+${formatRate(pranaPerSecond)} Prana",
                            style = MaterialTheme.typography.labelSmall,
                            color = CosmicColors.Accent
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact Prana display for the meditation screen
 */
@Composable
fun PranaCounterCompact(
    sessionPrana: Long,
    pranaPerSecond: Double,
    modifier: Modifier = Modifier
) {
    var displayedPrana by remember { mutableStateOf(sessionPrana) }
    
    LaunchedEffect(sessionPrana) {
        displayedPrana = sessionPrana
    }
    
    // Subtle pulse when prana increases
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "pranaScale"
    )
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CosmicColors.GlassBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = "Prana",
            tint = CosmicColors.Accent,
            modifier = Modifier.size(20.dp)
        )
        
        Text(
            text = "+${formatPrana(displayedPrana)}",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = CosmicColors.Accent
        )
        
        Text(
            text = "(${formatRate(pranaPerSecond)})",
            style = MaterialTheme.typography.labelSmall,
            color = CosmicColors.TextTertiary
        )
    }
}

/**
 * Prana earned summary for completion screen
 */
@Composable
fun PranaEarnedSummary(
    pranaEarned: Long,
    bonusMultiplier: Double = 1.0,
    onWatchAdClick: () -> Unit,
    showAdButton: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "earnedGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "earnedPulse"
    )
    
    GlassmorphicCardWithGlow(
        modifier = modifier.fillMaxWidth(),
        glowColor = CosmicColors.Accent,
        cornerRadius = 20.dp,
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            Text(
                text = "Prana Earned",
                style = MaterialTheme.typography.titleMedium,
                color = CosmicColors.TextTertiary
            )
            
            // Big prana number
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = CosmicColors.Accent,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "+${formatPrana(pranaEarned)}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = CosmicColors.Accent
                )
            }
            
            // Multiplier info if not 1.0
            if (bonusMultiplier > 1.0) {
                Text(
                    text = "${String.format("%.1f", bonusMultiplier)}x Bonus Active!",
                    style = MaterialTheme.typography.labelMedium,
                    color = CosmicColors.Success
                )
            }
            
            // Watch ad for 2x button
            if (showAdButton) {
                Spacer(modifier = Modifier.height(4.dp))
                
                PrimaryGradientButton(
                    onClick = onWatchAdClick,
                    cornerRadius = 24.dp,
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Watch Ad for 2x Prana",
                        style = MaterialTheme.typography.labelLarge,
                        color = CosmicColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Helper functions

private fun formatPrana(prana: Long): String {
    return when {
        prana >= 1_000_000_000_000L -> String.format("%.2fT", prana / 1_000_000_000_000.0)
        prana >= 1_000_000_000L -> String.format("%.2fB", prana / 1_000_000_000.0)
        prana >= 1_000_000L -> String.format("%.2fM", prana / 1_000_000.0)
        prana >= 1_000L -> String.format("%.1fK", prana / 1_000.0)
        else -> prana.toString()
    }
}

private fun formatRate(rate: Double): String {
    return when {
        rate >= 1_000_000L -> String.format("%.1fM/s", rate / 1_000_000.0)
        rate >= 1_000L -> String.format("%.1fK/s", rate / 1_000.0)
        rate >= 10 -> String.format("%.0f/s", rate)
        else -> String.format("%.1f/s", rate)
    }
}
