package com.thirdeyetimer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thirdeyetimer.app.GuidedMeditationData
import com.thirdeyetimer.app.MeditationCategory
import com.thirdeyetimer.app.ui.components.*
import com.thirdeyetimer.app.ui.theme.CosmicColors
import kotlinx.coroutines.launch

/**
 * SessionsScreen
 * 
 * Displays a list of guided meditation categories and featured sessions.
 * This screen allows users to browse and select guided meditations.
 */
@Composable
fun SessionsScreen(
    onBackClick: () -> Unit,
    onMeditationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = GuidedMeditationData.CATEGORY_INFO.entries.toList()
    var selectedCategory by remember { mutableStateOf<MeditationCategory?>(null) }
    
    // Override back button to handle category navigation
    // Note: We're not using BackHandler here because this is a simple state-based navigation within one screen
    // The parent handles the actual system back press
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicColors.BackgroundStart)
    ) {
        // Starry background
        CosmicBackground(
            particleCount = 25
        )
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Header with Back Navigation logic
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                IconButton(onClick = {
                    if (selectedCategory != null) {
                        selectedCategory = null
                    } else {
                        onBackClick()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CosmicColors.TextPrimary
                    )
                }
                
                Text(
                    text = if (selectedCategory != null) 
                        GuidedMeditationData.CATEGORY_INFO[selectedCategory]?.first ?: "Sessions" 
                        else "Guided Sessions",
                    style = MaterialTheme.typography.headlineMedium,
                    color = CosmicColors.TextPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Main Content Area
            if (selectedCategory == null) {
                // CATEGORY LIST VIEW
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Info Banner
                    item {
                        GlassmorphicCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            backgroundColor = CosmicColors.Primary.copy(alpha = 0.2f),
                            borderColor = CosmicColors.Accent
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "✨ Discover Inner Peace",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CosmicColors.Accent,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Choose a category below to explore our collection of guided meditations.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CosmicColors.TextSecondary
                                )
                            }
                        }
                    }

                    // Categories
                    items(categories) { (category, info) ->
                        val (title, icon, description) = info
                        
                        GlassmorphicCardWithGlow(
                            modifier = Modifier.fillMaxWidth(),
                            glowColor = CosmicColors.Secondary,
                            onClick = { selectedCategory = category }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Icon container
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            color = CosmicColors.GlassHighlight.copy(alpha = 0.1f),
                                            shape = MaterialTheme.shapes.medium
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = icon,
                                        fontSize = 24.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = CosmicColors.TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CosmicColors.TextTertiary,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            } else {
                // MEDITATION TRACK LIST VIEW
                val meditations = remember(selectedCategory) {
                    GuidedMeditationData.getMeditationsByCategory(selectedCategory!!)
                }
                
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (meditations.isEmpty()) {
                        item {
                            Text(
                                text = "No meditations available in this category yet.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = CosmicColors.TextSecondary,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(meditations) { meditation ->
                            GlassmorphicCardWithGlow(
                                modifier = Modifier.fillMaxWidth(),
                                glowColor = CosmicColors.Primary,
                                onClick = { onMeditationSelected(meditation.resourceId) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Play Icon
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                color = CosmicColors.GlassHighlight.copy(alpha = 0.2f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "▶",
                                            color = CosmicColors.Accent,
                                            fontSize = 20.sp
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = meditation.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = CosmicColors.TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${meditation.duration} • ${meditation.description}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CosmicColors.TextSecondary,
                                            maxLines = 2
                                        )
                                    }
                                }
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
}

