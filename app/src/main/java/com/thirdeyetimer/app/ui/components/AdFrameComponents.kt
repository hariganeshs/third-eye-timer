package com.thirdeyetimer.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.thirdeyetimer.app.ui.theme.*

/**
 * Frames a banner ad as part of the "Societal Programming Feed".
 */
@Composable
fun SocietalProgrammingFrame(
    label: String = "SOCIETAL PROGRAMMING FEED",
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .border(1.dp, TerminalGray.copy(alpha = 0.3f))
    ) {
        // Feed Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalGray.copy(alpha = 0.1f))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = TerminalGray,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "[ TRANSMISSION STABLE ]",
                color = TerminalGreen.copy(alpha = 0.5f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        
        // Ad Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
        
        // Footer (distraction warning)
        Text(
            text = "DO NOT RESIST THE COMFORT OF CONSUMPTION",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            color = TerminalGray.copy(alpha = 0.4f),
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

/**
 * A dialog/frame shown after viewing a rewarded ad ("The Shortcut to Nowhere").
 */
@Composable
fun ShortcutAftermathFrame(
    rewardDescription: String,
    onDismiss: () -> Unit
) {
    BrutalistDialog(
        onDismiss = onDismiss,
        title = "THE SHORTCUT TO NOWHERE",
        titleColor = TerminalRed
    ) {
        Column {
            Text(
                text = "Congratulations. You traded 30 seconds of your finite existence for $rewardDescription.",
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "The system is pleased. Your Spiritual Ego has swelled effectively. Was it worth the fragmentation of your attention?",
                color = TerminalGray,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            TerminalButton(
                text = "CONTINUE THE CHARADE",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * An interstitial transition frame representing "Mind Chatter".
 * Auto-dismisses after 2 seconds or can be tapped to skip.
 */
@Composable
fun MindChatterFrame(
    onComplete: () -> Unit
) {
    // Auto-dismiss after 2 seconds
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        onComplete()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onComplete() }, // Tap to skip
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "INTERRUPTING PRESENCE...",
                color = TerminalRed,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "SOCIETAL CONDITIONING: LOADING",
                color = TerminalGray,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Simulating a loading bar or interference
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(2.dp)
                    .background(TerminalGray.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight()
                        .background(TerminalRed)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "[ TAP TO SKIP ]",
                color = TerminalGray.copy(alpha = 0.5f),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Brutalist-style dialog base
 */
@Composable
fun BrutalistDialog(
    onDismiss: () -> Unit,
    title: String,
    titleColor: Color = TerminalAmber,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(TerminalBlack)
                    .border(2.dp, titleColor)
                    .padding(24.dp)
                    .clickable(enabled = false) { }
            ) {
                Text(
                    text = title,
                    color = titleColor,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                content()
            }
        }
    }
}

/**
 * Harsh-styled button for brutalist UI with pulsing glow animation
 */
@Composable
fun TerminalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = TerminalGreen
) {
    // Pulsing animation for attention
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = glowScale; scaleY = glowScale }
            .background(color.copy(alpha = pulseAlpha * 0.15f))
            .background(Color.Black)
            .border(2.dp, color.copy(alpha = pulseAlpha + 0.2f))
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "> $text",
            color = color,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

