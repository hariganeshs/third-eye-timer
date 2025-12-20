package com.thirdeyetimer.app.ui

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.thirdeyetimer.app.ui.screens.*
import com.thirdeyetimer.app.ui.theme.CosmicZenTheme
import com.thirdeyetimer.app.domain.TruthPunchManager

/**
 * Main App Compose Entry Point
 * 
 * This composable serves as the main entry point for the Compose UI.
 * It manages navigation between screens and holds the app state.
 */
sealed class AppScreen {
    object Home : AppScreen()
    object Meditation : AppScreen()
    object Completion : AppScreen()
    object Achievements : AppScreen()
    object SoundSettings : AppScreen()
    object Sessions : AppScreen()
    object Pet : AppScreen()
    object Quests : AppScreen()
    object UpgradeShop : AppScreen()
    object TruthPunches : AppScreen()
}

/**
 * MeditationAppState
 * 
 * Holds all the state needed for the meditation app.
 */
data class MeditationAppState(
    val timeInput: String = "",
    val timerText: String = "00:00",
    val progress: Float = 0f,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val totalMeditationTime: String = "00h 00m",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val sessionDuration: String = "00:00",
    val heartRateReduction: Int? = null,
    val newAchievement: String? = null,
    val guidedMeditationName: String? = null,
    val achievementList: List<AchievementItem> = emptyList(),
    val selectedBellId: Int = 0,
    val selectedBackgroundId: Int = 0,
    val availableBells: List<SoundOption> = emptyList(),
    val availableBackgrounds: List<SoundOption> = emptyList(),
    val userLevel: String = "Seeker",
    val karmaPoints: Int = 0,
    // Idle game state
    val totalSpiritualEgo: Long = 0L,
    val lifetimeSpiritualEgo: Long = 0L,
    val sessionSpiritualEgo: Long = 0L,
    val spiritualEgoPerSecond: Double = 0.0,
    val totalMultiplier: Double = 1.0,
    val upgradeStatuses: List<com.thirdeyetimer.app.domain.UpgradeManager.UpgradeStatus> = emptyList(),
    val sessionSpiritualEgoEarned: Long = 0L,
    val showDoubleAdButton: Boolean = true,
    // Truth Punch system state
    val allTruths: List<TruthPunchManager.TruthPunch> = emptyList(),
    val nextUnlockThreshold: Long = 0L,
    val truthProgress: Float = 0f,
    val unseenTruthCount: Int = 0,
    // Wait Wall system
    val isWaitWallActive: Boolean = false,
    val waitWallRemainingMs: Long = 0L
)

/**
 * ThirdEyeTimerApp
 * 
 * The root composable for the Third Eye Timer app with the Cosmic Zen theme.
 */
