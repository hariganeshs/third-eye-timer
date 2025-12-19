package com.thirdeyetimer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.CheckCircle

@Composable
fun QuestBoardScreen(
    onBackClick: () -> Unit,
    onWatchAdForStardust: () -> Unit // Rewarded Ad
) {
    val context = LocalContext.current
    val questManager = remember { QuestManager(context) }
    
    // State
    var stardust by remember { mutableStateOf(questManager.stardust) }
    val quests = remember { questManager.quests } // In real MVVM, observe flow
    
    // Force recomposition on updates - simplistic approach
    var refreshTrigger by remember { mutableStateOf(0) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1A))
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
                        imageVector = androidx.compose.material.icons.Icons.Filled.ArrowBack,
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
                
                // Stardust Balance
                Surface(
                    color = Color(0xFF312E81),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Star,
                            contentDescription = "Stardust",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$stardust",
                            color = Color(0xFFFBBF24),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Quest List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(quests) { quest ->
                    QuestItem(quest)
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { 
                            onWatchAdForStardust()
                            questManager.addStardust(50)
                            stardust = questManager.stardust
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                         Icon(
                             imageVector = androidx.compose.material.icons.Icons.Filled.Videocam,
                             contentDescription = "Watch Ad",
                             tint = Color.White
                         )
                         Spacer(modifier = Modifier.width(8.dp))
                         Text("Watch Ad (+50 Stardust)")
                    }
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
                    progress = quest.progress.toFloat() / quest.target.toFloat(),
                    modifier = Modifier.fillMaxWidth().height(8.dp),
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
                     imageVector = androidx.compose.material.icons.Icons.Filled.CheckCircle,
                     contentDescription = "Completed",
                     tint = Color(0xFF10B981),
                     modifier = Modifier.size(24.dp)
                 )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                     Text("+${quest.reward}", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold)
                     Icon(
                         imageVector = androidx.compose.material.icons.Icons.Filled.Star,
                         contentDescription = "Stardust",
                         tint = Color(0xFFFBBF24),
                         modifier = Modifier.size(12.dp)
                     )
                }
            }
        }
    }
}
