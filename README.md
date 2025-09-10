# Third Eye Timer - Meditation Timer App

A beautifully designed meditation timer app featuring a serene Shiva-inspired interface with heart rate monitoring, customizable sounds, and achievement tracking.

## ��� Project Overview

**Third Eye Timer** is an Android meditation application that provides users with a comprehensive meditation experience including customizable timers, heart rate monitoring, ambient sounds, and achievement tracking.

## ⚠️ CRITICAL INFORMATION FOR FUTURE AGENTS

### Repository Structure
- **Current State**: Clean repository with source code only (107.70 KiB)
- **Missing Files**: Large media files (PNG, WAV, MP3) and keystore
- **Reason**: Original repository was 40.24 MiB, causing GitHub push failures

### What Was Fixed
1. **Timer Pause/Resume**: Critical bug where timer restarted from beginning
2. **UI Layout**: Buttons overlapping with ad banners
3. **16KB Memory Support**: Required for Android 15+ compatibility — see `docs/16kb-page-support.md` for the root cause, fix, and verification steps.
4. **GitHub Repository**: Successfully pushed clean version

### Documentation
- 16 KB page sizes: `docs/16kb-page-support.md`

### Key Implementation Details
- **Timer Logic**: Uses `remainingTimeMillis` and `wasPaused` variables
- **UI Design**: Responsive layout with `0dp` width buttons
- **Ad Integration**: Dual banner ads (top and bottom)
- **Build Config**: Target SDK 36, Min SDK 21

## ���️ Development Setup

```bash
# Clone the repository
git clone https://github.com/hariganeshs/third-eye-timer.git
cd third-eye-timer

# Build commands
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew bundleRelease
```

## ��� What NOT to Do

1. **Don't add large media files directly to Git** - Repository will break
2. **Don't change timer pause/resume logic without testing**
3. **Don't remove 16KB memory page support configuration**

## ✅ What TO Do

1. **Always test timer pause/resume functionality**
2. **Test UI on multiple screen sizes**
3. **Increment version numbers for releases**
4. **Use clean repository approach for Git operations**

---

**Repository**: https://github.com/hariganeshs/third-eye-timer  
**Status**: ✅ Successfully deployed to GitHub  
**Last Updated**: January 2025
