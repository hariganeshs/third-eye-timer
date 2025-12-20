package com.thirdeyetimer.app.domain

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

/**
 * IdleGameManager
 * 
 * Core idle game logic managing Spiritual Ego (spiritual energy/XP) accumulation.
 * Uses exponential growth formulas typical of idle games to create
 * an addictive progression system built on false progress.
 * 
 * Formula: spiritualEgoPerSecond = baseRate * streakMultiplier * upgradeMultiplier * sessionBonus
 * 
 * The longer you meditate in a session, the faster Spiritual Ego accumulates,
 * creating the satisfying "idle game" feel.
 * 
 * High-speed handling: Uses precise time tracking to ensure no Spiritual Ego is lost
 * even when the rate is millions per second.
 */
class IdleGameManager(context: Context) {
    
    private val PREFS_NAME = "IdleGamePrefs"
    private val KEY_TOTAL_SPIRITUAL_EGO = "total_spiritual_ego"
    private val KEY_LIFETIME_SPIRITUAL_EGO = "lifetime_spiritual_ego"
    private val KEY_SESSION_SPIRITUAL_EGO = "session_spiritual_ego"
    private val KEY_UPGRADE_MULTIPLIER = "upgrade_multiplier"
    private val KEY_AD_BOOST_ACTIVE = "ad_boost_active"
    private val KEY_AD_BOOST_EXPIRY = "ad_boost_expiry"
    private val KEY_LAST_SESSION_END_TIME = "last_session_end_time"
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        // Base rate: 1 Spiritual Ego per second at the start
        const val BASE_RATE = 1.0
        
        // Session bonus increases by 2% per minute of meditation (faster growth)
        const val SESSION_BONUS_PER_MINUTE = 0.02
        
        // Maximum session bonus to prevent overflow
        const val MAX_SESSION_BONUS = 10.0
        
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

