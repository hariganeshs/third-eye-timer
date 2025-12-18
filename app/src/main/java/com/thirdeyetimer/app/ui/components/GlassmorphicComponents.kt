package com.thirdeyetimer.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thirdeyetimer.app.ui.theme.CosmicColors

/**
 * GlassmorphicCard
 * 
 * A beautiful frosted glass effect card for the Cosmic Zen theme.
 * Creates a premium glassmorphism effect with subtle transparency and border.
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    backgroundColor: Color = CosmicColors.GlassBackground,
    borderColor: Color = CosmicColors.GlassBorder,
    borderWidth: Dp = 1.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = shape
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * GlassmorphicCardWithGlow
 * 
 * Enhanced glassmorphic card with a subtle glow effect.
 */
@Composable
fun GlassmorphicCardWithGlow(
    modifier: Modifier = Modifier,
    glowColor: Color = CosmicColors.GlowIndigo,
    cornerRadius: Dp = 20.dp,
    glowRadius: Dp = 16.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
    ) {
        // Glow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(4.dp)
                .blur(glowRadius)
                .background(
                    color = glowColor,
                    shape = RoundedCornerShape(cornerRadius)
                )
        )
        
        // Main card
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = cornerRadius,
            contentPadding = contentPadding,
            content = content
        )
    }
}

/**
 * GlassmorphicButton
 * 
 * A glassmorphic styled button for secondary actions.
 */
@Composable
fun GlassmorphicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    
    Surface(
        onClick = onClick,
        modifier = modifier
            .clip(shape)
            .border(
                width = 1.dp,
                color = CosmicColors.GlassBorder,
                shape = shape
            ),
        shape = shape,
        color = CosmicColors.GlassBackground
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * PrimaryGradientButton
 * 
 * A primary action button with gradient background.
 */
@Composable
fun PrimaryGradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cornerRadius: Dp = 28.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 48.dp, vertical = 16.dp),
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val gradientBrush = Brush.horizontalGradient(
        colors = if (enabled) {
            listOf(CosmicColors.Secondary, CosmicColors.SecondaryLight)
        } else {
            listOf(CosmicColors.TextMuted, CosmicColors.TextMuted)
        }
    )
    
    Surface(
        onClick = { if (enabled) onClick() },
        modifier = modifier
            .clip(shape),
        shape = shape,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(gradientBrush)
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

/**
 * AccentGradientButton
 * 
 * An accent button with gold gradient for special actions.
 */
@Composable
fun AccentGradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cornerRadius: Dp = 28.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 48.dp, vertical = 16.dp),
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val gradientBrush = Brush.horizontalGradient(
        colors = if (enabled) {
            listOf(CosmicColors.AccentLight, CosmicColors.Accent)
        } else {
            listOf(CosmicColors.TextMuted, CosmicColors.TextMuted)
        }
    )
    
    Surface(
        onClick = { if (enabled) onClick() },
        modifier = modifier
            .clip(shape),
        shape = shape,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(gradientBrush)
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}
