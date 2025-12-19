package com.thirdeyetimer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thirdeyetimer.app.ui.components.*
import com.thirdeyetimer.app.ui.theme.CosmicColors

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean
)

/**
 * AchievementsScreen
 * 
 * Displays user's meditation achievements, badges, and streaks.
 * Uses a grid layout for badges and a heatmap-style calendar for streaks.
 */
@Composable
fun AchievementsScreen(
    currentStreak: Int,
    longestStreak: Int,
    totalTime: String,
    achievements: List<AchievementItem>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicColors.BackgroundStart)
    ) {
        // Starry background
        CosmicBackground(
            particleCount = 30
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CosmicColors.TextPrimary
                    )
                }
                
                Text(
                    text = "Your Journey",
                    style = MaterialTheme.typography.headlineMedium,
                    color = CosmicColors.TextPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Streak Stats
                item {
                    GlassmorphicCardWithGlow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        glowColor = CosmicColors.GlowGold
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatItem(
                                icon = "🔥",
                                value = "$currentStreak",
                                label = "Current Streak",
                                valueColor = CosmicColors.Accent
                            )
                            StatItem(
                                icon = "⚡",
                                value = "$longestStreak",
                                label = "Best Streak",
                                valueColor = CosmicColors.TextPrimary
                            )
                            StatItem(
                                icon = "🧘",
                                value = totalTime,
                                label = "Total Time",
                                valueColor = CosmicColors.Secondary
                            )
                        }
                    }
                }
                
                // Badges Section
                item {
                    Text(
                        text = "Badges",
                        style = MaterialTheme.typography.titleLarge,
                        color = CosmicColors.TextSecondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                // Achievement Grid
                // Note: Nested LazyVerticalGrid inside LazyColumn needs fixed height or distinct layout approach
                // Here we calculate items per row to render manually or use a simple Column since grid items are limited
                
                if (achievements.isEmpty()) {
                    item {
                        Text(
                            text = "No badges yet. Start meditating to earn achievements!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CosmicColors.TextTertiary
                        )
                    }
                } else {
                    items(achievements.chunked(2)) { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            for (item in rowItems) {
                                AchievementCard(
                                    achievement = item,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(bottom = 16.dp)
                                )
                            }
                            // Fill empty space if odd number of items
                            if (rowItems.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: String,
    value: String,
    label: String,
    valueColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 24.sp)
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = CosmicColors.TextTertiary
        )
    }
}

@Composable
private fun AchievementCard(
    achievement: AchievementItem,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier,
        backgroundColor = if (achievement.isUnlocked) 
            CosmicColors.GlassBackground 
        else 
            CosmicColors.GlassBackground.copy(alpha = 0.05f),
        borderColor = if (achievement.isUnlocked) 
            CosmicColors.Accent.copy(alpha = 0.5f)
        else 
            CosmicColors.GlassBorder.copy(alpha = 0.3f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!achievement.isUnlocked) {
                    // Lock overlay
                    Text(
                        text = "🔒",
                        fontSize = 24.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                Text(
                    text = achievement.icon,
                    fontSize = 32.sp,
                    color = if (achievement.isUnlocked) 
                        Color.Unspecified 
                    else 
                        Color.Gray.copy(alpha = 0.3f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (achievement.isUnlocked) 
                    CosmicColors.TextPrimary 
                else 
                    CosmicColors.TextMuted,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = CosmicColors.TextTertiary,
                textAlign = TextAlign.Center,
                minLines = 2
            )
        }
    }
}
