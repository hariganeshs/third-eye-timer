package com.thirdeyetimer.app.domain

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

/**
 * AuraSystem
 * 
 * Generates aura readings with colors, types, energy levels, and viral captions.
 * This is the core logic for the "Vibe Check" feature.
 */
object AuraSystem {
    
    /**
     * Represents an aura reading result
     */
    data class AuraReading(
        val auraType: AuraType,
        val energyPercent: Int,
        val caption: String,
        val subCaption: String
    )
    
    /**
     * The 7 distinct aura types with their colors and meanings
     */
    enum class AuraType(
        val displayName: String,
        val primaryColor: Color,
        val secondaryColor: Color,
        val glowColor: Color,
        val meaning: String
    ) {
        COSMIC_VIOLET(
            displayName = "Cosmic Consciousness",
            primaryColor = Color(0xFF9B59B6),
            secondaryColor = Color(0xFF8E44AD),
            glowColor = Color(0xFFBB8FCE),
            meaning = "Spiritually Elevated"
        ),
        DEEP_INDIGO(
            displayName = "Deep Wisdom",
            primaryColor = Color(0xFF5B6BF5),
            secondaryColor = Color(0xFF4834D4),
            glowColor = Color(0xFF7B8DF7),
            meaning = "Intuitive & Calm"
        ),
        HEALING_TEAL(
            displayName = "Healing Flow",
            primaryColor = Color(0xFF1ABC9C),
            secondaryColor = Color(0xFF16A085),
            glowColor = Color(0xFF48C9B0),
            meaning = "Balanced & Nurturing"
        ),
        GROWTH_GREEN(
            displayName = "Growth Energy",
            primaryColor = Color(0xFF27AE60),
            secondaryColor = Color(0xFF2ECC71),
            glowColor = Color(0xFF58D68D),
            meaning = "Transformative"
        ),
        SOLAR_GOLD(
            displayName = "Solar Power",
            primaryColor = Color(0xFFF39C12),
            secondaryColor = Color(0xFFE67E22),
            glowColor = Color(0xFFF7DC6F),
            meaning = "Confident & Radiant"
        ),
        CREATIVE_FIRE(
            displayName = "Creative Fire",
            primaryColor = Color(0xFFE74C3C),
            secondaryColor = Color(0xFFFF6B6B),
            glowColor = Color(0xFFF1948A),
            meaning = "Passionate & Dynamic"
        ),
        HEART_GLOW(
            displayName = "Heart Glow",
            primaryColor = Color(0xFFE91E8C),
            secondaryColor = Color(0xFFF06292),
            glowColor = Color(0xFFF48FB1),
            meaning = "Loving & Compassionate"
        )
    }
    
    /**
     * Viral captions for sharing - designed to be Instagram-worthy
     */
    private val viralCaptions = listOf(
        // Status signals
        "Current Vibe: Chaotic Good ✨",
        "Energy: Main Character Mode 🎬",
        "Aura Status: Untouchable 💫",
        "Spiritual Flex: Legendary 🏆",
        "Vibe Level: Immaculate ✨",
        "Energy Reading: Off the Charts 📈",
        "Aura Type: Built Different 💎",
        "Current Status: Ascending 🚀",
        
        // Battery/charging metaphors
        "Energy: 100% Charged 🔋",
        "Soul Battery: Fully Loaded ⚡",
        "Spiritual Charge: Maximum 🌟",
        "Inner Power: Overflowing 💫",
        
        // Mystical vibes
        "Third Eye: Wide Open 👁️",
        "Chakras: Aligned & Thriving 🌈",
        "Frequency: Higher Realm 🌌",
        "Vibration: Cosmic Level 🌠",
        "Aura: Radiating Excellence ✨",
        
        // Confidence boosters
        "Not Everyone Can Handle This Energy 💅",
        "This Aura Hits Different 🔥",
        "Some Call It Magic, I Call It Me ✨",
        "Born To Glow 🌟",
        "Blessed & Glowing 💫",
        
        // Playful ones
        "Warning: Highly Vibrational Being ⚠️",
        "Do Not Disturb: Ascending 🧘",
        "Caught My Good Side... and My Aura 📸",
        "POV: You Checked Your Vibe ✨",
        "The Aura Never Lies 💎",
        
        // Meditation-themed
        "Post-Meditation Glow Up 🧘‍♀️",
        "Inner Peace: Activated ☮️",
        "Centered & Glowing 🌸",
        "Mindfulness Level: Expert 🎯"
    )
    
    /**
     * Sub-captions that describe the energy level
     */
    private val subCaptions = listOf(
        "Your energy is magnetic today",
        "The universe is vibing with you",
        "Peak spiritual performance detected",
        "Your aura is radiant right now",
        "High frequency energy flowing",
        "Inner light: exceptionally bright",
        "Vibrational alignment: perfect",
        "Cosmic connection: established"
    )
    
    /**
     * Generate a unique aura reading
     * Uses randomness + time-based factors for variety
     */
    fun generateAuraReading(): AuraReading {
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE)
        
        // Add some time-based influence for variety (same person gets different results at different times)
        val timeFactor = ((currentHour * 60 + currentMinute) % 100) / 100f
        
        // Energy is always 70-100% (we want people to feel good!)
        val energyPercent = 70 + Random.nextInt(31)
        
        // Select aura type with some time influence
        val auraTypes = AuraType.values()
        val baseIndex = Random.nextInt(auraTypes.size)
        val timeInfluence = (timeFactor * 2).toInt() % auraTypes.size
        val finalIndex = (baseIndex + timeInfluence) % auraTypes.size
        val auraType = auraTypes[finalIndex]
        
        // Select random caption
        val caption = viralCaptions[Random.nextInt(viralCaptions.size)]
        val subCaption = subCaptions[Random.nextInt(subCaptions.size)]
        
        return AuraReading(
            auraType = auraType,
            energyPercent = energyPercent,
            caption = caption,
            subCaption = subCaption
        )
    }
    
    /**
     * Get the gradient colors for an aura type
     */
    fun getAuraGradient(auraType: AuraType): List<Color> {
        return listOf(
            auraType.primaryColor.copy(alpha = 0.8f),
            auraType.secondaryColor.copy(alpha = 0.6f),
            auraType.glowColor.copy(alpha = 0.3f),
            Color.Transparent
        )
    }
    
    /**
     * Get outer glow colors for layered effect
     */
    fun getOuterGlowGradient(auraType: AuraType): List<Color> {
        return listOf(
            auraType.glowColor.copy(alpha = 0.4f),
            auraType.primaryColor.copy(alpha = 0.2f),
            Color.Transparent
        )
    }
}
