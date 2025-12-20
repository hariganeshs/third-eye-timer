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
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import com.thirdeyetimer.app.R
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
import androidx.compose.material.icons.filled.Psychology
import com.thirdeyetimer.app.domain.TruthPunchManager

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
    totalSpiritualEgo: Long = 0L,
    lifetimeSpiritualEgo: Long = 0L,
    allTruths: List<TruthPunchManager.TruthPunch> = emptyList(),
    onStartClick: () -> Unit,
    onSoundSettingsClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onPetClick: () -> Unit,
    onQuestsClick: () -> Unit,
    onUpgradeShopClick: () -> Unit = {},
    onTruthsClick: () -> Unit = {},
    onBrowseSessionsClick: () -> Unit = {},
    unseenTruthCount: Int = 0,
    isTimerRunning: Boolean = false,
    timerText: String = "00:00",
    progress: Float = 0f,
    isWaitWallActive: Boolean = false,
    waitWallRemainingMs: Long = 0L,
    onWatchAdToBypassWaitWall: () -> Unit = {},
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
            
            // Spiritual Ego and Karma display row
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
                // Spiritual Ego
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_spiritual_ego),
                        contentDescription = "Spiritual Ego",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = formatSpiritualEgoHome(totalSpiritualEgo),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_karma),
                        contentDescription = "Karma",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "$karmaPoints",
                        style = MaterialTheme.typography.labelMedium,
                        color = CosmicColors.TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Level
                Text(
                    text = userLevel,
                    style = MaterialTheme.typography.labelMedium,
                    color = CosmicColors.TextTertiary
                )
            }
            
            // Wait Wall / Fatigue Banner
            if (isWaitWallActive && !isTimerRunning) {
                Spacer(modifier = Modifier.height(16.dp))
                
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 12.dp,
                    backgroundColor = Color(0xFF1A0A0A), // Darker, ominous
                    borderWidth = 2.dp,
                    borderColor = Color(0xFFFF3B3B).copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "MEDITATIVE FATIGUE DETECTED",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF3B3B),
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "System needs ${waitWallRemainingMs / 60000 + 1}m to cool down.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CosmicColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TerminalButton(
                            text = "TAKE THE SHORTCUT",
                            onClick = onWatchAdToBypassWaitWall,
                            color = Color(0xFFFF3B3B),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Meditation Timer with breathing circle
            val disintegrationLevel = (lifetimeSpiritualEgo / 1_000_000f).coerceIn(0f, 1f)
            
            Box(
                modifier = Modifier.size(320.dp),
                contentAlignment = Alignment.Center
            ) {
                MeditationTimer(
                    timeText = if (isTimerRunning) timerText else timeInput.ifEmpty { "00" } + ":00",
                    progress = progress,
                    isRunning = isTimerRunning,
                    size = 300.dp,
                    disintegrationLevel = disintegrationLevel
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
                        text = "Boost your Spiritual Ego",
                        style = MaterialTheme.typography.labelSmall,
                        color = CosmicColors.TextTertiary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Truths Button - Access the Truth Punch system
            GlassmorphicButton(
                onClick = onTruthsClick,
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
                        imageVector = Icons.Filled.Psychology,
                        contentDescription = "Truths",
                        tint = Color(0xFFFF3B3B), // Terminal Red
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Truths",
                        style = MaterialTheme.typography.titleMedium,
                        color = CosmicColors.TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    if (unseenTruthCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFF3B3B), MaterialTheme.shapes.small)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$unseenTruthCount NEW",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Terminal of Truth",
                            style = MaterialTheme.typography.labelSmall,
                            color = CosmicColors.TextTertiary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Start Button
            PrimaryGradientButton(
                onClick = onStartClick,
                enabled = (timeInput.isNotEmpty() || isTimerRunning) && !isWaitWallActive,
                modifier = Modifier.fillMaxWidth(0.7f),
                cornerRadius = 32.dp,
                contentPadding = PaddingValues(vertical = 18.dp)
            ) {
                Text(
                    text = if (isTimerRunning) "Pause" else if (isWaitWallActive) "Locked" else "Start",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isWaitWallActive) CosmicColors.TextMuted else CosmicColors.TextPrimary,
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
        totalSpiritualEgo = 12500L, 
        onStartClick = {},
        onSoundSettingsClick = {},
        onAchievementsClick = {},
        onPetClick = {},
        onQuestsClick = {},
        onUpgradeShopClick = {},
        onTruthsClick = {},
        onBrowseSessionsClick = {}
    )
}

// Helper function for formatting Spiritual Ego in HomeScreen
private fun formatSpiritualEgoHome(spiritualEgo: Long): String {
    return when {
        spiritualEgo >= 1_000_000_000L -> String.format("%.1fB", spiritualEgo / 1_000_000_000.0)
        spiritualEgo >= 1_000_000L -> String.format("%.1fM", spiritualEgo / 1_000_000.0)
        spiritualEgo >= 1_000L -> String.format("%.1fK", spiritualEgo / 1_000.0)
        else -> spiritualEgo.toString()
    }
}