@Composable
fun ThirdEyeTimerApp(
    state: MeditationAppState,
    currentScreen: AppScreen,
    onTimeInputChange: (String) -> Unit,
    onStartClick: () -> Unit,
    onPauseResumeClick: () -> Unit,
    onStopClick: () -> Unit,
    onSoundSettingsClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onPetClick: () -> Unit,
    onFeedPetClick: () -> Unit,
    onQuestsClick: () -> Unit,
    onWatchAdForKarma: () -> Unit,
    onBrowseSessionsClick: () -> Unit,
    onMeditationSelected: (Int) -> Unit,
    onStartAnotherClick: () -> Unit,
    onShareClick: () -> Unit,
    onDismiss: () -> Unit,
    onBellSelected: (Int) -> Unit = {},
    onBackgroundSelected: (Int) -> Unit = {},
    // Upgrade shop callbacks
    onUpgradeShopClick: () -> Unit = {},
    onPurchaseUpgrade: (com.thirdeyetimer.app.domain.UpgradeManager.Upgrade) -> Unit = {},
    onWatchAdForDoubleSpiritualEgo: () -> Unit = {},
    // Truth Punch callbacks
    onTruthPunchesClick: () -> Unit = {},
    onTruthClick: (TruthPunchManager.TruthPunch) -> Unit = {},
    getTierName: (Int) -> String = { "" },
    getTierSubtitle: (Int) -> String = { "" },
    onWatchAdToBypassWaitWall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    CosmicZenTheme {
        when (currentScreen) {
            is AppScreen.Home -> {
                HomeScreen(
                    timeInput = state.timeInput,
                    onTimeInputChange = onTimeInputChange,
                    totalMeditationTime = state.totalMeditationTime,
                    currentStreak = state.currentStreak,
                    onStartClick = onStartClick,
                    onSoundSettingsClick = onSoundSettingsClick,
                    onAchievementsClick = onAchievementsClick,
                    onPetClick = onPetClick,
                    onQuestsClick = onQuestsClick,
                    onUpgradeShopClick = onUpgradeShopClick,
                    onTruthsClick = onTruthPunchesClick,
                    onBrowseSessionsClick = onBrowseSessionsClick,
                    unseenTruthCount = state.unseenTruthCount,
                    isTimerRunning = state.isRunning,
                    timerText = state.timerText,
                    progress = state.progress,
                    userLevel = state.userLevel,
                    karmaPoints = state.karmaPoints,
                    totalSpiritualEgo = state.totalSpiritualEgo,
                    lifetimeSpiritualEgo = state.lifetimeSpiritualEgo,
                    allTruths = state.allTruths,
                    isWaitWallActive = state.isWaitWallActive,
                    waitWallRemainingMs = state.waitWallRemainingMs,
                    onWatchAdToBypassWaitWall = onWatchAdToBypassWaitWall,
                    modifier = modifier
                )
            }
            
            is AppScreen.Meditation -> {
                MeditationScreen(
                    timerText = state.timerText,
                    progress = state.progress,
                    isRunning = state.isRunning,
                    isPaused = state.isPaused,
                    guidedMeditationName = state.guidedMeditationName,
                    sessionSpiritualEgo = state.sessionSpiritualEgo,
                    spiritualEgoPerSecond = state.spiritualEgoPerSecond,
                    onPauseResumeClick = onPauseResumeClick,
                    onStopClick = onStopClick,
                    lifetimeSpiritualEgo = state.totalSpiritualEgo,
                    modifier = modifier
                )
            }
            
            is AppScreen.Completion -> {
                CompletionScreen(
                    sessionDuration = state.sessionDuration,
                    currentStreak = state.currentStreak,
                    totalTime = state.totalMeditationTime,
                    heartRateReduction = state.heartRateReduction,
                    newAchievement = state.newAchievement,
                    spiritualEgoEarned = state.sessionSpiritualEgoEarned,
                    showDoubleAdButton = state.showDoubleAdButton,
                    onStartAnotherClick = onStartAnotherClick,
                    onShareClick = onShareClick,
                    onWatchAdForDoubleSpiritualEgo = onWatchAdForDoubleSpiritualEgo,
                    onDismiss = onDismiss,
                    modifier = modifier
                )
            }
            
            is AppScreen.Achievements -> {
                AchievementsScreen(
                    currentStreak = state.currentStreak,
                    longestStreak = state.longestStreak,
                    totalTime = state.totalMeditationTime,
                    achievements = state.achievementList,
                    onBackClick = onDismiss,
                    modifier = modifier
                )
            }
            
            is AppScreen.SoundSettings -> {
                SoundSettingsScreen(
                    selectedBellId = state.selectedBellId,
                    selectedBackgroundId = state.selectedBackgroundId,
                    bells = state.availableBells,
                    backgrounds = state.availableBackgrounds,
                    onBellSelected = onBellSelected,
                    onBackgroundSelected = onBackgroundSelected,
                    onBackClick = onDismiss,
                    modifier = modifier
                )
            }

            is AppScreen.Sessions -> {
                SessionsScreen(
                    onBackClick = onDismiss,
                    onMeditationSelected = onMeditationSelected,
                    modifier = modifier
                )
            }
            
            is AppScreen.Pet -> {
                PetScreen(
                    onBackClick = onDismiss,
                    onFeedClick = onFeedPetClick
                )
            }
            
            is AppScreen.Quests -> {
                com.thirdeyetimer.app.ui.screens.QuestBoardScreen(
                    onBackClick = onDismiss,
                    onWatchAdForKarma = onWatchAdForKarma
                )
            }
            
            is AppScreen.UpgradeShop -> {
                UpgradeShopScreen(
                    totalSpiritualEgo = state.totalSpiritualEgo,
                    karmaBalance = state.karmaPoints,
                    upgradeStatuses = state.upgradeStatuses,
                    totalMultiplier = state.totalMultiplier,
                    onPurchaseUpgrade = onPurchaseUpgrade,
                    onWatchAdForKarma = onWatchAdForKarma,
                    onBackClick = onDismiss
                )
            }
            
            is AppScreen.TruthPunches -> {
                TruthPunchScreen(
                    truths = state.allTruths,
                    currentSpiritualEgo = state.totalSpiritualEgo,
                    nextUnlockThreshold = state.nextUnlockThreshold,
                    overallProgress = state.truthProgress,
                    onBackClick = onDismiss,
                    onTruthClick = onTruthClick,
                    getTierName = getTierName,
                    getTierSubtitle = getTierSubtitle
                )
            }
        }
    }
}

/**
 * Preview-friendly version with internal state management
 */
@Composable
fun ThirdEyeTimerAppPreview() {
    var appState by remember { mutableStateOf(MeditationAppState()) }
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
    
    ThirdEyeTimerApp(
        state = appState,
        currentScreen = currentScreen,
        onTimeInputChange = { appState = appState.copy(timeInput = it) },
        onStartClick = { 
            appState = appState.copy(isRunning = true)
            currentScreen = AppScreen.Meditation
        },
        onPauseResumeClick = { 
            appState = appState.copy(isPaused = !appState.isPaused)
        },
        onStopClick = {
            appState = appState.copy(
                isRunning = false,
                sessionDuration = "15:00"
            )
            currentScreen = AppScreen.Completion
        },
        onSoundSettingsClick = { },
        onAchievementsClick = { },
        onPetClick = { },
        onFeedPetClick = { },
        onQuestsClick = { },
        onWatchAdForKarma = { },
        onBrowseSessionsClick = { },
        onMeditationSelected = { },
        onStartAnotherClick = {
            appState = appState.copy(
                isRunning = false,
                progress = 0f,
                timerText = "00:00"
            )
            currentScreen = AppScreen.Home
        },
        onShareClick = { },
        onDismiss = { currentScreen = AppScreen.Home }
    )
}
