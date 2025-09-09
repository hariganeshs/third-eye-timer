# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

**Third Eye Timer** is an Android meditation app built in Kotlin featuring timer functionality, heart rate monitoring via camera, ambient sounds, achievement tracking, and AdMob integration. The app has a Shiva-inspired UI design and provides a comprehensive meditation experience.

## 🚨 Critical Repository Context

### Repository Size Management
- **Current State**: Clean repository (107.70 KiB) with source code only
- **Missing Files**: Large media files (PNG, WAV, MP3) and keystore intentionally excluded
- **Reason**: Original repository was 40.24 MiB causing GitHub push failures (HTTP 408 errors)
- **WARNING**: Never add large media files directly to Git - will break repository again

### Critical Bug Fixes Applied
1. **Timer Pause/Resume**: Fixed critical bug where timer restarted from beginning instead of continuing
2. **UI Layout**: Resolved button overlapping with ad banners through responsive design
3. **16KB Memory Support**: Enabled for Android 15+ compatibility (required for Play Store)

## Development Commands

### Build Commands
```bash
# Debug build for development
./gradlew assembleDebug

# Release APK for distribution  
./gradlew assembleRelease

# Release AAB bundle for Play Store
./gradlew bundleRelease

# Clean build artifacts
./gradlew clean
```

### Platform-Specific Build Scripts
```bash
# Windows
./build_release.bat

# Linux/Mac
./build_release.sh --install  # Optional --install flag for device deployment
```

### Testing Commands
```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Lint check
./gradlew lint
```

## Architecture Overview

### Core Components
- **MainActivity.kt**: Main meditation timer interface with pause/resume logic, heart rate integration, and AdMob ads
- **MeditationTimerService**: Background service for timer functionality with foreground notifications
- **SimpleHeartRateActivity**: Camera-based PPG heart rate measurement using flashlight
- **PPGProcessor**: Signal processing for heart rate calculation from camera data
- **RelaxationSummaryActivity**: Post-meditation summary with heart rate variability analysis
- **AchievementPopupManager**: Gamification system for meditation streaks and milestones

### Key Implementation Details

#### Timer State Management (Critical)
```kotlin
// Key variables in MainActivity.kt
private var remainingTimeMillis = 0L  // Current remaining time
private var wasPaused = false         // Tracks if timer was paused
private var isRunning = false         // Current timer state
```

The timer pause/resume logic MUST maintain remaining time - this was a critical bug that was fixed.

#### Responsive UI System
The app implements a comprehensive responsive layout system:
- **Base Layout**: `layout/activity_main.xml` (portrait)
- **Landscape**: `layout-land/activity_main.xml` (three-column)  
- **Small Screens**: `layout-sw280dp/activity_main.xml` (with ScrollView)
- **Dimension Resources**: Multiple `values-*dp/dimens.xml` files for different screen sizes

#### Heart Rate Monitoring
Uses camera flashlight and front camera to measure PPG (photoplethysmography):
- Measures at meditation start and end for relaxation analysis
- Calculates heart rate variability (HRV) as wellness metric
- Integrates with timer workflow via Activity Result API

### Build Configuration

#### Android Targets
- **compileSdk**: 36 (Android 15)
- **targetSdk**: 36 (Android 15)  
- **minSdk**: 21 (Android 5.0)
- **Current Version**: 1.0.6 (versionCode 7)

#### Critical Build Settings
```kotlin
// Required for Android 15+ compatibility
packaging {
    jniLibs {
        useLegacyPackaging = false  // Enables 16KB page size support
    }
}
```

#### AdMob Integration
- **App ID**: `ca-app-pub-2722920301958819~5438496026`
- **Top Banner**: `ca-app-pub-2722920301958819/3959238290`
- **Bottom Banner**: `ca-app-pub-2722920301958819/2481160193`
- **Interstitial**: `ca-app-pub-2722920301958819/7531366385`

## Development Guidelines

### Essential Testing Requirements
1. **Timer Pause/Resume**: Start timer → pause → resume → verify continues from pause point (not restart)
2. **UI Responsiveness**: Test on different screen sizes to ensure buttons don't overlap with ads
3. **Heart Rate Flow**: Test camera permissions and measurement accuracy
4. **Build Verification**: Ensure app compiles and runs successfully after changes

### Code Patterns
- Uses traditional Android Views (not Compose) with constraint layouts
- Implements foreground service for background timer functionality
- Uses SharedPreferences for meditation statistics and achievements
- Follows reactive patterns with BroadcastReceiver for service communication

### Critical Files to Review Before Changes
1. `MainActivity.kt` - Core timer logic and UI state management
2. `app/build.gradle.kts` - Build configuration and dependencies  
3. `AndroidManifest.xml` - Service declarations and permissions
4. Layout files in `res/layout*/` - Responsive UI system

### Version Management
Always increment both values in `app/build.gradle.kts` before releases:
```kotlin
versionCode = 7        // Integer - increment for each release
versionName = "1.0.6"  // String - semantic version
```

### Missing Media Files
The following files are intentionally excluded (use Git LFS if needed):
- App icons: `ic_trishul.png`, `shiva_bg.png`  
- Audio files: `*.wav` bell sounds, `*.mp3` background music
- Keystore: `thirdeyetimer-release-key.keystore`

## Repository Status
- **GitHub URL**: https://github.com/hariganeshs/third-eye-timer
- **Status**: ✅ Successfully deployed
- **Last Major Update**: January 2025 (fixed critical bugs and responsive UI)
