package com.thirdeyetimer.app.domain

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

/**
 * PetManager
 * 
 * Manages the logic for the "Cosmic Pet" (Soul Wisp).
 * This is a key viral retention mechanic.
 */
class PetManager(context: Context) {
    
    private val PREFS_NAME = "CosmicPetPrefs"
    private val KEY_PET_EXP = "pet_exp"
    private val KEY_PET_STAGE = "pet_stage"
    private val KEY_LAST_FED_TIME = "last_fed_time"
    private val KEY_MOOD_LEVEL = "mood_level" // 0-100
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Game Constants
    companion object {
        const val STAGE_EGG = 0
        const val STAGE_WISP = 1
        const val STAGE_GUARDIAN = 2
        
        const val EXP_TO_WISP = 60 // Minutes of meditation
        const val EXP_TO_GUARDIAN = 300 // Minutes of meditation
        
        const val MOOD_DECAY_PER_HOUR = 5
        const val MOOD_MAX = 100
        const val FEED_BOOST = 30
    }
    
    var exp: Int
        get() = prefs.getInt(KEY_PET_EXP, 0)
        private set(value) = prefs.edit().putInt(KEY_PET_EXP, value).apply()
        
    var stage: Int
        get() = prefs.getInt(KEY_PET_STAGE, STAGE_EGG)
        private set(value) = prefs.edit().putInt(KEY_PET_STAGE, value).apply()
        
    var lastFedTime: Long
        get() = prefs.getLong(KEY_LAST_FED_TIME, 0L)
        private set(value) = prefs.edit().putLong(KEY_LAST_FED_TIME, value).apply()

    // Calculated mood based on time passed
    var mood: Int
        get() {
            val savedMood = prefs.getInt(KEY_MOOD_LEVEL, 50)
            val lastUpdate = lastFedTime
            if (lastUpdate == 0L) return 50
            
            val hoursPassed = (System.currentTimeMillis() - lastUpdate) / (1000 * 60 * 60)
            val decay = (hoursPassed * MOOD_DECAY_PER_HOUR).toInt()
            
            return (savedMood - decay).coerceIn(0, MOOD_MAX)
        }
        private set(value) = prefs.edit().putInt(KEY_MOOD_LEVEL, value).putLong(KEY_LAST_FED_TIME, System.currentTimeMillis()).apply()

    fun addExp(minutes: Int) {
        val currentExp = exp
        val newExp = currentExp + minutes
        exp = newExp
        
        checkEvolution()
        
        // Meditating also improves mood!
        feedPet(10) 
    }
    
    fun feedPet(amount: Int = FEED_BOOST) {
        val currentMood = mood // captures decay since last time
        mood = (currentMood + amount).coerceAtMost(MOOD_MAX)
    }
    
    private fun checkEvolution() {
        val currentStage = stage
        if (currentStage == STAGE_EGG && exp >= EXP_TO_WISP) {
            stage = STAGE_WISP
        } else if (currentStage == STAGE_WISP && exp >= EXP_TO_GUARDIAN) {
            stage = STAGE_GUARDIAN
        }
    }
    
    fun getPetName(): String {
        return when(stage) {
            STAGE_EGG -> "Cosmic Egg"
            STAGE_WISP -> "Soul Wisp"
            STAGE_GUARDIAN -> "Astral Guardian"
            else -> "Unknown Entity"
        }
    }
    
    fun getPetDescription(): String {
        return when(stage) {
            STAGE_EGG -> "A dormant potential waiting for your mindfulness to hatch."
            STAGE_WISP -> "A flickering light fueled by your inner peace."
            STAGE_GUARDIAN -> "A powerful protector manifestation of your consistency."
            else -> "???"
        }
    }
    
    // Image representation for Pet
    fun getPetImageRes(): Int {
         return when(stage) {
            STAGE_EGG -> com.thirdeyetimer.app.R.drawable.pet_egg
            STAGE_WISP -> com.thirdeyetimer.app.R.drawable.pet_wisp 
            STAGE_GUARDIAN -> com.thirdeyetimer.app.R.drawable.pet_guardian
            else -> com.thirdeyetimer.app.R.drawable.icon_lock_locked
        }
    }
}
