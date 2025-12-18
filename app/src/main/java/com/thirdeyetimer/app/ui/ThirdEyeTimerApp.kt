package com.thirdeyetimer.app.ui

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.thirdeyetimer.app.ui.screens.*
import com.thirdeyetimer.app.ui.theme.CosmicZenTheme

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
    val availableBackgrounds: List<SoundOption> = emptyList()
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
    onBrowseSessionsClick: () -> Unit,
    onStartAnotherClick: () -> Unit,
    onShareClick: () -> Unit,
    onDismiss: () -> Unit,
    onBellSelected: (Int) -> Unit = {},
    onBackgroundSelected: (Int) -> Unit = {},
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
                    onBrowseSessionsClick = onBrowseSessionsClick,
                    isTimerRunning = state.isRunning,
                    timerText = state.timerText,
                    progress = state.progress,
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
                    onPauseResumeClick = onPauseResumeClick,
                    onStopClick = onStopClick,
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
                    onStartAnotherClick = onStartAnotherClick,
                    onShareClick = onShareClick,
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
        onBrowseSessionsClick = { },
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
