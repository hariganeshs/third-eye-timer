package com.thirdeyetimer.app.domain

import android.content.Context
import android.content.SharedPreferences

/**
 * UpgradeManager
 * 
 * Manages the upgrade system for the idle game mechanics.
 * Players spend Karma to purchase permanent upgrades that
 * increase their Prana accumulation rate.
 * 
 * All upgrades scale with levels, creating the satisfying
 * progression loop typical of idle games.
 */
class UpgradeManager(context: Context, private val idleGameManager: IdleGameManager) {
    
    private val PREFS_NAME = "UpgradePrefs"
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Upgrade definition with scaling costs and effects
     */
    data class Upgrade(
        val id: String,
        val name: String,
        val description: String,
        val baseCost: Int,
        val costMultiplier: Double,  // Cost increases by this factor per level
        val effectPerLevel: Double,  // Multiplier bonus per level
        val maxLevel: Int,
        val iconName: String  // For loading appropriate icon
    )
    
    companion object {
        // All available upgrades
        val UPGRADES = listOf(
            Upgrade(
                id = "inner_focus",
                name = "Inner Focus",
                description = "Channel your awareness. +10% Prana per level.",
                baseCost = 100,
                costMultiplier = 1.5,
                effectPerLevel = 0.10,
                maxLevel = 10,
                iconName = "focus"
            ),
            Upgrade(
                id = "chakra_alignment",
                name = "Chakra Alignment",
                description = "Align your energy centers. +25% Prana per level.",
                baseCost = 500,
                costMultiplier = 2.0,
                effectPerLevel = 0.25,
                maxLevel = 5,
                iconName = "chakra"
            ),
            Upgrade(
                id = "third_eye_opening",
                name = "Third Eye Opening",
                description = "Awaken your inner vision. 2x Prana permanently.",
                baseCost = 2000,
                costMultiplier = 1.0,  // Only one level
                effectPerLevel = 1.0,  // Doubles the rate
                maxLevel = 1,
                iconName = "third_eye"
            ),
            Upgrade(
                id = "cosmic_consciousness",
                name = "Cosmic Consciousness",
                description = "Connect to the universe. +50% all sources per level.",
                baseCost = 10000,
                costMultiplier = 3.0,
                effectPerLevel = 0.50,
                maxLevel = 3,
                iconName = "cosmic"
            ),
            Upgrade(
                id = "breath_mastery",
                name = "Breath Mastery",
                description = "Perfect your breathing. +15% Prana per level.",
                baseCost = 250,
                costMultiplier = 1.8,
                effectPerLevel = 0.15,
                maxLevel = 8,
                iconName = "breath"
            ),
            Upgrade(
                id = "lotus_position",
                name = "Lotus Position",
                description = "Perfect form brings clarity. +20% Prana per level.",
                baseCost = 750,
                costMultiplier = 2.2,
                effectPerLevel = 0.20,
                maxLevel = 5,
                iconName = "lotus"
            )
        )
        
        private const val KEY_PREFIX = "upgrade_level_"
    }
    
    /**
     * Get current level of an upgrade
     */
    fun getUpgradeLevel(upgradeId: String): Int {
        return prefs.getInt(KEY_PREFIX + upgradeId, 0)
    }
    
    /**
     * Set upgrade level (used internally)
     */
    private fun setUpgradeLevel(upgradeId: String, level: Int) {
        prefs.edit().putInt(KEY_PREFIX + upgradeId, level).apply()
    }
    
    /**
     * Calculate cost for next level of an upgrade
     */
    fun getUpgradeCost(upgrade: Upgrade): Int {
        val currentLevel = getUpgradeLevel(upgrade.id)
        if (currentLevel >= upgrade.maxLevel) return Int.MAX_VALUE
        
        // Exponential cost scaling
        val cost = upgrade.baseCost * Math.pow(upgrade.costMultiplier, currentLevel.toDouble())
        return cost.toInt()
    }
    
    /**
     * Check if user can afford an upgrade
     */
    fun canAfford(upgrade: Upgrade, karma: Int): Boolean {
        val cost = getUpgradeCost(upgrade)
        return karma >= cost && getUpgradeLevel(upgrade.id) < upgrade.maxLevel
    }
    
    /**
     * Purchase an upgrade, returns the cost or 0 if failed
     */
    fun purchaseUpgrade(upgrade: Upgrade, currentKarma: Int): Int {
        val cost = getUpgradeCost(upgrade)
        val currentLevel = getUpgradeLevel(upgrade.id)
        
        if (currentKarma < cost || currentLevel >= upgrade.maxLevel) {
            return 0
        }
        
        setUpgradeLevel(upgrade.id, currentLevel + 1)
        recalculateTotalMultiplier()
        return cost
    }
    
    /**
     * Calculate total multiplier from all upgrades
     */
    fun calculateTotalMultiplier(): Double {
        var multiplier = 1.0
        
        for (upgrade in UPGRADES) {
            val level = getUpgradeLevel(upgrade.id)
            if (level > 0) {
                // Each level adds to the multiplier
                multiplier += (upgrade.effectPerLevel * level)
            }
        }
        
        return multiplier
    }
    
    /**
     * Update the IdleGameManager with current multiplier
     */
    private fun recalculateTotalMultiplier() {
        idleGameManager.upgradeMultiplier = calculateTotalMultiplier()
    }
    
    /**
     * Get all upgrades with their current status
     */
    fun getUpgradeStatuses(): List<UpgradeStatus> {
        return UPGRADES.map { upgrade ->
            UpgradeStatus(
                upgrade = upgrade,
                currentLevel = getUpgradeLevel(upgrade.id),
                cost = getUpgradeCost(upgrade),
                isMaxed = getUpgradeLevel(upgrade.id) >= upgrade.maxLevel
            )
        }
    }
    
    /**
     * Initialize multiplier on app start
     */
    fun initialize() {
        recalculateTotalMultiplier()
    }
    
    /**
     * Status wrapper for UI display
     */
    data class UpgradeStatus(
        val upgrade: Upgrade,
        val currentLevel: Int,
        val cost: Int,
        val isMaxed: Boolean
    )
}
