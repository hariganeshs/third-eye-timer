package com.thirdeyetimer.app.domain

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar
import java.util.Locale

/**
 * QuestManager
 * 
 * Manages Daily Quests and Karma rewards.
 * Quests reset daily. Completing quests awards Karma.
 * Also manages ad reward cooldowns.
 */
class QuestManager(context: Context) {
    
    private val PREFS_NAME = "QuestPrefs"
    private val KEY_KARMA_FROM_QUESTS = "karma_from_quests"
    private val KEY_LAST_QUEST_DATE = "last_quest_date"
    private val KEY_QUESTS_JSON = "quests_json"
    private val KEY_LAST_AD_REWARD_TIME = "last_ad_reward_time"
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        const val QUEST_MEDITATE_10_MIN = "meditate_10"
        const val QUEST_USE_BELL = "use_bell"
        const val QUEST_VISIT_PET = "visit_pet"
        const val QUEST_SHARE_STREAK = "share_streak"
        
        const val REWARD_STANDARD = 50  // Karma per quest
        const val AD_REWARD_KARMA = 25  // Karma per ad
        const val AD_COOLDOWN_MS = 5 * 60 * 1000L  // 5 minutes cooldown
    }
    
    data class Quest(
        val id: String,
        val description: String,
        val target: Int,
        var progress: Int = 0,
        var isCompleted: Boolean = false,
        val reward: Int = REWARD_STANDARD
    )
    
    // Track karma earned from quests (for display purposes)
    var karmaFromQuests: Int
        get() = prefs.getInt(KEY_KARMA_FROM_QUESTS, 0)
        private set(value) = prefs.edit().putInt(KEY_KARMA_FROM_QUESTS, value).apply()
    
    // Callback to add karma to main activity
    var onKarmaEarned: ((Int) -> Unit)? = null
        
    private var _quests: MutableList<Quest> = mutableListOf()
    val quests: List<Quest>
        get() = _quests
        
    init {
        checkDailyReset()
        loadQuests()
    }
    
    /**
     * Add karma (uses callback to main activity)
     */
    fun addKarma(amount: Int) {
        karmaFromQuests += amount
        onKarmaEarned?.invoke(amount)
    }
    
    /**
     * Check if ad reward is available (cooldown expired)
     */
    fun isAdRewardAvailable(): Boolean {
        val lastTime = prefs.getLong(KEY_LAST_AD_REWARD_TIME, 0L)
        return System.currentTimeMillis() - lastTime >= AD_COOLDOWN_MS
    }
    
    /**
     * Get remaining cooldown time in milliseconds
     */
    fun getAdCooldownRemaining(): Long {
        val lastTime = prefs.getLong(KEY_LAST_AD_REWARD_TIME, 0L)
        val elapsed = System.currentTimeMillis() - lastTime
        val remaining = AD_COOLDOWN_MS - elapsed
        return if (remaining > 0) remaining else 0L
    }
    
    /**
     * Record that ad was watched (start cooldown)
     */
    fun recordAdWatched() {
        prefs.edit().putLong(KEY_LAST_AD_REWARD_TIME, System.currentTimeMillis()).apply()
    }
    
    /**
     * Format cooldown for display
     */
    fun formatCooldown(): String {
        val remaining = getAdCooldownRemaining()
        if (remaining <= 0) return "Ready!"
        val minutes = (remaining / 60000).toInt()
        val seconds = ((remaining % 60000) / 1000).toInt()
        return String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
    
    fun updateProgress(questId: String, amount: Int = 1) {
        val quest = _quests.find { it.id == questId } ?: return
        if (!quest.isCompleted) {
            quest.progress += amount
            if (quest.progress >= quest.target) {
                quest.progress = quest.target
                quest.isCompleted = true
                addKarma(quest.reward)  // Award Karma
            }
            saveQuests()
        }
    }
    
    private fun checkDailyReset() {
        val lastDate = prefs.getString(KEY_LAST_QUEST_DATE, "")
        val today = getTodayDate()
        
        if (lastDate != today) {
            generateNewQuests()
            prefs.edit().putString(KEY_LAST_QUEST_DATE, today).apply()
        }
    }
    
    private fun generateNewQuests() {
        _quests.clear()
        _quests.add(Quest(QUEST_MEDITATE_10_MIN, "Meditate for 10 minutes", 10))
        _quests.add(Quest(QUEST_USE_BELL, "Complete a session with a Bell", 1))
        _quests.add(Quest(QUEST_VISIT_PET, "Check on your Cosmic Companion", 1))
        saveQuests()
    }
    
    private fun saveQuests() {
        val serialized = _quests.joinToString("|") { "${it.id}:${it.progress}:${it.isCompleted}" }
        prefs.edit().putString(KEY_QUESTS_JSON, serialized).apply()
    }
    
    private fun loadQuests() {
        val serialized = prefs.getString(KEY_QUESTS_JSON, "") ?: ""
        if (serialized.isEmpty()) {
            if (_quests.isEmpty()) generateNewQuests()
            return
        }
        
        _quests.clear()
        try {
            val parts = serialized.split("|")
            for (part in parts) {
                val data = part.split(":")
                val id = data[0]
                val progress = data[1].toInt()
                val isCompleted = data[2].toBoolean()
                
                val (desc, target) = when(id) {
                    QUEST_MEDITATE_10_MIN -> "Meditate for 10 minutes" to 10
                    QUEST_USE_BELL -> "Complete a session with a Bell" to 1
                    QUEST_VISIT_PET -> "Check on your Cosmic Companion" to 1
                    else -> "Unknown Task" to 1
                }
                
                _quests.add(Quest(id, desc, target, progress, isCompleted))
            }
        } catch (e: Exception) {
            generateNewQuests()
        }
    }
    
    private fun getTodayDate(): String {
        val calendar = Calendar.getInstance()
        return String.format(Locale.US, "%04d-%02d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH))
    }
}
