package com.thirdeyetimer.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.LoadAdError
import com.thirdeyetimer.app.ui.*
import com.thirdeyetimer.app.ui.theme.CosmicZenTheme
import com.thirdeyetimer.app.ui.screens.AchievementItem
import com.thirdeyetimer.app.ui.screens.SoundOption
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdError

/**
 * MainActivityCompose
 * 
 * The new Compose-based main activity for Third Eye Timer.
 * This provides the stunning Cosmic Zen UI experience.
 */
class MainActivityCompose : ComponentActivity() {
    
    // Preferences
    private val PREFS_NAME = "MeditationPrefs"
    private val KEY_TOTAL_TIME = "total_meditation_time"
    private val KEY_BELL_SOUND = "bell_sound"
    private val KEY_CURRENT_STREAK = "current_streak"
    private val KEY_LONGEST_STREAK = "longest_streak"
    private val KEY_LAST_MEDITATION_DATE = "last_meditation_date"
    private val KEY_TOTAL_HOURS = "total_hours"
    private val KEY_ACHIEVEMENTS = "achievements"
    
    // Ad Unit IDs - USING TEST IDS
    private val PROD_TOP_BANNER_AD_ID = "ca-app-pub-3940256099942544/6300978111" // Test Banner ID
    private val PROD_BOTTOM_BANNER_AD_ID = "ca-app-pub-3940256099942544/6300978111" // Test Banner ID
    private val PROD_INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/1033173712" // Test Interstitial ID
    
    // State
    private var totalMeditationTimeMillis: Long = 0L
    private var currentStreak: Int = 0
    private var longestStreak: Int = 0
    private var lastMeditationDate: String = ""
    private var achievementsUnlocked: MutableSet<String> = mutableSetOf()
    private var selectedBellResId: Int = R.raw.bell_1
    private var selectedBackgroundResId: Int = 0
    private var selectedGuidedMeditationResId: Int = 0
    
    private var sessionStartTime: Long = 0L
    private var timeInMilliSeconds: Long = 0L
    private var initialTimeMillis: Long = 0L
    private var lastSessionDuration: Long = 0L
    private var interstitialAd: InterstitialAd? = null
    
    // Data Lists
    private val achievementList = listOf(
        AchievementItem("first_hour", "First Hour", "Meditate for 1 hour total", "🌅", false),
        AchievementItem("dedicated_beginner", "Dedicated Beginner", "Meditate for 5 hours total", "🌟", false),
        AchievementItem("mindful_explorer", "Mindful Explorer", "Meditate for 10 hours total", "🧘‍♀️", false),
        AchievementItem("meditation_master", "Meditation Master", "Meditate for 25 hours total", "👑", false),
        AchievementItem("consistent_practitioner", "3-Day Streak", "Meditate for 3 days in a row", "🔥", false),
        AchievementItem("weekly_warrior", "Weekly Warrior", "Meditate for 7 days in a row", "⚡", false),
        AchievementItem("monthly_master", "Monthly Master", "Meditate for 30 days in a row", "🌙", false)
    )

    private val availableBells = listOf(
        SoundOption(R.raw.bell_1, "Tibetan Bell 1", "🔔"),
        SoundOption(R.raw.bell_2, "Tibetan Bell 2", "🔔"),
        SoundOption(R.raw.bell_3, "Zen Gong", "🛎️"),
        SoundOption(R.raw.bell_4, "Crystal Bowl", "🥣")
    )

    private val availableBackgrounds = listOf(
        SoundOption(0, "None", "🔇"),
        SoundOption(R.raw.rain, "Rain", "🌧️"),
        SoundOption(R.raw.forest, "Forest", "🌲"),
        SoundOption(R.raw.ocean, "Ocean", "🌊"),
        SoundOption(R.raw.river, "River", "💧"),
        SoundOption(R.raw.temple, "Temple", "🕌")
    )
    
    // Timer receiver
    private val TIMER_FINISHED_ACTION = "com.thirdeyetimer.app.TIMER_FINISHED"
    private val TIMER_TICK_ACTION = "com.thirdeyetimer.app.TIMER_TICK"

    
    // Compose state holders
    private var _appState = mutableStateOf(MeditationAppState())
    private var _currentScreen = mutableStateOf<AppScreen>(AppScreen.Home)
    
    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                TIMER_FINISHED_ACTION -> {
                    onTimerFinished()
                }
                TIMER_TICK_ACTION -> {
                    val remainingTime = intent.getLongExtra(MeditationTimerService.EXTRA_REMAINING_TIME, 0L)
                    timeInMilliSeconds = remainingTime
                    updateTimerState(remainingTime)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivityCompose", "onCreate started")
        
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Load saved data
        loadPreferences()
        
        // Initialize ads
        initializeAds()
        
        // Register timer receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
            timerReceiver,
            IntentFilter().apply {
                addAction(TIMER_FINISHED_ACTION)
                addAction(TIMER_TICK_ACTION)

            }
        )
        
