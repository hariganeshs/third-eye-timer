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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.thirdeyetimer.app.ui.components.*
import com.thirdeyetimer.app.ui.theme.CosmicZenTheme
import com.thirdeyetimer.app.ui.theme.TerminalColors
import com.thirdeyetimer.app.ui.screens.AchievementItem
import com.thirdeyetimer.app.ui.screens.SoundOption
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdError
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.LocalFireDepartment

/**
 * MainActivityCompose
 * 
 * The new Compose-based main activity for Third Eye Timer.
 * This provides the stunning Cosmic Zen UI experience.
 */
class MainActivityCompose : ComponentActivity() {
    
    // Preferences
    // Preferences
    private val PREFS_NAME = "MeditationPrefs"
    private val KEY_TOTAL_TIME = "total_meditation_time"
    private val KEY_BELL_SOUND = "bell_sound"
    private val KEY_CURRENT_STREAK = "current_streak"
    private val KEY_LONGEST_STREAK = "longest_streak"
    private val KEY_LAST_MEDITATION_DATE = "last_meditation_date"
    private val KEY_TOTAL_HOURS = "total_hours"
    private val KEY_ACHIEVEMENTS = "achievements"
    private val KEY_KARMA = "karma_points"
    private val KEY_LEVEL = "user_level"
    
    // Ad Unit IDs - USING TEST IDS FOR DEVELOPMENT
    private val PROD_TOP_BANNER_AD_ID = "ca-app-pub-3940256099942544/6300978111" // Test Banner ID
    private val PROD_BOTTOM_BANNER_AD_ID = "ca-app-pub-3940256099942544/6300978111" // Test Banner ID
    private val PROD_INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/1033173712" // Test Interstitial ID
    
    // State
    private var totalMeditationTimeMillis: Long = 0L
    private var currentStreak: Int = 0
    private var longestStreak: Int = 0
    private var lastMeditationDate: String = ""
    private var achievementsUnlocked: MutableSet<String> = mutableSetOf()
    private var karmaPoints: Int = 0
    private var userLevel: String = "Seeker"
    private var selectedBellResId: Int = R.raw.bell_1
    private var selectedBackgroundResId: Int = 0
    private var selectedGuidedMeditationResId: Int = 0
    
    private var sessionStartTime: Long = 0L
    private var timeInMilliSeconds: Long = 0L
    private var initialTimeMillis: Long = 0L
    private var lastSessionDuration: Long = 0L
    private var interstitialAd: InterstitialAd? = null
    private lateinit var petManager: com.thirdeyetimer.app.domain.PetManager
    private lateinit var questManager: com.thirdeyetimer.app.domain.QuestManager
    private lateinit var idleGameManager: com.thirdeyetimer.app.domain.IdleGameManager
    private lateinit var upgradeManager: com.thirdeyetimer.app.domain.UpgradeManager
    private lateinit var truthPunchManager: com.thirdeyetimer.app.domain.TruthPunchManager
    private lateinit var nudgeManager: com.thirdeyetimer.app.domain.NudgeManager
    
    // Idle game tracking
    private var lastSpiritualEgoUpdateTime: Long = 0L
    private var sessionStartMinute: Double = 0.0
    
    // Data Lists
    private val achievementList = listOf(
        AchievementItem("first_hour", "First Hour", "Meditate for 1 hour total", Icons.Filled.Schedule, false),
        AchievementItem("dedicated_beginner", "Dedicated Beginner", "Meditate for 5 hours total", Icons.Filled.SelfImprovement, false),
        AchievementItem("mindful_explorer", "Mindful Explorer", "Meditate for 10 hours total", Icons.Filled.Explore, false),
        AchievementItem("meditation_master", "Meditation Master", "Meditate for 25 hours total", Icons.Filled.EmojiEvents, false),
        AchievementItem("consistent_practitioner", "3-Day Streak", "Meditate for 3 days in a row", Icons.Filled.LocalFireDepartment, false),
        AchievementItem("weekly_warrior", "Weekly Warrior", "Meditate for 7 days in a row", Icons.Filled.Bolt, false),
        AchievementItem("monthly_master", "Monthly Master", "Meditate for 30 days in a row", Icons.Filled.Nightlight, false)
    )

