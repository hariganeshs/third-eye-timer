# Android 16 KB Page Size Compatibility Fix Guide

## Overview
Starting **November 1st, 2025**, all new apps and updates to existing apps submitted to Google Play targeting Android 15+ devices must support 16 KB page sizes. This guide provides a comprehensive solution for fixing compatibility issues.

## Error Symptoms
- APK build warnings: `"APK app-debug.apk is not compatible with 16 KB devices"`
- Native library alignment errors: `"Some libraries have LOAD segments not aligned at 16 KB boundaries"`
- Libraries commonly affected: `libimage_processing_util_jni.so`, `libandroidx.graphics.path.so`

## Root Cause Analysis
The issue typically occurs when:
1. Native libraries (.so files) from dependencies are not aligned to 16 KB boundaries
2. Android Gradle Plugin (AGP) version is below 8.5.1
3. NDK configuration doesn't specify proper alignment settings
4. Third-party dependencies (CameraX, image processing libraries) contain unaligned native code

## Solution Implementation

### Step 1: Verify Current Configuration
```bash
# Check current AGP version in gradle/libs.versions.toml
agp = "8.12.2"  # Should be 8.5.1 or higher

# Verify zipalign tool availability
& "C:\Users\[USER]\AppData\Local\Android\Sdk\build-tools\36.0.0\zipalign.exe" -c -P 16 -v 4 "app\build\outputs\apk\debug\app-debug.apk"
```

### Step 2: Update build.gradle.kts Configuration

#### app/build.gradle.kts
```kotlin
android {
    namespace = "com.your.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.your.app"
        minSdk = 21
        targetSdk = 36
        versionCode = 7
        versionName = "1.0.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Add NDK configuration for 16 KB page size support
        ndk {
            // Ensure we're building for the right architectures
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    // Enable 16 KB page size support (required for Android 15+)
    packaging {
        jniLibs {
            useLegacyPackaging = false
            // Ensure all native libraries are aligned to 16 KB boundaries
            pickFirsts += "lib/arm64-v8a/*.so"
            pickFirsts += "lib/x86_64/*.so"
        }
        // Additional packaging options to handle alignment
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
```

### Step 3: Update gradle.properties
```properties
# Enable 16 KB page size support (required for Android 15+)
# Note: android.bundle.enableUncompressedNativeLibs is deprecated in AGP 8.1+
# 16 KB alignment is handled by AGP 8.5.1+ automatically
android.useAndroidX=true
android.nonTransitiveRClass=true
```

### Step 4: Update Dependencies (Critical for CameraX)
```kotlin
dependencies {
    // CameraX - Updated to latest versions for 16 KB compatibility
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
    
    // Other dependencies...
}
```

### Step 5: Build and Verify
```bash
# Clean and rebuild
.\gradlew clean
.\gradlew assembleDebug

# Verify 16 KB alignment
& "C:\Users\[USER]\AppData\Local\Android\Sdk\build-tools\36.0.0\zipalign.exe" -c -P 16 -v 4 "app\build\outputs\apk\debug\app-debug.apk"
```

## Verification Success Indicators
- Build completes without 16 KB warnings
- zipalign command returns "Verification successful"
- Native libraries show "(OK)" status in zipalign output
- Libraries appear aligned at 16 KB boundaries (addresses ending in 0000, 4000, 8000, C000)

## Common Issues and Solutions

### Issue 1: Deprecated Property Error
**Error:** `The option 'android.bundle.enableUncompressedNativeLibs' is deprecated`
**Solution:** Remove deprecated properties from gradle.properties - AGP 8.5.1+ handles this automatically

### Issue 2: Third-party Library Conflicts
**Error:** Multiple .so files causing conflicts
**Solution:** Use `pickFirsts` configuration in packaging block to resolve conflicts

### Issue 3: PowerShell Command Execution
**Error:** PowerShell syntax issues with zipalign command
**Solution:** Use proper PowerShell syntax with `&` operator:
```powershell
& "path\to\zipalign.exe" -c -P 16 -v 4 "app.apk"
```

## AGP Version Requirements
| AGP Version | 16 KB Support | Notes |
|-------------|---------------|--------|
| < 8.5.1     | Manual config required | Need explicit settings |
| 8.5.1+      | Automatic | Handles alignment automatically |
| 8.12.2      | Full support | Recommended version |

## Testing Recommendations
1. **Build both debug and release APKs** and verify alignment
2. **Test on Android 15 emulator** with 16 KB page size enabled
3. **Use zipalign verification** as final confirmation
4. **Check Google Play Console** upload compatibility

## Dependencies Most Likely to Cause Issues
- **CameraX libraries** (versions < 1.3.4)
- **Google Play Services** (ensure latest versions)
- **Image processing libraries**
- **Custom NDK/JNI code**
- **Third-party SDKs** with native components

## Future Prevention
1. Always use AGP 8.5.1 or higher for new projects
2. Keep CameraX and other native-dependent libraries updated
3. Include zipalign verification in CI/CD pipelines
4. Test on 16 KB page size emulators during development

## Command Reference

### Find Android SDK Build Tools
```powershell
Get-ChildItem "C:\Users\[USER]\AppData\Local\Android\Sdk\build-tools" | Sort-Object Name -Descending | Select-Object -First 1
```

### Zipalign Verification (Windows PowerShell)
```powershell
& "C:\Users\[USER]\AppData\Local\Android\Sdk\build-tools\36.0.0\zipalign.exe" -c -P 16 -v 4 "app\build\outputs\apk\debug\app-debug.apk"
```

### Gradle Commands
```bash
# Clean build
.\gradlew clean

# Build debug APK
.\gradlew assembleDebug

# Build release APK  
.\gradlew assembleRelease
```

## Google Play Requirements Timeline
- **November 1st, 2025**: Mandatory for all new apps and updates targeting Android 15+
- **Current**: Optional but recommended
- **Testing**: Available now with Android 15 emulators

---
*Last Updated: January 2025*
*Tested with: AGP 8.12.2, Android Studio Ladybug 2024.2.1, NDK r27+*
