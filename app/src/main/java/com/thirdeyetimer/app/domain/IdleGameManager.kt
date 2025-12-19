package com.thirdeyetimer.app.domain

import android.content.Context
import android.content.SharedPreferences

/**
 * IdleGameManager
 * 
 * Core idle game logic managing Prana (spiritual energy/XP) accumulation.
 * Uses exponential growth formulas typical of idle games to create
 * an addictive progression system.
 * 
 * Formula: pranaPerSecond = baseRate * streakMultiplier * upgradeMultiplier * sessionBonus
 * 
 * The longer you meditate in a session, the faster Prana accumulates,
 * creating the satisfying "idle game" feel.
 */
class IdleGameManager(context: Context) {
    
    private val PREFS_NAME = "IdleGamePrefs"
    private val KEY_TOTAL_PRANA = "total_prana"
    private val KEY_LIFETIME_PRANA = "lifetime_prana"
    private val KEY_SESSION_PRANA = "session_prana"
    private val KEY_UPGRADE_MULTIPLIER = "upgrade_multiplier"
    private val KEY_AD_BOOST_ACTIVE = "ad_boost_active"
    private val KEY_AD_BOOST_EXPIRY = "ad_boost_expiry"
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        // Base rate: 1 Prana per second at the start
        const val BASE_RATE = 1.0
        
        // Session bonus increases by 1% per minute of meditation
        const val SESSION_BONUS_PER_MINUTE = 0.01
        
        // Streak multiplier thresholds
        val STREAK_MULTIPLIERS = mapOf(
            0 to 1.0,
            1 to 1.0,
            3 to 1.2,
            7 to 1.5,
            14 to 2.0,
            30 to 3.0,
            60 to 4.0
        )
        
        // Ad boost multiplier (2x for watching rewarded ad)
        const val AD_BOOST_MULTIPLIER = 2.0
        const val AD_BOOST_DURATION_MS = 30 * 60 * 1000L // 30 minutes
    }
    
    // Total Prana (can be spent on upgrades in future)
    var totalPrana: Long
        get() = prefs.getLong(KEY_TOTAL_PRANA, 0L)
        private set(value) = prefs.edit().putLong(KEY_TOTAL_PRANA, value).apply()
    
    // Lifetime Prana earned (never decreases, for achievements)
    var lifetimePrana: Long
        get() = prefs.getLong(KEY_LIFETIME_PRANA, 0L)
        private set(value) = prefs.edit().putLong(KEY_LIFETIME_PRANA, value).apply()
    
    // Current session's Prana (reset each session)
    var sessionPrana: Long = 0L
        private set
    
    // Base upgrade multiplier (from purchased upgrades)
    var upgradeMultiplier: Double
        get() = prefs.getFloat(KEY_UPGRADE_MULTIPLIER, 1.0f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_UPGRADE_MULTIPLIER, value.toFloat()).apply()
    
    // Ad boost state
    private val isAdBoostActive: Boolean
        get() {
            val expiry = prefs.getLong(KEY_AD_BOOST_EXPIRY, 0L)
            return System.currentTimeMillis() < expiry
        }
    
    /**
     * Calculate the streak multiplier based on current streak days
     */
    fun getStreakMultiplier(streakDays: Int): Double {
        var multiplier = 1.0
        for ((threshold, mult) in STREAK_MULTIPLIERS) {
            if (streakDays >= threshold) {
                multiplier = mult
            }
        }
        return multiplier
    }
    
    /**
     * Calculate the session bonus based on elapsed minutes
     * The longer you meditate, the higher the bonus
     */
    fun getSessionBonus(elapsedMinutes: Double): Double {
        return 1.0 + (SESSION_BONUS_PER_MINUTE * elapsedMinutes)
    }
    
    /**
     * Calculate Prana per second at the current moment
     */
    fun calculatePranaPerSecond(
        streakDays: Int,
        elapsedMinutes: Double
    ): Double {
        val streakMult = getStreakMultiplier(streakDays)
        val sessionBonus = getSessionBonus(elapsedMinutes)
        val adBoost = if (isAdBoostActive) AD_BOOST_MULTIPLIER else 1.0
        
        return BASE_RATE * streakMult * upgradeMultiplier * sessionBonus * adBoost
    }
    
    /**
     * Calculate Prana earned for a time interval
     * Uses integration to account for increasing session bonus
     */
    fun calculatePranaForInterval(
        streakDays: Int,
        startMinute: Double,
        endMinute: Double
    ): Long {
        // For short intervals, use average rate
        val avgMinute = (startMinute + endMinute) / 2
        val durationSeconds = (endMinute - startMinute) * 60
        val rate = calculatePranaPerSecond(streakDays, avgMinute)
        
        return (rate * durationSeconds).toLong()
    }
    
    /**
     * Called periodically during meditation to add Prana
     */
    fun addSessionPrana(amount: Long) {
        sessionPrana += amount
    }
    
    /**
     * Called when meditation session completes
     * Commits session Prana to totals
     */
    fun commitSession(): Long {
        val earned = sessionPrana
        totalPrana += earned
        lifetimePrana += earned
        sessionPrana = 0L
        return earned
    }
    
    /**
     * Double the session Prana (rewarded ad)
     */
    fun doubleSessionPrana() {
        sessionPrana *= 2
    }
    
    /**
     * Reset session Prana (when stopping early without completing)
     */
    fun resetSession() {
        sessionPrana = 0L
    }
    
    /**
     * Activate ad boost (2x for 30 minutes)
     */
    fun activateAdBoost() {
        val expiry = System.currentTimeMillis() + AD_BOOST_DURATION_MS
        prefs.edit().putLong(KEY_AD_BOOST_EXPIRY, expiry).apply()
    }
    
    /**
     * Get remaining ad boost time in milliseconds
     */
    fun getAdBoostRemainingMs(): Long {
        val expiry = prefs.getLong(KEY_AD_BOOST_EXPIRY, 0L)
        val remaining = expiry - System.currentTimeMillis()
        return if (remaining > 0) remaining else 0L
    }
    
    /**
     * Format Prana for display with K/M/B notation
     */
    fun formatPrana(prana: Long): String {
        return when {
            prana >= 1_000_000_000_000L -> String.format("%.2fT", prana / 1_000_000_000_000.0)
            prana >= 1_000_000_000L -> String.format("%.2fB", prana / 1_000_000_000.0)
            prana >= 1_000_000L -> String.format("%.2fM", prana / 1_000_000.0)
            prana >= 1_000L -> String.format("%.1fK", prana / 1_000.0)
            else -> prana.toString()
        }
    }
    
    /**
     * Format Prana per second rate
     */
    fun formatRate(rate: Double): String {
        return when {
            rate >= 1_000_000_000L -> String.format("%.2fB/s", rate / 1_000_000_000.0)
            rate >= 1_000_000L -> String.format("%.2fM/s", rate / 1_000_000.0)
            rate >= 1_000L -> String.format("%.1fK/s", rate / 1_000.0)
            rate >= 10 -> String.format("%.0f/s", rate)
            else -> String.format("%.1f/s", rate)
        }
    }
}