    private val availableBells = listOf(
        SoundOption(R.raw.bell_1, "Tibetan Bell", Icons.Filled.Notifications),
        SoundOption(R.raw.bell_2, "Meditation Chime", Icons.Filled.Notifications),
        SoundOption(R.raw.bell_3, "Zen Gong", Icons.Filled.Notifications),
        SoundOption(R.raw.bell_4, "Crystal Bowl", Icons.Filled.Notifications),
        SoundOption(R.raw.bell_5, "Temple Bell", Icons.Filled.Notifications),
        SoundOption(R.raw.bell_6, "Singing Bowl", Icons.Filled.Notifications)
    )

    private val availableBackgrounds = listOf(
        SoundOption(0, "None (Silence)", Icons.Filled.VolumeOff),
        SoundOption(R.raw.birds, "Bird Song", Icons.Filled.Park),
        SoundOption(R.raw.jungle_rain, "Jungle Rain", Icons.Filled.WaterDrop),
        SoundOption(R.raw.tibetan_chant, "Tibetan Chant", Icons.Filled.RecordVoiceOver),
        SoundOption(R.raw.aum_mantra, "Om Mantra", Icons.Filled.SelfImprovement)
    )
    
    // Preview player for sound selection
    private var previewPlayer: MediaPlayer? = null
    
    // Timer receiver
    private val TIMER_FINISHED_ACTION = "com.thirdeyetimer.app.TIMER_FINISHED"
    private val TIMER_TICK_ACTION = "com.thirdeyetimer.app.TIMER_TICK"

    
    // Compose state holders
    private var _appState = mutableStateOf(MeditationAppState())
    private var _currentScreen = mutableStateOf<AppScreen>(AppScreen.Home)
    private var showMindChatter = mutableStateOf(false)
    private var activeNudge = mutableStateOf<String?>(null)
    
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
        
        // Initialize Pet Manager
        petManager = com.thirdeyetimer.app.domain.PetManager(this)
        questManager = com.thirdeyetimer.app.domain.QuestManager(this)
        
        // Connect quest karma rewards to main karma tracking
        questManager.onKarmaEarned = { amount ->
            karmaPoints += amount
            savePreferences()
            updateAppState()
        }
        
        // Initialize Idle Game Managers
        idleGameManager = com.thirdeyetimer.app.domain.IdleGameManager(this)
        upgradeManager = com.thirdeyetimer.app.domain.UpgradeManager(this, idleGameManager)
        upgradeManager.initialize()
        
        // Initialize Truth Punch Manager
        truthPunchManager = com.thirdeyetimer.app.domain.TruthPunchManager(this)
        truthPunchManager.checkUnlocks(idleGameManager.lifetimeSpiritualEgo)
        
        // Initialize Nudge Manager
        nudgeManager = com.thirdeyetimer.app.domain.NudgeManager(this)
        
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
                    SocietalProgrammingFrame(label = "PRIORITY SOCIETAL FEED") {
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
                    }
                    
