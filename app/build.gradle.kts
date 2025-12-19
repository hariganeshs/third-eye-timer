plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.thirdeyetimer.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.thirdeyetimer.app"
        minSdk = 21
        targetSdk = 36
        versionCode = 9
        versionName = "1.0.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Note: No local native build is configured in this project, so externalNativeBuild args are unnecessary.
    }

    signingConfigs {
        create("release") {
            storeFile = file("../thirdeyetimer-release-key.keystore")
            storePassword = "thirdeyetimer123"
            keyAlias = "thirdeyetimer-key"
            keyPassword = "thirdeyetimer123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    
    // Enable 16 KB page size support (required for Android 15+)
    packaging {
        jniLibs {
            useLegacyPackaging = false
            // Keep all native libraries without compression for better alignment
            keepDebugSymbols += "**/*.so"
        }
        // Additional packaging options to handle alignment
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    
    // Disable ABI splits when building App Bundles to avoid multiple APK outputs
    // Play will handle ABI/device-specific distribution from the AAB
    splits {
        abi {
            isEnable = false
            reset()
            include("arm64-v8a", "x86_64")
            isUniversalApk = false
        }
    }
    
}


// Removed forced versions to allow pulling latest 16KB-aligned native artifacts.

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.9.2")
    
    // Compose dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended")
    
    // Media3 (ExoPlayer) - Audio Playback
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-common:1.2.0")
    implementation("androidx.media3:media3-session:1.2.0")
    
    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // CameraX - Updated to latest versions for 16 KB compatibility
    implementation("androidx.camera:camera-core:1.4.0")
    implementation("androidx.camera:camera-camera2:1.4.0")
    implementation("androidx.camera:camera-lifecycle:1.4.0")
    implementation("androidx.camera:camera-view:1.4.0")
    implementation("com.google.guava:guava:31.1-android")
    
    // Google Play Services Ads
    implementation("com.google.android.gms:play-services-ads:22.6.0")
    
    // Apache Commons Math for signal processing
    implementation("org.apache.commons:commons-math3:3.6.1")
    
    // Signal processing - using built-in Kotlin math functions
    // Removed external libraries to avoid dependency conflicts
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}