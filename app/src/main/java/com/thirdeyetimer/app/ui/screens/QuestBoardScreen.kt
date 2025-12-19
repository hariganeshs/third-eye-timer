package com.thirdeyetimer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thirdeyetimer.app.domain.QuestManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import kotlinx.coroutines.delay

@Composable
fun QuestBoardScreen(
    onBackClick: () -> Unit,
    onWatchAdForStardust: () -> Unit // Renamed but kept for compatibility
) {
    val context = LocalContext.current
    val questManager = remember { QuestManager(context) }
    
    // State
    val quests = remember { questManager.quests }
    
    // Cooldown tracking
    var cooldownText by remember { mutableStateOf(questManager.formatCooldown()) }
    var isAdAvailable by remember { mutableStateOf(questManager.isAdRewardAvailable()) }
    
    // Update cooldown every second
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            cooldownText = questManager.formatCooldown()
            isAdAvailable = questManager.isAdRewardAvailable()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F0F1A), Color(0xFF1A1A2E))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Daily Quests",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Info text
            Text(
                text = "Complete quests to earn Karma!",
                color = Color(0xFFB0B0B0),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Quest List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(quests) { quest ->
                    QuestItem(quest)
                }
                
                // Watch Ad for Karma button with cooldown
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { 
                            if (isAdAvailable) {
                                onWatchAdForStardust()  // Uses the callback which now handles cooldown
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = isAdAvailable,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAdAvailable) Color(0xFF6366F1) else Color(0xFF3D3D5C),
                            disabledContainerColor = Color(0xFF3D3D5C)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isAdAvailable) {
                            Icon(
                                imageVector = Icons.Filled.PlayCircle,
                                contentDescription = "Watch Ad",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Watch Ad (+${QuestManager.AD_REWARD_KARMA} Karma)",
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = "Cooldown",
                                tint = Color(0xFFB0B0B0)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Next Ad: $cooldownText",
                                color = Color(0xFFB0B0B0)
                            )
                        }
                    }
                }
                
                // Bottom spacer
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun QuestItem(quest: QuestManager.Quest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quest.description,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { quest.progress.toFloat() / quest.target.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = if (quest.isCompleted) Color(0xFF10B981) else Color(0xFF6366F1),
                    trackColor = Color(0xFF2D2D44)
                )
                Text(
                    text = "${quest.progress} / ${quest.target}",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            if (quest.isCompleted) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Completed",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "+${quest.reward}",
                        color = Color(0xFFFBBF24),
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Filled.Stars,
                        contentDescription = "Karma",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