                    // Main app content
                    Box(modifier = Modifier.weight(1f)) {
                        ThirdEyeTimerApp(
                            state = appState,
                            currentScreen = currentScreen,
                            onTimeInputChange = { updateTimeInput(it) },
                            onStartClick = { handleStartPause() },
                            onPauseResumeClick = { handlePauseResume() },
                            onStopClick = { handleStop() },
                            onSoundSettingsClick = { 
                                updateAppState()
                                _currentScreen.value = AppScreen.SoundSettings 
                            },
                            onAchievementsClick = { 
                                updateAppState()
                                _currentScreen.value = AppScreen.Achievements 
                            },
                            onPetClick = {
                                updateAppState()
                                questManager.updateProgress(com.thirdeyetimer.app.domain.QuestManager.QUEST_VISIT_PET, 1)
                                _currentScreen.value = AppScreen.Pet
                            },
                            onFeedPetClick = {
                                showRewardedAd("divine treats") {
                                    petManager.feedPet()
                                }
                            },
                            onQuestsClick = {
                                updateAppState()
                                _currentScreen.value = AppScreen.Quests
                            },
                            onWatchAdForKarma = {
                                if (questManager.isAdRewardAvailable()) {
                                    showRewardedAd("imaginary merit points") {
                                        karmaPoints += com.thirdeyetimer.app.domain.QuestManager.AD_REWARD_KARMA
                                        questManager.recordAdWatched()
                                        savePreferences()
                                        updateAppState()
                                    }
                                }
                            },
                            onBrowseSessionsClick = { _currentScreen.value = AppScreen.Sessions },
                            onMeditationSelected = { id -> handleMeditationSelected(id) },
                            onStartAnotherClick = { handleStartAnother() },
                            onShareClick = { handleShare() },
                            onDismiss = { 
                                stopPreviewSound()
                                _currentScreen.value = AppScreen.Home 
                            },
                            onBellSelected = { id -> 
                                selectedBellResId = id
                                savePreferences()
                                updateAppState()
                                playPreviewSound(id)
                            },
                            onBackgroundSelected = { id -> 
                                val premiumIds = listOf(R.raw.jungle_rain, R.raw.tibetan_chant)
                                if (id in premiumIds) {
                                    showRewardedAd("cosmic ambience") {
                                        selectedBackgroundResId = id
                                        savePreferences()
                                        updateAppState()
                                        playPreviewSound(id)
                                    }
                                } else {
                                    selectedBackgroundResId = id
                                    savePreferences()
                                    updateAppState()
                                    if (id != 0) playPreviewSound(id) else stopPreviewSound()
                                }
                            },
                            onUpgradeShopClick = {
                                updateAppState()
                                _currentScreen.value = AppScreen.UpgradeShop
                            },
                            onPurchaseUpgrade = { upgrade ->
                                val cost = upgradeManager.purchaseUpgrade(upgrade, karmaPoints)
                                if (cost > 0) {
                                    karmaPoints -= cost
                                    savePreferences()
                                    updateAppState()
                                }
                            },
                            onWatchAdForDoubleSpiritualEgo = {
                                showRewardedAd("vibrational alignment") {
                                    idleGameManager.doubleSessionSpiritualEgo()
                                    updateAppState()
                                }
                            },
                            onTruthPunchesClick = {
                                updateAppState()
                                _currentScreen.value = AppScreen.TruthPunches
                            },
                            onTruthClick = { truth ->
                                truthPunchManager.markAsSeen(truth.rank)
                                updateAppState()
                            },
                            getTierName = { tier -> truthPunchManager.getTierName(tier) },
                            getTierSubtitle = { tier -> truthPunchManager.getTierSubtitle(tier) },
                            onWatchAdToBypassWaitWall = {
                                showRewardedAd("fatigue bypass") {
                                    idleGameManager.bypassWaitWall()
                                    updateAppState()
                                }
                            },
                            onVibeCheckClick = {
                                // Navigate to Aura Selfie screen
                                _currentScreen.value = AppScreen.AuraSelfie
                            },
                            onScreamJarClick = {
                                _currentScreen.value = AppScreen.ScreamJar
                            },
                            onAuraShare = { uri ->
                                // Record that Vibe Check was completed and award karma
                                questManager.recordVibeCheckCompleted()
                                updateAppState()
                                
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/jpeg"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_TEXT, "Check out my aura! #ThirdEyeTimer #VibeCheck")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                startActivity(Intent.createChooser(shareIntent, "Share your aura"))
                            }
                        )

                        // Ad payoffs and transitions
                        val rewardMessage by rewardedAdPayoffMessage
                        rewardMessage?.let {
                            ShortcutAftermathFrame(
                                rewardDescription = it,
                                onDismiss = { rewardedAdPayoffMessage.value = null }
                            )
                        }

                        if (showMindChatter.value) {
                            MindChatterFrame(
                                onComplete = {
                                    showMindChatter.value = false
                                    interstitialAd?.show(this@MainActivityCompose)
                                }
                            )
                        }
                    }
                    
