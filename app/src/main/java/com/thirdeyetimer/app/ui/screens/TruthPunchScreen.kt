package com.thirdeyetimer.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thirdeyetimer.app.domain.TruthPunchManager

/**
 * TruthPunchScreen
 * 
 * Displays the hierarchical "Truth Punch" system with Terminal/Brutalist aesthetic.
 * Locked truths show only ominous titles. Unlocked truths reveal harsh philosophical truths.
 * 
 * Design Philosophy:
 * - Black background, monospace fonts, harsh red accents
 * - Minimal ornamentation, maximum impact
 * - Truths are delivered as "blows" not gentle revelations
 */

// Terminal/Brutalist color scheme
private val TerminalBlack = Color(0xFF0A0A0A)
private val TerminalDarkGray = Color(0xFF1A1A1A)
private val TerminalRed = Color(0xFFFF3B3B)
private val TerminalAmber = Color(0xFFFFB000)
private val TerminalGreen = Color(0xFF00FF41)
private val TerminalWhite = Color(0xFFE0E0E0)
private val TerminalGray = Color(0xFF666666)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TruthPunchScreen(
    truths: List<TruthPunchManager.TruthPunch>,
    currentSpiritualEgo: Long,
    nextUnlockThreshold: Long,
    overallProgress: Float,
    onBackClick: () -> Unit,
    onTruthClick: (TruthPunchManager.TruthPunch) -> Unit,
    getTierName: (Int) -> String,
    getTierSubtitle: (Int) -> String,
    modifier: Modifier = Modifier
) {
    var selectedTruth by remember { mutableStateOf<TruthPunchManager.TruthPunch?>(null) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBlack)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Terminal-style header
            TruthHeader(
                overallProgress = overallProgress,
                currentSpiritualEgo = currentSpiritualEgo,
                nextUnlockThreshold = nextUnlockThreshold,
                onBackClick = onBackClick
            )
            
            // Truth list grouped by tier
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Group truths by tier (5 -> 1)
                val groupedTruths = truths.groupBy { it.tier }.toSortedMap(reverseOrder())
                
                groupedTruths.forEach { (tier, tierTruths) ->
                    // Tier header
                    item {
                        TierHeader(
                            tier = tier,
                            tierName = getTierName(tier),
                            tierSubtitle = getTierSubtitle(tier),
                            unlockedCount = tierTruths.count { it.isUnlocked },
                            totalCount = tierTruths.size
                        )
                    }
                    
                    // Truths in this tier
                    items(tierTruths.sortedByDescending { it.rank }) { truth ->
                        TruthCard(
                            truth = truth,
                            onClick = { 
                                if (truth.isUnlocked) {
                                    selectedTruth = truth
                                    onTruthClick(truth)
                                }
                            }
                        )
                    }
                    
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
        
        // Truth detail popup
        selectedTruth?.let { truth ->
            TruthDetailPopup(
                truth = truth,
                onDismiss = { selectedTruth = null }
            )
        }
    }
}

