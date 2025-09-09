# Agent Documentation - Third Eye Timer Project

**⚠️ CRITICAL INFORMATION FOR FUTURE AI AGENTS**

This document contains essential technical details, troubleshooting steps, and implementation notes that future agents MUST read before making any changes to this project.

## 🚨 Critical Issues & Solutions

### 1. GitHub Repository Size Problem (RESOLVED)

**Problem**: Original repository was 40.24 MiB, causing HTTP 408 errors during push
**Root Cause**: Large media files (PNG, WAV, MP3) and keystore files
**Solution Applied**: Created clean repository with source code only (107.70 KiB)

**DO NOT ATTEMPT TO ADD LARGE FILES DIRECTLY** - This will break the repository again.

### 2. Missing Media Files

The following files are intentionally NOT in the repository:
```
app/src/main/res/drawable/ic_trishul.png      # 48x48 PNG (app icon)
app/src/main/res/drawable/shiva_bg.png        # Background image
app/src/main/res/raw/*.wav                    # 6 bell sounds + chants
app/src/main/res/raw/*.mp3                    # Background music
thirdeyetimer-release-key.keystore            # Release signing key
```

**If you need these files**: Use Git LFS or add them manually to the device during development.

## 🔧 Technical Implementation Details

### Timer Pause/Resume Logic (CRITICAL)

**Location**: `MainActivity.kt` lines 310-350
**Key Variables**:
```kotlin
private var remainingTimeMillis = 0L
private var wasPaused = false
```

**Implementation**:
```kotlin
if (isRunning) {
    // Pausing - stop the timer
    stopTimerService()
    wasPaused = true
    startPauseButton.text = "Start"
    resetButton.visibility = View.VISIBLE
} else {
    // If resuming from pause, use current remaining time; otherwise use input
    if (!wasPaused || timeInMilliSeconds <= 0) {
        timeInMilliSeconds = timeInput.toLong() * 60000L
        // Reset heart rate measurement state for new session
    }
    // Start/resume timer with current remaining time
}
```

**⚠️ IMPORTANT**: The timer MUST continue from where it left off, not restart from the beginning. This was a critical bug that was fixed.

### UI Layout Constraints (RESPONSIVE DESIGN)

**Key Changes Made**:
- **Buttons**: Changed from fixed width to `0dp` with proper constraints
- **Spacing**: Reduced margins (60dp → 32dp, 48dp → 24dp)
- **Padding**: Reduced from 24dp to 16dp
- **Ad Placement**: Ensured buttons don't overlap with ad banners

**Layout Structure**:
```xml
<!-- Start/Pause Button -->
<Button
    android:layout_width="0dp"
    android:layout_marginStart="32dp"
    android:layout_marginEnd="32dp"
    app:layout_constraintBottom_toTopOf="@id/button_reset" />

<!-- Reset Timer Button -->
<Button
    android:layout_width="0dp"
    android:layout_marginStart="48dp"
    android:layout_marginEnd="48dp"
    app:layout_constraintBottom_toTopOf="@id/adView" />
```

### 16KB Memory Page Support (ANDROID 15+)

**Location**: `app/build.gradle.kts` lines 50-55
**Configuration**:
```kotlin
packaging {
    jniLibs {
        useLegacyPackaging = false
    }
}
```

**Purpose**: Required for Android 15+ compatibility and Google Play Store compliance.

## 🚀 Build & Deployment Process

### Version Management

**Current Version**: 1.0.5 (version code 6)
**Location**: `app/build.gradle.kts` lines 16-17

**ALWAYS INCREMENT**:
```kotlin
versionCode = 6  // Increment this
versionName = "1.0.5"  // Increment this
```

### Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease

# Release AAB (for Play Store)
./gradlew bundleRelease
```

### Signing Configuration

**Keystore**: `thirdeyetimer-release-key.keystore` (NOT in repository)
**Password**: `thirdeyetimer123`
**Key Alias**: `thirdeyetimer-key`

**⚠️ WARNING**: Never commit the keystore file to Git. It's excluded by `.gitignore`.

## 🔍 Troubleshooting Guide

### Build Failures

1. **JDK Version**: Ensure JDK 11+ is installed
2. **Android SDK**: Target SDK 36 required
3. **Gradle Sync**: Always sync after changing build files

### GitHub Push Issues

**If you encounter HTTP 408 errors**:
1. **DON'T** try to add large files directly
2. **DO** use the clean repository approach
3. **Alternative**: Use Git LFS for large files

**Git LFS Setup** (if needed):
```bash
git lfs install
git lfs track "*.png" "*.wav" "*.mp3"
git add .gitattributes
git commit -m "Add Git LFS tracking"
```

### AdMob Issues

**Ad Unit IDs** (verify these are correct):
- Top Banner: `ca-app-pub-2722920301958819/3959238290`
- Bottom Banner: `ca-app-pub-2722920301958819/2481160193`
- Interstitial: `ca-app-pub-2722920301958819/7531366385`

**Common Problems**:
- Ads not showing: Check network and AdMob account status
- Test ads: Use test device IDs during development

## 📱 Testing Requirements

### Critical Test Cases

1. **Timer Pause/Resume**:
   - Start timer (e.g., 5 minutes)
   - Pause at 2:30 remaining
   - Resume → should continue from 2:30, not restart from 5:00

2. **UI Responsiveness**:
   - Test on different screen sizes
   - Verify buttons don't overlap with ads
   - Check all elements are visible and accessible

3. **Heart Rate Measurement**:
   - Camera permission handling
   - Start and end measurements
   - Relaxation summary display

### Device Testing

**Minimum Requirements**:
- Android 5.0+ (API 21)
- Camera access
- Internet connection for ads

**Recommended Testing**:
- Multiple screen densities
- Different Android versions
- Various device manufacturers

## 🚫 What NOT to Do

1. **❌ Don't add large media files directly to Git**
2. **❌ Don't commit the keystore file**
3. **❌ Don't change the timer pause/resume logic without testing**
4. **❌ Don't modify UI constraints without testing on multiple screen sizes**
5. **❌ Don't remove the 16KB memory page support configuration**

## ✅ What TO Do

1. **✅ Always test timer pause/resume functionality**
2. **✅ Test UI on multiple screen sizes**
3. **✅ Increment version numbers for releases**
4. **✅ Use clean repository approach for Git operations**
5. **✅ Document any changes to critical functionality**

## 🔗 Key Files & Locations

**Critical Implementation Files**:
- `MainActivity.kt` - Timer logic, UI handling
- `MeditationTimerService.kt` - Background timer service
- `activity_main.xml` - Main UI layout
- `build.gradle.kts` - Build configuration

**Configuration Files**:
- `.gitignore` - Git ignore rules
- `gradle.properties` - Gradle configuration
- `AndroidManifest.xml` - App permissions and components

**Documentation Files**:
- `README.md` - Project overview
- `AD_UNITS_GUIDE.md` - AdMob configuration
- `PUBLISHING_GUIDE.md` - Play Store deployment

## 📞 Emergency Contacts

**If you encounter critical issues**:
1. **Check this documentation first**
2. **Review the troubleshooting section**
3. **Test the specific functionality mentioned**
4. **Document the issue and solution for future agents**

---

**Document Version**: 1.0  
**Last Updated**: January 2025  
**Maintained By**: AI Assistant  
**Next Review**: After any major changes to critical functionality



