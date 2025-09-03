# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep CameraX classes
-keep class androidx.camera.** { *; }
-keep class com.google.common.util.concurrent.** { *; }

# Keep AdMob classes
-keep class com.google.android.gms.ads.** { *; }

# Keep MediaPlayer and audio classes
-keep class android.media.** { *; }

# Keep our main activities and services
-keep class com.thirdeyetimer.app.MainActivity { *; }
-keep class com.thirdeyetimer.app.SimpleHeartRateActivity { *; }
-keep class com.thirdeyetimer.app.RelaxationSummaryActivity { *; }
-keep class com.thirdeyetimer.app.MeditationTimerService { *; }
-keep class com.thirdeyetimer.app.PPGProcessor { *; }

# Keep data classes
-keepclassmembers class com.thirdeyetimer.app.PPGResult {
    public *;
}
-keepclassmembers class com.thirdeyetimer.app.SignalStats {
    public *;
}

# Keep BroadcastReceiver
-keep class * extends android.content.BroadcastReceiver { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep R classes
-keep class **.R$* {
    public static <fields>;
}

# Keep resource names
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep View constructors
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# Keep onClick methods
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Compose
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }