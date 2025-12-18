package com.thirdeyetimer.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/**
 * Cosmic Zen Theme
 * The main theme for Third Eye Timer meditation app
 */

private val CosmicDarkColorScheme = darkColorScheme(
    // Primary colors
    primary = CosmicColors.Primary,
    onPrimary = CosmicColors.TextPrimary,
    primaryContainer = CosmicColors.PrimaryContainer,
    onPrimaryContainer = CosmicColors.TextSecondary,
    
    // Secondary colors
    secondary = CosmicColors.Secondary,
    onSecondary = CosmicColors.TextPrimary,
    secondaryContainer = CosmicColors.SecondaryContainer,
    onSecondaryContainer = CosmicColors.TextSecondary,
    
    // Tertiary colors (accent)
    tertiary = CosmicColors.Accent,
    onTertiary = CosmicColors.BackgroundStart,
    tertiaryContainer = CosmicColors.AccentDark,
    onTertiaryContainer = CosmicColors.TextPrimary,
    
    // Background colors
    background = CosmicColors.BackgroundStart,
    onBackground = CosmicColors.TextPrimary,
    
    // Surface colors
    surface = CosmicColors.Surface,
    onSurface = CosmicColors.TextPrimary,
    surfaceVariant = CosmicColors.SurfaceVariant,
    onSurfaceVariant = CosmicColors.TextSecondary,
    
    // Error colors
    error = CosmicColors.Error,
    onError = CosmicColors.TextPrimary,
    errorContainer = CosmicColors.Error.copy(alpha = 0.3f),
    onErrorContainer = CosmicColors.TextPrimary,
    
    // Other colors
    outline = CosmicColors.GlassBorder,
    outlineVariant = CosmicColors.GlassHighlight,
    inverseSurface = CosmicColors.TextPrimary,
    inverseOnSurface = CosmicColors.BackgroundStart,
    inversePrimary = CosmicColors.PrimaryDark,
    scrim = CosmicColors.BackgroundStart.copy(alpha = 0.8f)
)

/**
 * Cosmic Zen Theme Composable
 * 
 * This theme creates a stunning cosmic meditation experience with
 * deep space backgrounds, glowing accents, and glassmorphic elements.
 */
@Composable
fun CosmicZenTheme(
    darkTheme: Boolean = true, // Always dark for the cosmic theme
    content: @Composable () -> Unit
) {
    val colorScheme = CosmicDarkColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Make status bar blend with the cosmic background
            window.statusBarColor = CosmicColors.BackgroundStart.toArgb()
            window.navigationBarColor = CosmicColors.BackgroundStart.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CosmicTypography,
        content = content
    )
}

/**
 * Shape definitions for the Cosmic Zen theme
 */
object CosmicShapes {
    val small = RoundedCornerShape(8.dp)
    val medium = RoundedCornerShape(16.dp)
    val large = RoundedCornerShape(24.dp)
    val extraLarge = RoundedCornerShape(32.dp)
    val pill = RoundedCornerShape(50)
}

