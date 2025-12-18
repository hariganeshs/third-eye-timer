package com.thirdeyetimer.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.thirdeyetimer.app.R

/**
 * Cosmic Zen Typography System
 * Premium typography for a meditation app
 */

// Custom font family for the app
// Using system fonts that work well for meditation apps
val CosmicFontFamily = FontFamily.Default

// Timer-specific font (monospace for consistent width)
val TimerFontFamily = FontFamily.Monospace

/**
 * Cosmic Zen Typography
 * Clean, readable typography with elegant spacing
 */
val CosmicTypography = Typography(
    // Display styles - for large hero text
    displayLarge = TextStyle(
        fontFamily = CosmicFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = CosmicFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = CosmicFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    
    // Headline styles - for section headers
    headlineLarge = TextStyle(
        fontFamily = CosmicFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = CosmicFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = CosmicFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    
    // Title styles - for cards and list items
    titleLarge = TextStyle(
        fontFamily = CosmicFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = CosmicFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = CosmicFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    
    // Body styles - for content text
    bodyLarge = TextStyle(
        fontFamily = CosmicFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = CosmicFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = CosmicFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    
    // Label styles - for buttons and chips
    labelLarge = TextStyle(
        fontFamily = CosmicFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = CosmicFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = CosmicFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * Timer Typography - Special styles for the meditation timer
 */
object TimerTypography {
    val timerLarge = TextStyle(
        fontFamily = TimerFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 72.sp,
        lineHeight = 80.sp,
        letterSpacing = 2.sp
    )
    
    val timerMedium = TextStyle(
        fontFamily = TimerFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 56.sp,
        lineHeight = 64.sp,
        letterSpacing = 1.sp
    )
    
    val timerSmall = TextStyle(
        fontFamily = TimerFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 1.sp
    )
}