        // Set Compose content
        setContent {
            val appState by _appState
            val currentScreen by _currentScreen
            
            CosmicZenTheme {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top ad banner
                    AndroidView(
                        factory = { context ->
                            AdView(context).apply {
                                setAdSize(AdSize.BANNER)
                                adUnitId = PROD_TOP_BANNER_AD_ID
                                loadAd(AdRequest.Builder().build())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                    
                    // Main app content
                    Box(modifier = Modifier.weight(1f)) {
                        ThirdEyeTimerApp(
                            state = appState,
                            currentScreen = currentScreen,
                            onTimeInputChange = { updateTimeInput(it) },
                            onStartClick = { handleStartPause() },
                            onPauseResumeClick = { handlePauseResume() },
                            onStopClick = { handleStop() },
                            onSoundSettingsClick = { _currentScreen.value = AppScreen.SoundSettings },
                            onAchievementsClick = { _currentScreen.value = AppScreen.Achievements },
                            onBrowseSessionsClick = { _currentScreen.value = AppScreen.Sessions },
                            onMeditationSelected = { id -> handleMeditationSelected(id) },
                            onStartAnotherClick = { handleStartAnother() },
                            onShareClick = { handleShare() },
                            onDismiss = { _currentScreen.value = AppScreen.Home },
                            onBellSelected = { id -> 
                                selectedBellResId = id
                                savePreferences()
                                updateAppState()
                            },
                            onBackgroundSelected = { id -> 
                                selectedBackgroundResId = id
                                savePreferences()
                                updateAppState()
                            }
                        )
                    }
                    
                    // Bottom ad banner (only show on Home screen)
                    if (currentScreen == AppScreen.Home) {
                        AndroidView(
                            factory = { context ->
                                AdView(context).apply {
                                    setAdSize(AdSize.BANNER)
                                    adUnitId = PROD_BOTTOM_BANNER_AD_ID
                                    loadAd(AdRequest.Builder().build())
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        )
                    }
                }
            }
        }
        
        // Update initial state
        updateAppState()
    }
    
    private fun loadPreferences() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        totalMeditationTimeMillis = prefs.getLong(KEY_TOTAL_TIME, 0L)
        currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0)
        longestStreak = prefs.getInt(KEY_LONGEST_STREAK, 0)
        lastMeditationDate = prefs.getString(KEY_LAST_MEDITATION_DATE, "") ?: ""
        achievementsUnlocked = prefs.getStringSet(KEY_ACHIEVEMENTS, setOf())?.toMutableSet() ?: mutableSetOf()
        selectedBellResId = prefs.getInt(KEY_BELL_SOUND, R.raw.bell_1)
        selectedBackgroundResId = prefs.getInt("KEY_BACKGROUND_SOUND", 0)
        selectedGuidedMeditationResId = prefs.getInt("KEY_GUIDED_MEDITATION_SOUND", 0)
    }
    
    private fun savePreferences() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit()
            .putLong(KEY_TOTAL_TIME, totalMeditationTimeMillis)
            .putInt(KEY_CURRENT_STREAK, currentStreak)
            .putInt(KEY_LONGEST_STREAK, longestStreak)
            .putString(KEY_LAST_MEDITATION_DATE, lastMeditationDate)
            .putStringSet(KEY_ACHIEVEMENTS, achievementsUnlocked)
            .apply()
    }
    
