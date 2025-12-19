package com.thirdeyetimer.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thirdeyetimer.app.domain.PetManager
import com.thirdeyetimer.app.ui.theme.*

@Composable
fun PetScreen(
    onBackClick: () -> Unit,
    onFeedClick: () -> Unit // Trigger Ad
) {
    val context = LocalContext.current
    val petManager = remember { PetManager(context) }
    
    // State to trigger recomposition when values change
    var petExp by remember { mutableStateOf(petManager.exp) }
    var petStage by remember { mutableStateOf(petManager.stage) }
    var petMood by remember { mutableStateOf(petManager.mood) }
    var showHeartAnimation by remember { mutableStateOf(false) }

    // Animation for breathing/floating effect
    val infiniteTransition = rememberInfiniteTransition(label = "petFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1E2E), // Dark Purple
                        Color(0xFF0F0F1A)  // Nearly Black
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Text("🔙", fontSize = 24.sp)
                }
                Text(
                    text = "Cosmic Companion",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Pet Display Area
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .graphicsLayer { translationY = floatOffset },
                contentAlignment = Alignment.Center
            ) {
                // Glow effect
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0x666366F1), // Inner glow
                                    Color.Transparent // Outer
                                )
                            )
                        )
                )
                
                // The Pet (Emoji for now, could be Image later)
                Text(
                    text = petManager.getPetEmoji(),
                    fontSize = 150.sp
                )
                
                // Heart animation on feed
                if (showHeartAnimation) {
                     Text(
                        text = "💖",
                        fontSize = 60.sp,
                        modifier = Modifier.align(Alignment.TopEnd)
                     )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Name & Status
            Text(
                text = petManager.getPetName(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFBBF24) // Gold
            )
            
            Text(
                text = petManager.getPetDescription(),
                fontSize = 14.sp,
                color = Color(0xFFA5B4FC), // Light Purple
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D44))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Mood
                    Text("Mood", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = petMood / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = if (petMood > 50) Color(0xFF10B981) else Color(0xFFEF4444),
                        trackColor = Color(0xFF1F1F2E)
                    )
                    Text(
                        text = if (petMood > 50) "Happy" else "Needs Attention! (Meditate or Feed)",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Evolution
                    Text("Evolution Progress", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val maxExp = when(petStage) {
                        PetManager.STAGE_EGG -> PetManager.EXP_TO_WISP
                        PetManager.STAGE_WISP -> PetManager.EXP_TO_GUARDIAN
                        else -> PetManager.EXP_TO_GUARDIAN * 2 // Cap
                    }
                    val progress = (petExp.toFloat() / maxExp.toFloat()).coerceIn(0f, 1f)
                    
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = Color(0xFF6366F1), // Indigo
                        trackColor = Color(0xFF1F1F2E)
                    )
                    Text(
                        text = "$petExp / $maxExp XP",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Interaction Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { 
                        onFeedClick() 
                        // Simulate feed effect locally too for instant feedback
                        petManager.feedPet()
                        petMood = petManager.mood
                        showHeartAnimation = true
                        // Reset heart after delay (in real app use LaunchedEffect delay)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("🍎 Feed (Watch Ad)")
                }
                
                 Button(
                    onClick = { /* Navigate to Meditation */ onBackClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("🧘 Meditate w/ Pet")
                }
            }
        }
    }
}
