package com.thirdeyetimer.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thirdeyetimer.app.domain.UpgradeManager
import com.thirdeyetimer.app.ui.components.*
import com.thirdeyetimer.app.ui.theme.CosmicColors

/**
 * UpgradeShopScreen
 * 
 * The upgrade shop where players spend Karma to purchase
 * permanent upgrades that increase their Prana accumulation rate.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeShopScreen(
    totalPrana: Long,
    karmaBalance: Int,
    upgradeStatuses: List<UpgradeManager.UpgradeStatus>,
    totalMultiplier: Double,
    onPurchaseUpgrade: (UpgradeManager.Upgrade) -> Unit,
    onWatchAdForKarma: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CosmicColors.BackgroundStart,
                        CosmicColors.BackgroundEnd
                    )
                )
            )
    ) {
        // Particle background
        ParticleBackground(
            particleCount = 25,
            enableAnimation = true
        )
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            TopAppBar(
                title = {
                    Text(
                        text = "Upgrades",
                        style = MaterialTheme.typography.headlineSmall,
                        color = CosmicColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = CosmicColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            
            // Balance cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Prana balance
                GlassmorphicCard(
                    modifier = Modifier.weight(1f),
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Prana",
                            tint = CosmicColors.Accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = formatLargeNumber(totalPrana),
                                style = MaterialTheme.typography.titleMedium,
                                color = CosmicColors.TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Prana",
                                style = MaterialTheme.typography.labelSmall,
                                color = CosmicColors.TextTertiary
                            )
                        }
                    }
                }
                
                // Karma balance
                GlassmorphicCard(
                    modifier = Modifier.weight(1f),
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "Karma",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = formatLargeNumber(karmaBalance.toLong()),
                                style = MaterialTheme.typography.titleMedium,
                                color = CosmicColors.TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Karma",
                                style = MaterialTheme.typography.labelSmall,
                                color = CosmicColors.TextTertiary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Current multiplier
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 12.dp,
                contentPadding = PaddingValues(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Multiplier",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CosmicColors.TextSecondary
                    )
                    Text(
                        text = "${String.format("%.2f", totalMultiplier)}x",
                        style = MaterialTheme.typography.titleMedium,
                        color = CosmicColors.Accent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Watch Ad for Karma button
            GlassmorphicButton(
                onClick = onWatchAdForKarma,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 16.dp,
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Watch Ad for +50 Karma",
                    style = MaterialTheme.typography.labelLarge,
                    color = CosmicColors.TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Upgrades list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(upgradeStatuses) { status ->
                    UpgradeCard(
                        status = status,
                        canAfford = karmaBalance >= status.cost && !status.isMaxed,
                        onPurchase = { onPurchaseUpgrade(status.upgrade) }
                    )
                }
                
                // Bottom spacer for navigation
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun UpgradeCard(
    status: UpgradeManager.UpgradeStatus,
    canAfford: Boolean,
    onPurchase: () -> Unit
) {
    val upgrade = status.upgrade
    
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        contentPadding = PaddingValues(16.dp),
        backgroundColor = if (status.isMaxed) 
            CosmicColors.Accent.copy(alpha = 0.1f) 
        else 
            CosmicColors.GlassBackground
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                CosmicColors.Primary.copy(alpha = 0.3f),
                                CosmicColors.Secondary.copy(alpha = 0.3f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getUpgradeIcon(upgrade.iconName),
                    contentDescription = null,
                    tint = CosmicColors.Accent,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = upgrade.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = CosmicColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Level badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (status.isMaxed) CosmicColors.Accent
                                else CosmicColors.GlassHighlight
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (status.isMaxed) "MAX" else "Lv.${status.currentLevel}/${upgrade.maxLevel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (status.isMaxed) CosmicColors.BackgroundStart else CosmicColors.TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Text(
                    text = upgrade.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = CosmicColors.TextTertiary
                )
            }
            
            // Purchase button
            if (!status.isMaxed) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onPurchase,
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canAfford) CosmicColors.Primary else CosmicColors.GlassBackground,
                            disabledContainerColor = CosmicColors.GlassBackground
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = if (canAfford) Color(0xFFFFD700) else CosmicColors.TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatLargeNumber(status.cost.toLong()),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (canAfford) CosmicColors.TextPrimary else CosmicColors.TextMuted
                        )
                    }
                }
            }
        }
    }
}

private fun getUpgradeIcon(iconName: String): ImageVector {
    return when (iconName) {
        "focus" -> Icons.Default.CenterFocusStrong
        "chakra" -> Icons.Default.Spa
        "third_eye" -> Icons.Default.Visibility
        "cosmic" -> Icons.Default.AllInclusive
        "breath" -> Icons.Default.Air
        "lotus" -> Icons.Default.FilterVintage
        else -> Icons.Default.Upgrade
    }
}

private fun formatLargeNumber(value: Long): String {
    return when {
        value >= 1_000_000_000L -> String.format("%.1fB", value / 1_000_000_000.0)
        value >= 1_000_000L -> String.format("%.1fM", value / 1_000_000.0)
        value >= 1_000L -> String.format("%.1fK", value / 1_000.0)
        else -> value.toString()
    }
}