                     // Bottom ad banner (only show on Home screen)
                    if (currentScreen == AppScreen.Home) {
                        SocietalProgrammingFrame(label = "SECONDARY SUBCONSCIOUS FEED") {
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
                    
                    // Nudge Dialog (Phase 4 Satire)
                    activeNudge.value?.let { nudgeText ->
                        BrutalistDialog(
                            title = "SYSTEM ADVISORY",
                            onDismiss = { activeNudge.value = null }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = nudgeText,
                                    color = TerminalColors.TerminalWhite,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                                TerminalButton(
                                    text = "I WILL IGNORE THIS",
                                    onClick = { activeNudge.value = null },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
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
        karmaPoints = prefs.getInt(KEY_KARMA, 0)
        userLevel = prefs.getString(KEY_LEVEL, "Seeker") ?: "Seeker"
    }
    
    private fun savePreferences() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit()
            .putLong(KEY_TOTAL_TIME, totalMeditationTimeMillis)
            .putInt(KEY_CURRENT_STREAK, currentStreak)
            .putInt(KEY_LONGEST_STREAK, longestStreak)
            .putString(KEY_LAST_MEDITATION_DATE, lastMeditationDate)
            .putStringSet(KEY_ACHIEVEMENTS, achievementsUnlocked)
            .putInt(KEY_KARMA, karmaPoints)
            .putString(KEY_LEVEL, userLevel)
            .apply()
    }
    
    private fun initializeAds() {
        MobileAds.initialize(this) { 
            Log.d("MainActivityCompose", "AdMob initialized")
            loadInterstitialAd()
            loadRewardedAd()
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
    
     private fun showInterstitialAd(onAdDismissed: () -> Unit = {}) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd()
                    onAdDismissed()
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    onAdDismissed()
                }
            }
            showMindChatter.value = true
        } else {
            onAdDismissed()
        }
    }
    
    // Rewarded Ads
    private var rewardedAd: com.google.android.gms.ads.rewarded.RewardedAd? = null
    private val PROD_REWARDED_AD_ID = "ca-app-pub-3940256099942544/5224354917" // Test Rewarded ID

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        com.google.android.gms.ads.rewarded.RewardedAd.load(
            this,
            PROD_REWARDED_AD_ID,
            adRequest,
            object : com.google.android.gms.ads.rewarded.RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                }
                override fun onAdLoaded(ad: com.google.android.gms.ads.rewarded.RewardedAd) {
                    rewardedAd = ad
                }
            })
    }
    
    private var rewardedAdPayoffMessage = mutableStateOf<String?>(null)

    private fun showRewardedAd(rewardName: String, onRewardEarned: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.show(this) { _ ->
                onRewardEarned()
                rewardedAdPayoffMessage.value = rewardName
                loadRewardedAd()
            }
        } else {
            Log.d("MainActivityCompose", "Ad not ready, granting reward anyway (fallback)")
            onRewardEarned()
            rewardedAdPayoffMessage.value = rewardName
            loadRewardedAd()
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
        
        Log.d("MainActivityCompose", "updateAppState: bells=${availableBells.size}, backgrounds=${availableBackgrounds.size}, achievements=${updatedAchievements.size}")
        
        _appState.value = _appState.value.copy(
            totalMeditationTime = totalTimeText,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            achievementList = updatedAchievements,
            selectedBellId = selectedBellResId,
            selectedBackgroundId = selectedBackgroundResId,
            availableBells = availableBells,
            availableBackgrounds = availableBackgrounds,
            userLevel = truthPunchManager.getCurrentLevelTitle(),
            userLevelInt = truthPunchManager.getLevel(),
            karmaPoints = karmaPoints,
            // Idle game state
            totalSpiritualEgo = idleGameManager.totalSpiritualEgo,
            lifetimeSpiritualEgo = idleGameManager.lifetimeSpiritualEgo,
            sessionSpiritualEgo = idleGameManager.sessionSpiritualEgo,
            totalMultiplier = upgradeManager.calculateTotalMultiplier(),
            upgradeStatuses = upgradeManager.getUpgradeStatuses(),
            // Truth Punch state
            allTruths = truthPunchManager.getAllTruths(),
            nextUnlockThreshold = truthPunchManager.getNextUnlockThreshold(),
            truthProgress = truthPunchManager.getOverallProgress(),
            unseenTruthCount = truthPunchManager.getUnseenCount(),
            // Wait Wall state
            isWaitWallActive = idleGameManager.isWaitWallActive(),
            waitWallRemainingMs = idleGameManager.getWaitWallRemainingMs()
        )
    }
    
    private fun updateTimeInput(input: String) {
        _appState.value = _appState.value.copy(timeInput = input)
    }
    
    private fun playPreviewSound(resId: Int) {
        try {
            stopPreviewSound()
            previewPlayer = MediaPlayer.create(this, resId)?.apply {
                setVolume(1.0f, 1.0f)
                setOnCompletionListener { 
                    it.release()
                    if (previewPlayer == it) previewPlayer = null
                }
                start()
            }
        } catch (e: Exception) {
            Log.e("MainActivityCompose", "Error playing preview sound: ${e.message}")
        }
    }
    
    private fun stopPreviewSound() {
        try {
            previewPlayer?.stop()
            previewPlayer?.release()
            previewPlayer = null
        } catch (e: Exception) {
            Log.e("MainActivityCompose", "Error stopping preview sound: ${e.message}")
        }
    }
    
    private fun handleStartPause() {
        val state = _appState.value
        
        if (state.isRunning) {
            // Pause
            handlePauseResume()
        } else {
            // Start
            if (state.isWaitWallActive) {
                // Cannot start if wait wall is active
                _appState.value = state.copy(timeInput = "Wait...") 
                return
            }
            
            val timeInput = state.timeInput
            if (timeInput.isNotEmpty()) {
                val minutes = timeInput.toLongOrNull() ?: return
                timeInMilliSeconds = minutes * 60000L
                initialTimeMillis = timeInMilliSeconds
                sessionStartTime = System.currentTimeMillis()
                
                // Reset Spiritual Ego session tracking
                idleGameManager.resetSession()
                lastSpiritualEgoUpdateTime = System.currentTimeMillis()
                
                startTimerService(timeInMilliSeconds, selectedBellResId)
                
                _appState.value = state.copy(
                    isRunning = true,
                    isPaused = false,
                    sessionSpiritualEgo = 0L,
                    spiritualEgoPerSecond = 0.0
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
        
        // Calculate Karma: 10 points per minute
        val minutesMeditated = (sessionDuration / 60000).toInt()
        val karmaEarned = minutesMeditated * 10
        karmaPoints += karmaEarned
        
        // Update Pet XP
        if (minutesMeditated > 0) {
            petManager.addExp(minutesMeditated)
            // Update Quest: Meditate 10 min
            questManager.updateProgress(com.thirdeyetimer.app.domain.QuestManager.QUEST_MEDITATE_10_MIN, minutesMeditated)
        }
        
        // Quest: Use Bell (if bell was used)
        if (selectedBellResId != 0) { 
             questManager.updateProgress(com.thirdeyetimer.app.domain.QuestManager.QUEST_USE_BELL, 1)
        }
        
        // Commit Spiritual Ego earned in this session
        val spiritualEgoEarned = idleGameManager.commitSession()
        lastSpiritualEgoUpdateTime = 0L  // Reset for next session
        
        // Check for newly unlocked truths
        val newTruths = truthPunchManager.checkUnlocks(idleGameManager.lifetimeSpiritualEgo)
        // TODO: Could show a popup for newTruths if desired
        
        // Update Level based on lifetime Spiritual Ego (permanent XP)
        // Update Level based on Truths Unlocked
        userLevel = truthPunchManager.getCurrentLevelTitle()
        
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
            timerText = "Done!",
            sessionSpiritualEgoEarned = spiritualEgoEarned,
            showDoubleAdButton = true
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
        
        // Calculate elapsed time for Spiritual Ego calculation
        val elapsedTimeMs = initialTimeMillis - remainingTime
        val elapsedMinutes = elapsedTimeMs / 60000.0
        
        // Calculate Spiritual Ego per second based on current state
        val spiritualEgoPerSecond = idleGameManager.calculateSpiritualEgoPerSecond(
            streakDays = currentStreak,
            elapsedMinutes = elapsedMinutes
        )
        
        // Calculate Spiritual Ego earned since last tick using precise accumulation
        val currentTime = System.currentTimeMillis()
        if (lastSpiritualEgoUpdateTime > 0) {
            val deltaSeconds = (currentTime - lastSpiritualEgoUpdateTime) / 1000.0
            // Use the improved accumulation method that handles fractional Spiritual Ego
            idleGameManager.accumulateSpiritualEgo(
                streakDays = currentStreak,
                elapsedMinutes = elapsedMinutes,
                deltaSeconds = deltaSeconds
            )
        }
        lastSpiritualEgoUpdateTime = currentTime
        
        _appState.value = _appState.value.copy(
            timerText = timerText,
            progress = progress.coerceIn(0f, 1f),
            sessionSpiritualEgo = idleGameManager.sessionSpiritualEgo,
            spiritualEgoPerSecond = spiritualEgoPerSecond
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
        showInterstitialAd {
            _currentScreen.value = AppScreen.Home
            updateAppState()
            
            // Randomly nudge user to stop playing (Phase 4 satire)
            if (nudgeManager.shouldShowNudge()) {
                activeNudge.value = nudgeManager.getRandomNudge()
            }
        }
    }
    
    private fun handleShare() {
        // Generate Brag Card logic
        val thread = Thread {
            try {
                val bitmap = com.thirdeyetimer.app.utils.SocialManager.generateBragCardBitmap(
                    context = this,
                    username = "Mindful Soul",
                    streakDays = currentStreak,
                    totalMinutes = totalMeditationTimeMillis / 60000,
                    level = userLevel,
                    spiritualEgo = idleGameManager.lifetimeSpiritualEgo
                )
                
                runOnUiThread {
                    com.thirdeyetimer.app.utils.SocialManager.shareImage(
                        this,
                        bitmap,
                        "🧘 Just reached $userLevel level on Third Eye Timer! #ThirdEyeTimer #Meditation"
                    )
                }
            } catch (e: Exception) {
                Log.e("MainActivityCompose", "Error generating brag card: ${e.message}")
            }
        }
        thread.start()
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
    
    override fun onResume() {
        super.onResume()
        checkStreakrescue()
    }

    private fun checkStreakrescue() {
        // Simple logic: if last meditation was > 1 day ago but < 3 days ago, offer rescue
        if (lastMeditationDate.isNotEmpty()) {
            try {
                val lastDateParts = lastMeditationDate.split("-")
                val lastCalendar = Calendar.getInstance()
                lastCalendar.set(lastDateParts[0].toInt(), lastDateParts[1].toInt() - 1, lastDateParts[2].toInt())
                val todayCalendar = Calendar.getInstance()
                
                // Clear times for date calc
                lastCalendar.set(Calendar.HOUR_OF_DAY, 0)
                lastCalendar.set(Calendar.MINUTE, 0)
                lastCalendar.set(Calendar.SECOND, 0)
                lastCalendar.set(Calendar.MILLISECOND, 0)
                
                todayCalendar.set(Calendar.HOUR_OF_DAY, 0)
                todayCalendar.set(Calendar.MINUTE, 0)
                todayCalendar.set(Calendar.SECOND, 0)
                todayCalendar.set(Calendar.MILLISECOND, 0)

                val diffInDays = (todayCalendar.timeInMillis - lastCalendar.timeInMillis) / (24 * 60 * 60 * 1000)
                
                if (diffInDays > 1L && diffInDays <= 2L) {
                    // Show rescue dialog
                     android.app.AlertDialog.Builder(this)
                        .setTitle("🔥 Oh no! Streak at Risk!")
                        .setMessage("You missed a day! Watch a short video to restore your $longestStreak day streak?")
                        .setPositiveButton("Rescue Streak") { _, _ ->
                            showRewardedAd("streak salvation") {
                                // Restore streak
                                lastMeditationDate = String.format(Locale.US, "%04d-%02d-%02d",
                                    todayCalendar.get(Calendar.YEAR),
                                    todayCalendar.get(Calendar.MONTH) + 1,
                                    todayCalendar.get(Calendar.DAY_OF_MONTH))
                                currentStreak = longestStreak // Or previous streak
                                // Ideally we track 'previousStreak', but for MVP we assume longest = current before break if high
                                // Or just set currentStreak back to what it was? 
                                // Actually 'longestStreak' tracks the max. 'currentStreak' is what we lost.
                                // We don't have 'previousStreak' saved separately. 
                                // Approximation: Restore to longestStreak if reasonable, or just 1.
                                // Let's restore to longestStreak to be generous (Viral!)
                                currentStreak = longestStreak 
                                savePreferences()
                                updateAppState()
                            }
                        }
                        .setNegativeButton("Let it go", null)
                        .show()
                }
            } catch (e: Exception) {
                Log.e("MainActivityCompose", "Error checking streak: ${e.message}")
            }
        }
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
