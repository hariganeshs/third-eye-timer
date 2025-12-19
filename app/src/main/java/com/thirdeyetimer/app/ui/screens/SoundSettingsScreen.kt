package com.thirdeyetimer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thirdeyetimer.app.ui.components.*
import com.thirdeyetimer.app.ui.theme.CosmicColors
import com.thirdeyetimer.app.ui.theme.CosmicShapes

data class SoundOption(
    val id: Int,
    val title: String,
    val icon: ImageVector
)

/**
 * SoundSettingsScreen
 * 
 * Allows users to customize their meditation experience with bells,
 * ambient backgrounds, and guided meditations.
 */
@Composable
fun SoundSettingsScreen(
    selectedBellId: Int,
    selectedBackgroundId: Int,
    bells: List<SoundOption>,
    backgrounds: List<SoundOption>,
    onBellSelected: (Int) -> Unit,
    onBackgroundSelected: (Int) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
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
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CosmicColors.TextPrimary
                    )
                }
                
                Text(
                    text = "Soundscape",
                    style = MaterialTheme.typography.headlineMedium,
                    color = CosmicColors.TextPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                TabButton(
                    text = "Bells",
                    imageVector = Icons.Default.VolumeUp,
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                TabButton(
                    text = "Ambience",
                    imageVector = Icons.Default.MusicNote,
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // List content using Glassmorphic cards
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTab == 0) {
                    if (bells.isEmpty()) {
                        item {
                            Text(
                                text = "No bell sounds available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CosmicColors.TextTertiary
                            )
                        }
                    } else {
                        items(bells) { bell ->
                            SoundItemCard(
                                title = bell.title,
                                icon = bell.icon,
                                isSelected = bell.id == selectedBellId,
                                onClick = { onBellSelected(bell.id) }
                            )
                        }
                    }
                } else {
                    if (backgrounds.isEmpty()) {
                        item {
                            Text(
                                text = "No background sounds available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CosmicColors.TextTertiary
                            )
                        }
                    } else {
                        items(backgrounds) { bg ->
                            SoundItemCard(
                                title = bg.title,
                                icon = bg.icon,
                                isSelected = bg.id == selectedBackgroundId,
                                onClick = { onBackgroundSelected(bg.id) }
                            )
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    imageVector: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = CosmicShapes.pill,
        color = if (isSelected) CosmicColors.Primary else CosmicColors.GlassBackground
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = if (isSelected) CosmicColors.TextPrimary else CosmicColors.TextTertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) CosmicColors.TextPrimary else CosmicColors.TextTertiary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SoundItemCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = if (isSelected) 
            CosmicColors.Primary.copy(alpha = 0.2f) 
        else 
            CosmicColors.GlassBackground,
        borderColor = if (isSelected) 
            CosmicColors.Accent 
        else 
            CosmicColors.GlassBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) CosmicColors.Accent else CosmicColors.TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) CosmicColors.TextPrimary else CosmicColors.TextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = CosmicColors.Accent
                )
            }
        }
    }
}
