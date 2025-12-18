package com.thirdeyetimer.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Cosmic Zen Color Palette
 * A premium meditation app color system inspired by the cosmos
 */
object CosmicColors {
    
    // ═══════════════════════════════════════════════════════════════════════
    // PRIMARY COLORS - Cosmic Indigo
    // ═══════════════════════════════════════════════════════════════════════
    val Primary = Color(0xFF6366F1)
    val PrimaryDark = Color(0xFF4F46E5)
    val PrimaryLight = Color(0xFF818CF8)
    val PrimaryContainer = Color(0xFF312E81)
    
    // ═══════════════════════════════════════════════════════════════════════
    // SECONDARY COLORS - Aurora Teal
    // ═══════════════════════════════════════════════════════════════════════
    val Secondary = Color(0xFF14B8A6)
    val SecondaryDark = Color(0xFF0D9488)
    val SecondaryLight = Color(0xFF2DD4BF)
    val SecondaryContainer = Color(0xFF115E59)
    
    // ═══════════════════════════════════════════════════════════════════════
    // ACCENT COLORS - Celestial Gold
    // ═══════════════════════════════════════════════════════════════════════
    val Accent = Color(0xFFF59E0B)
    val AccentDark = Color(0xFFD97706)
    val AccentLight = Color(0xFFFBBF24)
    
    // ═══════════════════════════════════════════════════════════════════════
    // BACKGROUND COLORS - Deep Space
    // ═══════════════════════════════════════════════════════════════════════
    val BackgroundStart = Color(0xFF0F0F23)
    val BackgroundEnd = Color(0xFF1E1E3F)
    val Surface = Color(0xFF252547)
    val SurfaceVariant = Color(0xFF2D2D5A)
    
    // ═══════════════════════════════════════════════════════════════════════
    // GLASS COLORS - Glassmorphism
    // ═══════════════════════════════════════════════════════════════════════
    val GlassBackground = Color(0x14FFFFFF)
    val GlassBorder = Color(0x33FFFFFF)
    val GlassHighlight = Color(0x1AFFFFFF)
    
    // ═══════════════════════════════════════════════════════════════════════
    // TEXT COLORS
    // ═══════════════════════════════════════════════════════════════════════
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFE0E7FF)
    val TextTertiary = Color(0xFFA5B4FC)
    val TextMuted = Color(0xFF6B7280)
    
    // ═══════════════════════════════════════════════════════════════════════
    // STATUS COLORS
    // ═══════════════════════════════════════════════════════════════════════
    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
    val Info = Color(0xFF3B82F6)
    
    // ═══════════════════════════════════════════════════════════════════════
    // GLOW COLORS
    // ═══════════════════════════════════════════════════════════════════════
    val GlowIndigo = Color(0x806366F1)
    val GlowTeal = Color(0x8014B8A6)
    val GlowGold = Color(0x80F59E0B)
    
    // ═══════════════════════════════════════════════════════════════════════
    // BREATHING CIRCLE COLORS
    // ═══════════════════════════════════════════════════════════════════════
    val BreathingRingInner = Color(0xFF6366F1)
    val BreathingRingOuter = Color(0xFF14B8A6)
    val BreathingGlow = Color(0x406366F1)
    
    // ═══════════════════════════════════════════════════════════════════════
    // ACHIEVEMENT COLORS
    // ═══════════════════════════════════════════════════════════════════════
    val AchievementGold = Color(0xFFFFD700)
    val AchievementSilver = Color(0xFFC0C0C0)
    val AchievementBronze = Color(0xFFCD7F32)
    val AchievementLocked = Color(0xFF4B5563)
    
    // ═══════════════════════════════════════════════════════════════════════
    // STREAK FIRE COLORS
    // ═══════════════════════════════════════════════════════════════════════
    val StreakFireStart = Color(0xFFF59E0B)
    val StreakFireMid = Color(0xFFEF4444)
    val StreakFireEnd = Color(0xFFDC2626)
    
    // ═══════════════════════════════════════════════════════════════════════
    // CONFETTI COLORS
    // ═══════════════════════════════════════════════════════════════════════
    val ConfettiColors = listOf(
        Color(0xFF6366F1),
        Color(0xFF14B8A6),
        Color(0xFFF59E0B),
        Color(0xFFEC4899),
        Color(0xFF8B5CF6)
    )
    
    // ═══════════════════════════════════════════════════════════════════════
    // GRADIENT COMBINATIONS
    // ═══════════════════════════════════════════════════════════════════════
    val BackgroundGradient = listOf(BackgroundStart, BackgroundEnd)
    val PrimaryGradient = listOf(Primary, Secondary)
    val AccentGradient = listOf(AccentLight, Accent, AccentDark)
    val StreakGradient = listOf(StreakFireStart, StreakFireMid, StreakFireEnd)
}
