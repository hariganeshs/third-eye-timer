#!/bin/bash

# Meditation Timer App - Release Build Script
# This script builds a release APK for Google Play Store

echo "🧘 Building Meditation Timer App Release..."

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Build release APK
echo "🔨 Building release APK..."
./gradlew assembleRelease

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo "📱 APK location: app/build/outputs/apk/release/app-release.apk"
    echo "📊 APK size: $(du -h app/build/outputs/apk/release/app-release.apk | cut -f1)"
    
    # Optional: Install on connected device
    if [ "$1" = "--install" ]; then
        echo "📲 Installing on connected device..."
        adb install -r app/build/outputs/apk/release/app-release.apk
    fi
else
    echo "❌ Build failed!"
    exit 1
fi

echo "🎉 Release build complete!"
echo ""
echo "📋 Next steps:"
echo "1. Test the APK on a real device"
echo "2. Create app store assets (screenshots, descriptions)"
echo "3. Upload to Google Play Console"
echo "4. Submit for review" 