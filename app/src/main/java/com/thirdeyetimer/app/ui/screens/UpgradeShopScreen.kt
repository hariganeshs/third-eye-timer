package com.thirdeyetimer.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * UpgradeShopScreen
 * 
 * The upgrade shop where players spend Karma to purchase
 * permanent upgrades that increase their Spiritual Ego accumulation rate.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeShopScreen(
    totalSpiritualEgo: Long,
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
        // State for ironic popup
        var showIronicPopup by remember { mutableStateOf(false) }
        var lastPurchasedUpgrade by remember { mutableStateOf<UpgradeManager.Upgrade?>(null) }
        
        // Handle purchase with ironic follow-up
        val handlePurchase: (UpgradeManager.Upgrade) -> Unit = { upgrade ->
            onPurchaseUpgrade(upgrade)
            lastPurchasedUpgrade = upgrade
            showIronicPopup = true
        }
        
        // Ironic popup dialog
        if (showIronicPopup && lastPurchasedUpgrade != null) {
            IronicPurchaseDialog(
                upgrade = lastPurchasedUpgrade!!,
                onDismiss = { showIronicPopup = false }
            )
        }
        
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
                // Spiritual Ego balance
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
                            contentDescription = "Spiritual Ego",
                            tint = CosmicColors.Accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = formatLargeNumber(totalSpiritualEgo),
                                style = MaterialTheme.typography.titleMedium,
                                color = CosmicColors.TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Spiritual Ego",
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
                        onPurchase = { handlePurchase(status.upgrade) }
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

/**
 * Satirical dialog shown after purchasing an upgrade.
 * Frames the purchase as a reinforcement of the player's Spiritual Ego.
 */
@Composable
private fun IronicPurchaseDialog(
    upgrade: UpgradeManager.Upgrade,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0A0A), RoundedCornerShape(8.dp))
                .border(2.dp, Color(0xFFFF3B3B), RoundedCornerShape(8.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TRANSACTION COMPLETE",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFFFF3B3B),
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = upgrade.name.uppercase(),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // The "Truth Punch" description
            Text(
                text = "\"${upgrade.ironicDescription}\"",
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = Color(0xFFE0E0E0),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A1A1A),
                    contentColor = Color(0xFFFF3B3B)
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.border(1.dp, Color(0xFFFF3B3B).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            ) {
                Text(
                    text = "> I UNDERSTAND",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "[ Your Spiritual Ego has increased ]",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color(0xFF666666)
            )
        }
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
