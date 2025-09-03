@echo off
echo 🚀 Building Meditation Timer App Release APK...

REM Try to find Android Studio Java installation
if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
    set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
    echo Found Android Studio Java at: %JAVA_HOME%
) else if exist "C:\Program Files\Android\Android Studio\jre\bin\java.exe" (
    set JAVA_HOME=C:\Program Files\Android\Android Studio\jre
    echo Found Android Studio Java at: %JAVA_HOME%
) else if exist "%LOCALAPPDATA%\Android\Sdk\jbr\bin\java.exe" (
    set JAVA_HOME=%LOCALAPPDATA%\Android\Sdk\jbr
    echo Found Android SDK Java at: %JAVA_HOME%
) else (
    echo ❌ Java not found! Please install Android Studio or set JAVA_HOME manually
    echo.
    echo To set JAVA_HOME manually, run:
    echo set JAVA_HOME=C:\path\to\your\java\installation
    pause
    exit /b 1
)

REM Add Java to PATH
set PATH=%JAVA_HOME%\bin;%PATH%

echo.
echo ✅ Java environment set up successfully
echo Java version:
java -version

echo.
echo 🔨 Building release APK...
call gradlew.bat assembleRelease

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Build successful!
    echo 📱 APK location: app\build\outputs\apk\release\app-release.apk
    echo.
    echo 🚀 Next steps:
    echo 1. Test the APK on your device
    echo 2. Create app store assets (icon, screenshots, etc.)
    echo 3. Set up Google Play Console account
    echo 4. Upload APK and submit for review
    echo.
    echo 📋 See PUBLISHING_CHECKLIST.md for detailed instructions
) else (
    echo.
    echo ❌ Build failed! Check the error messages above.
)

pause 