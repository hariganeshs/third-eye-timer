package com.thirdeyetimer.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thirdeyetimer.app.ui.components.*
import com.thirdeyetimer.app.ui.theme.CosmicColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Upgrade

/**
 * HomeScreen
 * 
 * The main home screen for the Third Eye Timer meditation app.
 * Features a stunning cosmic design with breathing animation and quick actions.
 */
@Composable
fun HomeScreen(
    timeInput: String,
    onTimeInputChange: (String) -> Unit,
    totalMeditationTime: String,
    currentStreak: Int,
    userLevel: String = "Seeker",
    karmaPoints: Int = 0,
    totalPrana: Long = 0L,
    onStartClick: () -> Unit,
    onSoundSettingsClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onPetClick: () -> Unit,
    onQuestsClick: () -> Unit,
    onUpgradeShopClick: () -> Unit = {},
    onBrowseSessionsClick: () -> Unit = {},
    isTimerRunning: Boolean = false,
    timerText: String = "00:00",
    progress: Float = 0f,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Cosmic particle background
        ParticleBackground(
            particleCount = 40,
            enableAnimation = !isTimerRunning
        )
        
        // Twinkling stars overlay
        AnimatedStars(
            starCount = 20
        )
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // App Title
            Text(
                text = "Third Eye Timer",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp
                ),
                color = CosmicColors.Accent,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Find your inner peace",
                style = MaterialTheme.typography.bodyMedium,
                color = CosmicColors.TextTertiary,
                textAlign = TextAlign.Center
            )
            
            // Prana and Karma display row
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .background(
                        color = CosmicColors.GlassHighlight,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prana
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "Prana",
                        tint = CosmicColors.Accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = formatPranaHome(totalPrana),
                        style = MaterialTheme.typography.labelMedium,
                        color = CosmicColors.Accent,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(CosmicColors.GlassBorder)
                )
                
                // Karma
                Text(
                    text = "$karmaPoints Karma",
                    style = MaterialTheme.typography.labelMedium,
                    color = CosmicColors.TextSecondary
                )
                
                // Level
                Text(
                    text = userLevel,
                    style = MaterialTheme.typography.labelMedium,
                    color = CosmicColors.TextTertiary
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Meditation Timer with breathing circle
            Box(
                modifier = Modifier.size(320.dp),
                contentAlignment = Alignment.Center
            ) {
                MeditationTimer(
                    timeText = if (isTimerRunning) timerText else timeInput.ifEmpty { "00" } + ":00",
                    progress = progress,
                    isRunning = isTimerRunning,
                    size = 300.dp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Time Input (only show when not running)
            if (!isTimerRunning) {
                GlassmorphicCard(
                    modifier = Modifier.width(200.dp),
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = timeInput,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                                onTimeInputChange(newValue)
                            }
                        },
                        placeholder = {
                            Text(
                                "Minutes",
                                color = CosmicColors.TextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            color = CosmicColors.TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicColors.Primary,
                            unfocusedBorderColor = CosmicColors.GlassBorder,
                            cursorColor = CosmicColors.Primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Stats Card
            GlassmorphicCardWithGlow(
                modifier = Modifier.fillMaxWidth(),
                glowColor = if (currentStreak > 0) CosmicColors.GlowGold else CosmicColors.GlowIndigo,
                cornerRadius = 20.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Streak
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (currentStreak > 0) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Filled.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = Color(0xFFFF5722), // Deep Orange
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                             Icon(
                                 imageVector = androidx.compose.material.icons.Icons.Filled.Star,
                                 contentDescription = "No Streak",
                                 tint = CosmicColors.Accent,
                                 modifier = Modifier.size(32.dp)
                             )
                        }
                        Text(
                            text = "$currentStreak Day${if (currentStreak != 1) "s" else ""}",
                            style = MaterialTheme.typography.titleMedium,
                            color = CosmicColors.Accent,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Streak",
                            style = MaterialTheme.typography.bodySmall,
                            color = CosmicColors.TextTertiary
                        )
                    }
                    
                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(50.dp)
                            .background(CosmicColors.GlassBorder)
                    )
                    
                    // Total Time
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Schedule,
                            contentDescription = "Total Time",
                            tint = CosmicColors.Accent,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = totalMeditationTime,
                            style = MaterialTheme.typography.titleMedium,
                            color = CosmicColors.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.bodySmall,
                            color = CosmicColors.TextTertiary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Daily Quests
            GlassmorphicButton(
                onClick = onQuestsClick,
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Row(
                   verticalAlignment = Alignment.CenterVertically,
                   horizontalArrangement = Arrangement.Center,
                   modifier = Modifier.fillMaxWidth()
                ) {
                     Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Assignment,
                        contentDescription = "Quests",
                        tint = CosmicColors.Accent,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Daily Quests",
                            style = MaterialTheme.typography.titleMedium,
                            color = CosmicColors.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Complete tasks for Karma ✨",
                            style = MaterialTheme.typography.labelSmall,
                            color = CosmicColors.TextSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quick Actions Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sound Settings
                GlassmorphicButton(
                    onClick = onSoundSettingsClick,
                    modifier = Modifier.weight(1f),
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.MusicNote,
                            contentDescription = "Sounds",
                            tint = CosmicColors.Accent,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sounds",
                            style = MaterialTheme.typography.labelMedium,
                            color = CosmicColors.TextSecondary
                        )
                    }
                }
                
                // Browse Sessions
                GlassmorphicButton(
                    onClick = onBrowseSessionsClick,
                    modifier = Modifier.weight(1f),
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                         Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.AutoStories,
                            contentDescription = "Sessions",
                            tint = CosmicColors.Accent,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sessions",
                            style = MaterialTheme.typography.labelMedium,
                            color = CosmicColors.TextSecondary
                        )
                    }
                }
                
                // Achievements
                GlassmorphicButton(
                    onClick = onAchievementsClick,
                    modifier = Modifier.weight(1f),
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                         Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.EmojiEvents,
                            contentDescription = "Awards",
                            tint = CosmicColors.Accent,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Awards",
                            style = MaterialTheme.typography.labelMedium,
                            color = CosmicColors.TextSecondary
                        )
                    }
                }
                
                // Pet
                GlassmorphicButton(
                    onClick = onPetClick,
                    modifier = Modifier.weight(1f),
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                         Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Pets,
                            contentDescription = "My Pet",
                            tint = CosmicColors.Accent,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "My Pet",
                            style = MaterialTheme.typography.labelMedium,
                            color = CosmicColors.TextSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Upgrades Button - Access the upgrade shop
            GlassmorphicButton(
                onClick = onUpgradeShopClick,
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Upgrade,
                        contentDescription = "Upgrades",
                        tint = CosmicColors.Accent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Upgrades",
                        style = MaterialTheme.typography.titleMedium,
                        color = CosmicColors.TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Boost your Prana",
                        style = MaterialTheme.typography.labelSmall,
                        color = CosmicColors.TextTertiary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Start Button
            PrimaryGradientButton(
                onClick = onStartClick,
                enabled = timeInput.isNotEmpty() || isTimerRunning,
                modifier = Modifier.fillMaxWidth(0.7f),
                cornerRadius = 32.dp,
                contentPadding = PaddingValues(vertical = 18.dp)
            ) {
                Text(
                    text = if (isTimerRunning) "Pause" else "Start",
                    style = MaterialTheme.typography.titleMedium,
                    color = CosmicColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // Space for bottom ad
        }
    }
}

/**
 * Preview composable for HomeScreen
 */
@Composable
fun HomeScreenPreview() {
    var timeInput by remember { mutableStateOf("10") }
    
    HomeScreen(
        timeInput = timeInput,
        onTimeInputChange = { timeInput = it },
        totalMeditationTime = "12h 30m",
        currentStreak = 5,
        userLevel = "Adept",
        karmaPoints = 4500,
        totalPrana = 12500L,
        onStartClick = {},
        onSoundSettingsClick = {},
        onAchievementsClick = {},
        onPetClick = {},
        onQuestsClick = {}
    )
}

// Helper function for formatting Prana in HomeScreen
private fun formatPranaHome(prana: Long): String {
    return when {
        prana >= 1_000_000_000L -> String.format("%.1fB", prana / 1_000_000_000.0)
        prana >= 1_000_000L -> String.format("%.1fM", prana / 1_000_000.0)
        prana >= 1_000L -> String.format("%.1fK", prana / 1_000.0)
        else -> prana.toString()
    }
}
