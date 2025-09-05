# Quick Reference - Third Eye Timer

**For Future AI Agents - Read This First!**

## CRITICAL WARNINGS

1. NEVER add large media files directly to Git - Repository will break (was 40.24 MiB, now 107.70 KiB)
2. Timer pause/resume MUST work correctly - Critical bug was fixed
3. 16KB memory page support is REQUIRED - Dont remove this configuration

## Key Information

| Item | Value | Location |
|------|-------|----------|
| Current Version | 1.0.5 (code 6) | app/build.gradle.kts:16-17 |
| Target SDK | 36 (Android 15) | app/build.gradle.kts:11 |
| Min SDK | 21 (Android 5.0) | app/build.gradle.kts:12 |
| Repository Size | 107.70 KiB (clean) | Successfully pushed to GitHub |
| GitHub URL | https://github.com/hariganeshs/third-eye-timer | Working |

## Missing Files (Intentionally Excluded)

- ic_trishul.png - App icon
- shiva_bg.png - Background image  
- *.wav - Bell sounds (6 files)
- *.mp3 - Background music (5 files)
- thirdeyetimer-release-key.keystore - Signing key

## What's Working

- Timer pause/resume functionality
- Responsive UI design
- 16KB memory page support
- AdMob integration
- Heart rate measurement
- Achievement system
- GitHub repository

## Essential Commands

# Build
./gradlew assembleDebug
./gradlew assembleRelease  
./gradlew bundleRelease

# Git (if you need to add large files)
git lfs install
git lfs track "*.png" "*.wav" "*.mp3"

## Test These Before Making Changes

1. Timer: Start → Pause → Resume (should continue, not restart)
2. UI: Check buttons dont overlap with ads
3. Build: Verify app compiles successfully

## Read These Files First

1. AGENT_DOCUMENTATION.md - Technical details
2. README.md - Project overview  
3. This file - Quick reference

---

Remember: This project had major issues that were resolved. Dont repeat the same mistakes!
