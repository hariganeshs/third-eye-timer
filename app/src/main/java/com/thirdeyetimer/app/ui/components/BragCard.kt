package com.thirdeyetimer.app.ui.components

import android.graphics.Bitmap
import android.graphics.Picture
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thirdeyetimer.app.ui.theme.CosmicColors
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.thirdeyetimer.app.R

/**
 * BragCard
 * 
 * A visually stunning card designed to be shared on social media.
 * It showcases the user's meditation stats and current "Level".
 */
@Composable
fun BragCard(
    username: String = "Mindful Soul",
    streakDays: Int,
    totalMinutes: Long,
    level: String = "Seeker",
    spiritualEgo: Long = 0L,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(320.dp)
            .height(400.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CosmicColors.BackgroundStart,
                        CosmicColors.PrimaryContainer
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        CosmicColors.Accent,
                        CosmicColors.Primary
                    )
                ),
                shape = MaterialTheme.shapes.extraLarge
            )
    ) {
        // Background particles/stars effect (static for image capture)
        CosmicBackground(particleCount = 15)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "THIRD EYE TIMER",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 4.sp,
                    color = CosmicColors.TextTertiary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = level.uppercase(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                CosmicColors.AccentLight,
                                CosmicColors.Accent,
                                CosmicColors.Secondary
                            )
                        )
                    )
                )
                
                // Spiritual Ego Icon (centered, without label)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_spiritual_ego),
                        contentDescription = "Spiritual Ego",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatSpiritualEgoBrag(spiritualEgo),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CosmicColors.Accent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Central Visual - The "Badge"
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                CosmicColors.Primary.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Inner glowing ring
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .border(
                            width = 3.dp,
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    CosmicColors.Primary,
                                    CosmicColors.Secondary,
                                    CosmicColors.Accent,
                                    CosmicColors.Primary
                                )
                            ),
                            shape = CircleShape
                        )
                )
                
                Text(
                    text = "🧘",
                    fontSize = 48.sp
                )
            }
            
            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Streak",
                    value = "$streakDays",
                    unit = "Days",
                    icon = "🔥"
                )
                
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(CosmicColors.GlassBorder)
                        .align(Alignment.CenterVertically)
                )
                
                StatItem(
                    label = "Total Zen",
                    value = "${totalMinutes / 60}",
                    unit = "Hours",
                    icon = "⏳"
                )
            }
            
            // Footer
            Text(
                text = "Join me on the path to enlightenment.",
                style = MaterialTheme.typography.bodySmall,
                color = CosmicColors.TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    unit: String,
    icon: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = icon,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = CosmicColors.TextPrimary
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = CosmicColors.TextTertiary
        )
    }
}

/**
 * Capture utility interface to be implemented by parent screens
 */
interface BragCardController {
    fun shareCard()
}

// Helper function for formatting Spiritual Ego in BragCard
private fun formatSpiritualEgoBrag(spiritualEgo: Long): String {
    return when {
        spiritualEgo >= 1_000_000_000L -> String.format("%.1fB", spiritualEgo / 1_000_000_000.0)
        spiritualEgo >= 1_000_000L -> String.format("%.1fM", spiritualEgo / 1_000_000.0)
        spiritualEgo >= 1_000L -> String.format("%.1fK", spiritualEgo / 1_000.0)
        else -> spiritualEgo.toString()
    }
}

