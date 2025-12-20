package com.thirdeyetimer.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Terminal/Brutalist colors for the "Truth Punch" and "Diegetic Ad" systems.
 * High-contrast, hardware-inspired palette.
 */
object TerminalColors {
    val TerminalBlack = Color(0xFF0A0A0A)
    val TerminalDarkGray = Color(0xFF1A1A1A)
    val TerminalRed = Color(0xFFFF3B3B)
    val TerminalAmber = Color(0xFFFFB000)
    val TerminalWhite = Color(0xFFE0E0E0)
    val TerminalGray = Color(0xFF666666)
    val TerminalGreen = Color(0xFF00FF41) // Classic matrix green
}

// Also export top-level for easier imports in AdFrameComponents, etc.
val TerminalBlack = TerminalColors.TerminalBlack
val TerminalDarkGray = TerminalColors.TerminalDarkGray
val TerminalRed = TerminalColors.TerminalRed
val TerminalAmber = TerminalColors.TerminalAmber
val TerminalWhite = TerminalColors.TerminalWhite
val TerminalGray = TerminalColors.TerminalGray
val TerminalGreen = TerminalColors.TerminalGreen