    private fun initializeAds() {
        MobileAds.initialize(this) { 
            Log.d("MainActivityCompose", "AdMob initialized")
            loadInterstitialAd()
        }
    }
    
    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            PROD_INTERSTITIAL_AD_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    setupInterstitialCallbacks()
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }
    
    private fun setupInterstitialCallbacks() {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadInterstitialAd()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
            }
        }
    }
    
    private fun updateAppState() {
        val hours = totalMeditationTimeMillis / (1000 * 60 * 60)
        val minutes = (totalMeditationTimeMillis % (1000 * 60 * 60)) / (1000 * 60)
        val totalTimeText = String.format(Locale.getDefault(), "%02dh %02dm", hours, minutes)
        
        // Update achievement unlock status
        val updatedAchievements = achievementList.map { item ->
            item.copy(isUnlocked = achievementsUnlocked.contains(item.id))
        }
        
        _appState.value = _appState.value.copy(
            totalMeditationTime = totalTimeText,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            achievementList = updatedAchievements,
            selectedBellId = selectedBellResId,
            selectedBackgroundId = selectedBackgroundResId,
            availableBells = availableBells,
            availableBackgrounds = availableBackgrounds
        )
    }
    
    private fun updateTimeInput(input: String) {
        _appState.value = _appState.value.copy(timeInput = input)
    }
    
    private fun handleStartPause() {
        val state = _appState.value
        
        if (state.isRunning) {
            // Pause
            handlePauseResume()
        } else {
            // Start
            val timeInput = state.timeInput
            if (timeInput.isNotEmpty()) {
                val minutes = timeInput.toLongOrNull() ?: return
                timeInMilliSeconds = minutes * 60000L
                initialTimeMillis = timeInMilliSeconds
                sessionStartTime = System.currentTimeMillis()
                
                startTimerService(timeInMilliSeconds, selectedBellResId)
                
                _appState.value = state.copy(
                    isRunning = true,
                    isPaused = false
                )
                _currentScreen.value = AppScreen.Meditation
            }
        }
    }
    
    private fun handlePauseResume() {
        val state = _appState.value
        
        if (state.isPaused) {
            // Resume
            startTimerService(timeInMilliSeconds, selectedBellResId)
            _appState.value = state.copy(isPaused = false, isRunning = true)
        } else {
            // Pause
            stopTimerService()
            _appState.value = state.copy(isPaused = true, isRunning = false)
        }
    }
    
    private fun handleMeditationSelected(resourceId: Int) {
        val meditation = GuidedMeditationData.getMeditationByResourceId(resourceId) ?: return
        
        // Parse duration string (e.g. "15 min") to millis
        var durationMillis = 15 * 60 * 1000L // Default fallback
        try {
            val minutes = meditation.duration.split(" ")[0].toLong()
            durationMillis = minutes * 60 * 1000L
        } catch (e: Exception) {
            Log.e("MainActivityCompose", "Error parsing duration: ${meditation.duration}")
        }
        
        timeInMilliSeconds = durationMillis
        initialTimeMillis = timeInMilliSeconds
        sessionStartTime = System.currentTimeMillis()
        
        // Update state
        _appState.value = _appState.value.copy(
            isRunning = true,
            isPaused = false,
            guidedMeditationName = meditation.title,
            timeInput = (durationMillis / 60000).toString()
        )
        
        // Start service with guided meditation
        selectedGuidedMeditationResId = resourceId
        savePreferences() // Ideally we should save this preference
        
        startTimerService(timeInMilliSeconds, 0) // 0 for bell means use guided audio only if needed
        
        _currentScreen.value = AppScreen.Meditation
    }

    private fun handleStop() {
        stopTimerService()
        
        // Calculate session duration
        val sessionEndTime = System.currentTimeMillis()
        val sessionDuration = (sessionEndTime - sessionStartTime).coerceAtLeast(0L)
        lastSessionDuration = sessionDuration
        
        // Update totals
        totalMeditationTimeMillis += sessionDuration
        updateStreak()
        savePreferences()
        
        // Format session duration
        val durationMinutes = sessionDuration / 60000
        val durationSeconds = (sessionDuration % 60000) / 1000
        val durationText = String.format(Locale.getDefault(), "%02d:%02d", durationMinutes, durationSeconds)
        
        _appState.value = _appState.value.copy(
            isRunning = false,
            isPaused = false,
            sessionDuration = durationText,
            guidedMeditationName = null // Reset guided name
        )
        
        updateAppState()
        _currentScreen.value = AppScreen.Completion
    }
    
    private fun onTimerFinished() {
        val sessionEndTime = System.currentTimeMillis()
        val sessionDuration = (sessionEndTime - sessionStartTime).coerceAtLeast(0L)
        lastSessionDuration = sessionDuration
        
        totalMeditationTimeMillis += sessionDuration
        updateStreak()
        checkAchievements()
        savePreferences()
        
        val durationMinutes = initialTimeMillis / 60000
        val durationSeconds = (initialTimeMillis % 60000) / 1000
        val durationText = String.format(Locale.getDefault(), "%02d:%02d", durationMinutes, durationSeconds)
        
        _appState.value = _appState.value.copy(
            isRunning = false,
            isPaused = false,
            sessionDuration = durationText,
            progress = 1f,
            timerText = "Done!"
        )
        
        updateAppState()
        _currentScreen.value = AppScreen.Completion
    }
    
    private fun updateTimerState(remainingTime: Long) {
        val minutes = (remainingTime / 1000) / 60
        val seconds = (remainingTime / 1000) % 60
        val timerText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        
        val progress = if (initialTimeMillis > 0) {
            1f - (remainingTime.toFloat() / initialTimeMillis.toFloat())
        } else 0f
        
        _appState.value = _appState.value.copy(
            timerText = timerText,
            progress = progress.coerceIn(0f, 1f)
        )
    }
    
    private fun startTimerService(time: Long, bellResId: Int) {
        val intent = Intent(this, MeditationTimerService::class.java).apply {
            action = MeditationTimerService.ACTION_START
            putExtra(MeditationTimerService.EXTRA_TIME_MILLIS, time)
            putExtra(MeditationTimerService.EXTRA_BELL_RES_ID, bellResId)
            putExtra(MeditationTimerService.EXTRA_BACKGROUND_RES_ID, selectedBackgroundResId)
            putExtra(MeditationTimerService.EXTRA_GUIDED_RES_ID, selectedGuidedMeditationResId)
        }
        startService(intent)
    }
    
    private fun stopTimerService() {
        val intent = Intent(this, MeditationTimerService::class.java).apply {
            action = MeditationTimerService.ACTION_STOP
        }
        startService(intent)
    }
    
    private fun updateStreak() {
        val calendar = Calendar.getInstance()
        val today = String.format(Locale.US, "%04d-%02d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH))
        
        if (lastMeditationDate.isEmpty()) {
            currentStreak = 1
        } else {
            try {
                val lastDateParts = lastMeditationDate.split("-")
                val lastCalendar = Calendar.getInstance()
                lastCalendar.set(lastDateParts[0].toInt(), lastDateParts[1].toInt() - 1, lastDateParts[2].toInt())
                
                val todayCalendar = Calendar.getInstance()
                todayCalendar.set(Calendar.HOUR_OF_DAY, 0)
                todayCalendar.set(Calendar.MINUTE, 0)
                todayCalendar.set(Calendar.SECOND, 0)
                todayCalendar.set(Calendar.MILLISECOND, 0)
                
                lastCalendar.set(Calendar.HOUR_OF_DAY, 0)
                lastCalendar.set(Calendar.MINUTE, 0)
                lastCalendar.set(Calendar.SECOND, 0)
                lastCalendar.set(Calendar.MILLISECOND, 0)
                
                val diffInDays = (todayCalendar.timeInMillis - lastCalendar.timeInMillis) / (24 * 60 * 60 * 1000)
                
                when {
                    diffInDays == 1L -> currentStreak++
                    diffInDays > 1L -> currentStreak = 1
                }
            } catch (e: Exception) {
                currentStreak = 1
            }
        }
        
        lastMeditationDate = today
        if (currentStreak > longestStreak) {
            longestStreak = currentStreak
        }
    }
    
    private fun checkAchievements(): String? {
        val totalHours = (totalMeditationTimeMillis / (1000 * 60 * 60)).toInt()
        var newAchievement: String? = null
        
        val achievementChecks = listOf(
            Triple("first_hour", 1, "🌅 First Hour"),
            Triple("dedicated_beginner", 5, "🌟 Dedicated Beginner"),
            Triple("mindful_explorer", 10, "🧘‍♀️ Mindful Explorer"),
            Triple("meditation_master", 25, "👑 Meditation Master")
        )
        
        for ((key, hours, name) in achievementChecks) {
            if (totalHours >= hours && !achievementsUnlocked.contains(key)) {
                achievementsUnlocked.add(key)
                newAchievement = name
            }
        }
        
        val streakAchievements = listOf(
            Triple("consistent_practitioner", 3, "🔥 3-Day Streak"),
            Triple("weekly_warrior", 7, "⚡ Weekly Warrior"),
            Triple("monthly_master", 30, "🌙 Monthly Master")
        )
        
        for ((key, days, name) in streakAchievements) {
            if (currentStreak >= days && !achievementsUnlocked.contains(key)) {
                achievementsUnlocked.add(key)
                newAchievement = name
            }
        }
        
        if (newAchievement != null) {
            _appState.value = _appState.value.copy(newAchievement = newAchievement)
        }
        
        return newAchievement
    }
    
    private fun handleStartAnother() {
        _appState.value = MeditationAppState(
            totalMeditationTime = _appState.value.totalMeditationTime,
            currentStreak = currentStreak
        )
        _currentScreen.value = AppScreen.Home
    }
    
    private fun handleShare() {
        val state = _appState.value
        val shareText = """
            🧘 Just completed a ${state.sessionDuration} meditation session with Third Eye Timer!
            
            🔥 Current streak: ${state.currentStreak} days
            ⏱️ Total meditation time: ${state.totalMeditationTime}
            
            Find your inner peace with Third Eye Timer 🪷
        """.trimIndent()
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Share your meditation"))
    }
    
    private fun showSoundSettings() {
        // For now, use the legacy dialog - will be replaced with Compose screen
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra("show_sound_settings", true)
        })
    }
    
    private fun showAchievements() {
        // For now, use the legacy dialog - will be replaced with Compose screen
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra("show_achievements", true)
        })
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(timerReceiver)
        } catch (e: Exception) {
            Log.e("MainActivityCompose", "Error unregistering receiver: ${e.message}")
        }
    }
}