@Composable
private fun TruthHeader(
    overallProgress: Float,
    currentSpiritualEgo: Long,
    nextUnlockThreshold: Long,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalDarkGray)
            .padding(16.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TerminalWhite
                )
            }
            
            Text(
                text = "TERMINAL OF TRUTH",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TerminalRed,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.width(48.dp))
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Progress bar
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DISILLUSIONMENT: ${overallProgress.toInt()}%",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = TerminalAmber
                )
                Text(
                    text = "${(100 - overallProgress).toInt()} TRUTHS REMAINING",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = TerminalGray
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            LinearProgressIndicator(
                progress = { overallProgress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = TerminalRed,
                trackColor = TerminalGray.copy(alpha = 0.3f)
            )
        }
        
        // Next unlock progress
        if (nextUnlockThreshold < Long.MAX_VALUE) {
            Spacer(modifier = Modifier.height(8.dp))
            val progressToNext = (currentSpiritualEgo.toFloat() / nextUnlockThreshold).coerceIn(0f, 1f)
            Text(
                text = "NEXT TRUTH: ${formatSpiritualEgoCompact(currentSpiritualEgo)} / ${formatSpiritualEgoCompact(nextUnlockThreshold)}",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = TerminalGreen.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun TierHeader(
    tier: Int,
    tierName: String,
    tierSubtitle: String,
    unlockedCount: Int,
    totalCount: Int
) {
    val tierColor = when (tier) {
        5 -> Color(0xFF4CAF50)  // Green - easiest
        4 -> Color(0xFF2196F3)  // Blue
        3 -> Color(0xFFFF9800)  // Orange
        2 -> Color(0xFFE91E63)  // Pink
        1 -> TerminalRed        // Red - hardest
        else -> TerminalWhite
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TIER $tier",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = tierColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "[$unlockedCount/$totalCount]",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = TerminalGray
            )
        }
        
        Text(
            text = tierName.uppercase(),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TerminalWhite
        )
        
        Text(
            text = tierSubtitle,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = TerminalGray
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Divider(
            color = tierColor.copy(alpha = 0.5f),
            thickness = 1.dp
        )
    }
}

@Composable
private fun TruthCard(
    truth: TruthPunchManager.TruthPunch,
    onClick: () -> Unit
) {
    val isLocked = !truth.isUnlocked
    val isNew = truth.isUnlocked && !truth.isSeen
    
    val borderColor = when {
        isNew -> TerminalGreen
        isLocked -> TerminalGray.copy(alpha = 0.3f)
        else -> TerminalGray.copy(alpha = 0.5f)
    }
    
    val backgroundColor = when {
        isLocked -> TerminalBlack
        isNew -> TerminalDarkGray.copy(alpha = 0.8f)
        else -> TerminalDarkGray.copy(alpha = 0.5f)
    }
    
    // Glitch animation for new truths
    val infiniteTransition = rememberInfiniteTransition(label = "glitch")
    val glitchAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glitchAlpha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(if (isNew) glitchAlpha else 1f)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable(enabled = truth.isUnlocked) { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank number
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isLocked) TerminalGray.copy(alpha = 0.2f)
                        else TerminalRed.copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLocked) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = TerminalGray,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "#${truth.rank}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TerminalRed
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Title and preview
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = truth.title,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isLocked) TerminalGray else TerminalWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (truth.isUnlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = truth.truth,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TerminalGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "[ LOCKED - Accumulate more Spiritual Ego to unlock ]",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = TerminalGray.copy(alpha = 0.5f)
                    )
                }
            }
            
            // New indicator
            if (isNew) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NEW",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = TerminalGreen,
                    modifier = Modifier
                        .background(TerminalGreen.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun TruthDetailPopup(
    truth: TruthPunchManager.TruthPunch,
    onDismiss: () -> Unit
) {
    // Screen shake animation
    val shakeOffset by rememberInfiniteTransition(label = "shake").animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(50),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeOffset"
    )
    
    // Only shake for first 500ms
    var shouldShake by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(500)
        shouldShake = false
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .clickable { onDismiss() }
            .offset(x = if (shouldShake) shakeOffset.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(TerminalDarkGray, RoundedCornerShape(8.dp))
                .border(2.dp, TerminalRed, RoundedCornerShape(8.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "TRUTH #${truth.rank}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = TerminalRed
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = truth.title,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TerminalAmber,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Divider
            Divider(color = TerminalRed.copy(alpha = 0.5f))
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // The Truth
            Text(
                text = "\"${truth.truth}\"",
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                color = TerminalWhite,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Tier indicator
            Text(
                text = "TIER ${truth.tier} • ${getTierNameLocal(truth.tier)}",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = TerminalGray
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Dismiss hint
            Text(
                text = "[ TAP ANYWHERE TO DISMISS ]",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = TerminalGray.copy(alpha = 0.5f)
            )
        }
    }
}

private fun getTierNameLocal(tier: Int): String {
    return when (tier) {
        5 -> "THE OPERATIONAL LIES"
        4 -> "THE HOLLOW PURSUIT"
        3 -> "THE TRANSACTIONAL HEART"
        2 -> "THE SPIRITUAL SCAM"
        1 -> "THE ANNIHILATION"
        else -> "UNKNOWN"
    }
}

private fun formatSpiritualEgoCompact(value: Long): String {
    return when {
        value >= 1_000_000_000L -> String.format("%.1fB", value / 1_000_000_000.0)
        value >= 1_000_000L -> String.format("%.1fM", value / 1_000_000.0)
        value >= 1_000L -> String.format("%.1fK", value / 1_000.0)
        else -> value.toString()
    }
}