        // Wait Wall: How long to wait between sessions
        const val WAIT_WALL_DURATION_MS = 10 * 60 * 1000L // 10 minutes (satirical bottleneck)
    }
    
    // Total Spiritual Ego (can be spent on upgrades)
    var totalSpiritualEgo: Long
        get() = prefs.getLong(KEY_TOTAL_SPIRITUAL_EGO, 0L)
        private set(value) = prefs.edit().putLong(KEY_TOTAL_SPIRITUAL_EGO, value).apply()
    
    // Lifetime Spiritual Ego earned (never decreases, for achievements)
    var lifetimeSpiritualEgo: Long
        get() = prefs.getLong(KEY_LIFETIME_SPIRITUAL_EGO, 0L)
        private set(value) = prefs.edit().putLong(KEY_LIFETIME_SPIRITUAL_EGO, value).apply()
    
    // Current session's Spiritual Ego (in memory, reset each session)
    private var _sessionSpiritualEgo: Long = 0L
    val sessionSpiritualEgo: Long get() = _sessionSpiritualEgo
    
    // Accumulated fractional spiritual ego (for high precision at low rates)
    private var fractionalSpiritualEgo: Double = 0.0
    
    // Base upgrade multiplier (from purchased upgrades)
    var upgradeMultiplier: Double
        get() = prefs.getFloat(KEY_UPGRADE_MULTIPLIER, 1.0f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_UPGRADE_MULTIPLIER, value.toFloat()).apply()
    
    // Ad boost state
    val isAdBoostActive: Boolean
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
     * Capped at MAX_SESSION_BONUS to prevent overflow
     */
    fun getSessionBonus(elapsedMinutes: Double): Double {
        val bonus = 1.0 + (SESSION_BONUS_PER_MINUTE * elapsedMinutes)
        return bonus.coerceAtMost(MAX_SESSION_BONUS)
    }
    
    /**
     * Calculate Spiritual Ego per second at the current moment
     */
    fun calculateSpiritualEgoPerSecond(
        streakDays: Int,
        elapsedMinutes: Double
    ): Double {
        val streakMult = getStreakMultiplier(streakDays)
        val sessionBonus = getSessionBonus(elapsedMinutes)
        val adBoost = if (isAdBoostActive) AD_BOOST_MULTIPLIER else 1.0
        
        return BASE_RATE * streakMult * upgradeMultiplier * sessionBonus * adBoost
    }
    
    /**
     * Calculate and add Spiritual Ego for a time delta
     * Uses fractional accumulation for precision at any speed
     */
    fun accumulateSpiritualEgo(
        streakDays: Int,
        elapsedMinutes: Double,
        deltaSeconds: Double
    ): Long {
        val rate = calculateSpiritualEgoPerSecond(streakDays, elapsedMinutes)
        val spiritualEgoEarned = rate * deltaSeconds + fractionalSpiritualEgo
        
        // Split into whole and fractional parts
        val wholeSpiritualEgo = spiritualEgoEarned.toLong()
        fractionalSpiritualEgo = spiritualEgoEarned - wholeSpiritualEgo
        
        if (wholeSpiritualEgo > 0) {
            _sessionSpiritualEgo += wholeSpiritualEgo
        }
        
        return wholeSpiritualEgo
    }
    
    /**
     * Called periodically during meditation to add Spiritual Ego
     */
    fun addSessionSpiritualEgo(amount: Long) {
        _sessionSpiritualEgo += amount
    }
    
    /**
     * Called when meditation session completes
     * Commits session Spiritual Ego to totals
     */
    fun commitSession(): Long {
        val earned = _sessionSpiritualEgo
        totalSpiritualEgo += earned
        lifetimeSpiritualEgo += earned
        _sessionSpiritualEgo = 0L
        fractionalSpiritualEgo = 0.0
        
        // Persist immediately and set wait wall
        prefs.edit()
            .putLong(KEY_TOTAL_SPIRITUAL_EGO, totalSpiritualEgo)
            .putLong(KEY_LIFETIME_SPIRITUAL_EGO, lifetimeSpiritualEgo)
            .putLong(KEY_LAST_SESSION_END_TIME, System.currentTimeMillis())
            .apply()
            
        return earned
    }
    
    /**
     * Double the session Spiritual Ego (rewarded ad)
     */
    fun doubleSessionSpiritualEgo() {
        _sessionSpiritualEgo *= 2
    }
    
    /**
     * Reset session Spiritual Ego (when stopping early without completing)
     */
    fun resetSession() {
        _sessionSpiritualEgo = 0L
        fractionalSpiritualEgo = 0.0
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
     * Format Spiritual Ego for display with K/M/B/T notation
     */
    fun formatSpiritualEgo(spiritualEgo: Long): String {
        return when {
            spiritualEgo >= 1_000_000_000_000L -> String.format(Locale.US, "%.2fT", spiritualEgo / 1_000_000_000_000.0)
            spiritualEgo >= 1_000_000_000L -> String.format(Locale.US, "%.2fB", spiritualEgo / 1_000_000_000.0)
            spiritualEgo >= 1_000_000L -> String.format(Locale.US, "%.2fM", spiritualEgo / 1_000_000.0)
            spiritualEgo >= 1_000L -> String.format(Locale.US, "%.1fK", spiritualEgo / 1_000.0)
            else -> spiritualEgo.toString()
        }
    }
    
    /**
     * Wait Wall: Check if a meditation session is currently blocked
     */
    fun isWaitWallActive(): Boolean {
        val lastEnd = prefs.getLong(KEY_LAST_SESSION_END_TIME, 0L)
        val elapsed = System.currentTimeMillis() - lastEnd
        return elapsed < WAIT_WALL_DURATION_MS
    }

    /**
     * Get remaining wait wall time in milliseconds
     */
    fun getWaitWallRemainingMs(): Long {
        val lastEnd = prefs.getLong(KEY_LAST_SESSION_END_TIME, 0L)
        val elapsed = System.currentTimeMillis() - lastEnd
        val remaining = WAIT_WALL_DURATION_MS - elapsed
        return if (remaining > 0) remaining else 0L
    }

    /**
     * Bypass wait wall (e.g. after watching a "Shortcut" ad)
     */
    fun bypassWaitWall() {
        prefs.edit().putLong(KEY_LAST_SESSION_END_TIME, 0L).apply()
    }

    /**
     * Format Spiritual Ego per second rate
     */
    fun formatRate(rate: Double): String {
        return when {
            rate >= 1_000_000_000L -> String.format(Locale.US, "%.2fB/s", rate / 1_000_000_000.0)
            rate >= 1_000_000L -> String.format(Locale.US, "%.2fM/s", rate / 1_000_000.0)
            rate >= 1_000L -> String.format(Locale.US, "%.1fK/s", rate / 1_000.0)
            rate >= 10 -> String.format(Locale.US, "%.0f/s", rate)
            else -> String.format(Locale.US, "%.1f/s", rate)
        }
    }
}

