package com.thirdeyetimer.app

import android.util.Log
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdError
import java.util.Locale
import android.media.AudioManager
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.util.Calendar
import android.app.Dialog
import android.view.ViewGroup
import android.view.Gravity
import android.widget.LinearLayout
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.SwitchCompat
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {
    private var isRunning = false
    private var timeInMilliSeconds = 0L
    private var wasPaused = false
    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var timeEditText: EditText
    private lateinit var timerText: TextView
    private lateinit var startPauseButton: Button
    private lateinit var resetButton: Button
    private lateinit var adView: AdView
    private lateinit var topAdView: AdView
    private var interstitialAd: InterstitialAd? = null
    private lateinit var totalTimeText: TextView
    private lateinit var appTitleText: TextView
    private lateinit var backgroundImageView: ImageView
    private var scrollViewForBackground: android.widget.ScrollView? = null

    // Background slideshow variables
    private val backgroundImages = arrayOf(
        R.drawable.shiva_bg,
        R.drawable.shiva_bg_1,
        R.drawable.shiva_bg_2,
        R.drawable.shiva_bg_3,
        R.drawable.trident_bg,
        R.drawable.trident_bg_1,
        R.drawable.trident_bg_2,
        R.drawable.trident_bg_3,
        R.drawable.trident_bg_4
    )
    private var currentBackgroundIndex = 0
    private val backgroundChangeInterval = 10000L // 10 seconds
    private lateinit var backgroundHandler: Handler
    private lateinit var backgroundRunnable: Runnable

    private val PREFS_NAME = "meditation_prefs"
    private val KEY_TOTAL_TIME = "total_meditation_time"
    private var totalMeditationTimeMillis: Long = 0L
    private var sessionStartTime: Long = 0L
    private val KEY_BELL_SOUND = "bell_sound"
    private var selectedBellResId: Int = R.raw.bell
    private var previewPlayer: MediaPlayer? = null
    private var isServiceRunning = false
    private val TIMER_FINISHED_ACTION = "com.example.meditationtimerapp.TIMER_FINISHED"
    private val TIMER_TICK_ACTION = "com.example.meditationtimerapp.TIMER_TICK"
    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                TIMER_FINISHED_ACTION -> {
                    timerText.text = "Done!"
                    startPauseButton.text = "Start"
                    resetButton.visibility = View.VISIBLE
                    isRunning = false
                    // Update total time
                    val sessionEndTime = System.currentTimeMillis()
                    val sessionDuration = (sessionEndTime - sessionStartTime).coerceAtLeast(0L)
                    lastSessionDuration = sessionDuration
                    totalMeditationTimeMillis += sessionDuration
                    val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    prefs.edit().putLong(KEY_TOTAL_TIME, totalMeditationTimeMillis).apply()
                    updateTotalTimeText()
                    updateStreak() // Update streak when meditation completes
                    // Check achievements after both time and streak updates
                    val newTotalHours = (totalMeditationTimeMillis / (1000 * 60 * 60)).toInt()
                    if (newTotalHours > totalHoursMeditated) {
                        totalHoursMeditated = newTotalHours
                    }
                    checkAchievements() // Check for achievements
                    
                    // Show relaxation summary if heart rate was measured
                    if (heartRateMeasured) {
                        // Measure end heart rate
                        measureEndHeartRate()
                    }
                }
                TIMER_TICK_ACTION -> {
                    val remainingTime = intent.getLongExtra(MeditationTimerService.EXTRA_REMAINING_TIME, 0L)
                    timeInMilliSeconds = remainingTime  // Update current time with remaining time
                    updateTimerText(remainingTime)
                }
            }
        }
    }

    private var selectedBackgroundResId: Int = 0
    private var previewBackgroundPlayer: MediaPlayer? = null
    
    // Activity Result API for heart rate measurement
    private val heartRateLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { intent ->
                val measurementCompleted = intent.getBooleanExtra("measurement_completed", false)
                val isEndMeasurement = intent.getBooleanExtra("is_end_measurement", false)
                
                if (measurementCompleted) {
                    if (!heartRateMeasured && !isEndMeasurement) {
                        // First measurement (start)
                        startHeartRate = intent.getIntExtra("start_heart_rate", 0)
                        startHRV = intent.getDoubleExtra("start_hrv", 0.0)
                        heartRateMeasured = true
                        startMeditationSession()
                    } else if (heartRateMeasured && isEndMeasurement) {
                        // Second measurement (end)
                        endHeartRate = intent.getIntExtra("start_heart_rate", 0)
                        endHRV = intent.getDoubleExtra("start_hrv", 0.0)
                        showRelaxationSummary()
                    }
                } else {
                    if (!heartRateMeasured) {
                        startMeditationSession()
                    }
                }
            }
        }
    }
    
    // Categorized background sounds - only these loop
    private val ambientSoundNames = arrayOf(
        "tibetan_chant", "aum_mantra", "birds", "jungle_rain"
    )

    private val guidedSoundNames = arrayOf(
        "Acceptance Practice", "acceptance_meditation", "anapanasati",
        "body_scan_bottom_up", "body_scan_front_back", "body_scan_left_right",
        "body_scan_meditation", "body_scan_top_down", "breath_counting",
        "buddhist_1_breath_anapanasati", "buddhist_2_loving_kindness_metta",
        "buddhist_3_body_scan_four_elements", "buddhist_4_open_awareness",
        "buddhist_5_walking_gatha", "buddhist_6_compassion_tonglen",
        "buddhist_7_refuge_three_jewels", "candle_flame", "cave", "chakra_crown",
        "chakra_heart", "chakra_root", "chakra_sacral", "chakra_solar",
        "chakra_third_eye", "chakra_throat", "choiceless_awareness",
        "compassion", "compassion_meditation", "courage", "dawn", "desert",
        "dusk", "eightfold_path", "equanimity", "forest", "forest_meditation",
        "forgiveness", "four_foundations", "four_immeasurables", "garden",
        "gratitude", "gratitude_meditation", "hindu_1_mantra_presence",
        "hindu_2_prana_body_scan", "hindu_3_om_resonance", "hindu_4_nature_dhyana",
        "inner_child", "inquiry_self", "kundalini", "letting_go",
        "letting_go_meditation", "light_meditation", "loving_kindness",
        "loving_kindness_meditation", "mantra", "meadow", "metta_benefactor",
        "metta_difficult", "metta_neutral", "metta_self", "mindful_eating",
        "mindful_eating_meditation", "mindfulness_breathing", "moon", "mountain",
        "mountain_meditation", "ocean", "ocean_meditation", "open_awareness",
        "pranayama", "rain", "resilience", "river", "shrine", "silence", "sky",
        "sound_listening", "sound_meditation", "space", "space_meditation",
        "sun", "temple", "tonglen", "walking", "walking_meditation", "yantra"
    )

    private val backgroundSoundNames = ambientSoundNames + guidedSoundNames
    // Background sound resource IDs (ordered to match backgroundSoundNames)
    private val ambientResIds = arrayOf(
        R.raw.tibetan_chant, R.raw.aum_mantra, R.raw.birds, R.raw.jungle_rain
    )

    private val guidedResIds = arrayOf(
        R.raw.acceptance, R.raw.acceptance_meditation, R.raw.anapanasati,
        R.raw.body_scan_bottom_up, R.raw.body_scan_front_back,
        R.raw.body_scan_left_right, R.raw.body_scan_meditation, R.raw.body_scan_top_down,
        R.raw.breath_counting, R.raw.buddhist_1_breath_anapanasati,
        R.raw.buddhist_2_loving_kindness_metta, R.raw.buddhist_3_body_scan_four_elements,
        R.raw.buddhist_4_open_awareness, R.raw.buddhist_5_walking_gatha,
        R.raw.buddhist_6_compassion_tonglen, R.raw.buddhist_7_refuge_three_jewels,
        R.raw.candle_flame, R.raw.cave, R.raw.chakra_crown, R.raw.chakra_heart,
        R.raw.chakra_root, R.raw.chakra_sacral, R.raw.chakra_solar,
        R.raw.chakra_third_eye, R.raw.chakra_throat, R.raw.choiceless_awareness,
        R.raw.compassion, R.raw.compassion_meditation, R.raw.courage, R.raw.dawn,
        R.raw.desert, R.raw.dusk, R.raw.eightfold_path, R.raw.equanimity, R.raw.forest,
        R.raw.forest_meditation, R.raw.forgiveness, R.raw.four_foundations,
        R.raw.four_immeasurables, R.raw.garden, R.raw.gratitude,
        R.raw.gratitude_meditation, R.raw.hindu_1_mantra_presence,
        R.raw.hindu_2_prana_body_scan, R.raw.hindu_3_om_resonance,
        R.raw.hindu_4_nature_dhyana, R.raw.inner_child, R.raw.inquiry_self,
        R.raw.kundalini, R.raw.letting_go, R.raw.letting_go_meditation,
        R.raw.light_meditation, R.raw.loving_kindness, R.raw.loving_kindness_meditation,
        R.raw.mantra, R.raw.meadow, R.raw.metta_benefactor, R.raw.metta_difficult,
        R.raw.metta_neutral, R.raw.metta_self, R.raw.mindful_eating,
        R.raw.mindful_eating_meditation, R.raw.mindfulness_breathing, R.raw.moon,
        R.raw.mountain, R.raw.mountain_meditation, R.raw.ocean,
        R.raw.ocean_meditation, R.raw.open_awareness, R.raw.pranayama, R.raw.rain,
        R.raw.resilience, R.raw.river, R.raw.shrine, R.raw.silence, R.raw.sky,
        R.raw.sound_listening, R.raw.sound_meditation, R.raw.space,
        R.raw.space_meditation, R.raw.sun, R.raw.temple, R.raw.tonglen,
        R.raw.walking, R.raw.walking_meditation, R.raw.yantra
    )

    private val backgroundResIds = ambientResIds + guidedResIds

    // Achievement and streak tracking
    private var currentStreak: Int = 0
    private var longestStreak: Int = 0
    private var lastMeditationDate: String = ""
    private var totalHoursMeditated: Int = 0
    private var achievementsUnlocked: MutableSet<String> = mutableSetOf()
    
    // Heart rate measurement variables
    private var startHeartRate = 0
    private var startHRV = 0.0
    private var endHeartRate = 0
    private var endHRV = 0.0
    private var heartRateMeasured = false
    private var lastSessionDuration: Long = 0L
    
    companion object {
        private const val PREFS_NAME = "MeditationPrefs"
        private const val KEY_TOTAL_TIME = "total_meditation_time"
        private const val KEY_BELL_SOUND = "bell_sound"
        private const val KEY_CURRENT_STREAK = "current_streak"
        private const val KEY_LONGEST_STREAK = "longest_streak"
        private const val KEY_LAST_MEDITATION_DATE = "last_meditation_date"
        private const val KEY_TOTAL_HOURS = "total_hours"
        private const val KEY_ACHIEVEMENTS = "achievements"
        private const val KEY_HEART_RATE_ENABLED = "heart_rate_enabled"
        private const val KEY_DARK_MODE_ENABLED = "dark_mode_enabled"
    
        private const val REQUEST_CODE_PERMISSIONS = 101
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply dark mode preference before inflating views
        try {
            val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            val darkModeEnabled = prefs.getBoolean(KEY_DARK_MODE_ENABLED, false)
            val desiredMode = if (darkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            if (AppCompatDelegate.getDefaultNightMode() != desiredMode) {
                AppCompatDelegate.setDefaultNightMode(desiredMode)
            }
        } catch (_: Exception) { }

        setContentView(R.layout.activity_main)

        // Initialize achievement popup manager
        achievementPopupManager = AchievementPopupManager(this)

        // Try to initialize views immediately first
        Log.d("MainActivity", "Trying immediate view initialization")
        Log.d("MainActivity", "Looking for view with ID: background_image_view")
        val immediateView = findViewById<ImageView>(R.id.background_image_view)
        Log.d("MainActivity", "findViewById returned: $immediateView")

        if (immediateView != null) {
            Log.d("MainActivity", "Immediate view initialization successful")
            backgroundImageView = immediateView
            initializeViews()
        } else {
            // Fallback to post method
            Log.d("MainActivity", "Immediate view initialization failed, using post method")
            window.decorView.post {
                Log.d("MainActivity", "Window decorView post called")
                initializeViews()
            }
        }


        



    }

    private fun initializeViews() {
        try {
            // Initialize views
            timeEditText = findViewById(R.id.time_edit_text)
            timerText = findViewById(R.id.timer)
            startPauseButton = findViewById(R.id.button_start_pause)
            resetButton = findViewById(R.id.button_reset)
            adView = findViewById(R.id.adView)
            topAdView = findViewById(R.id.topAdView)
            totalTimeText = findViewById(R.id.total_time_text)
            appTitleText = findViewById(R.id.app_title)

            Log.d("MainActivity", "About to find background ImageView")
            Log.d("MainActivity", "R.id.background_image_view = ${R.id.background_image_view}")

            // Debug the view hierarchy
            val decorView = window.decorView
            Log.d("MainActivity", "Decor view: $decorView")
            val contentView = decorView.findViewById<View>(android.R.id.content)
            Log.d("MainActivity", "Content view: $contentView")

            // Try multiple approaches to find the view
            var bgView: ImageView? = null

            // Approach 1: Standard findViewById with explicit cast
            bgView = findViewById(R.id.background_image_view) as? ImageView
            Log.d("MainActivity", "Approach 1 result: $bgView")

            // Approach 2: If first fails, try without generic
            if (bgView == null) {
                val tempView = findViewById<View>(R.id.background_image_view)
                bgView = tempView as? ImageView
                Log.d("MainActivity", "Approach 2 result: $bgView")
            }

            // Approach 3: Try to get the root view and search from there
            if (bgView == null) {
                val rootView = window.decorView.findViewById<View>(android.R.id.content)
                bgView = rootView?.findViewById(R.id.background_image_view) as? ImageView
                Log.d("MainActivity", "Approach 3 result: $bgView")
            }

            // Approach 4: Try to traverse the view hierarchy manually
            if (bgView == null) {
                Log.d("MainActivity", "Trying manual view hierarchy traversal")
                val rootView = contentView
                Log.d("MainActivity", "Root view: $rootView")
                if (rootView != null && rootView is android.view.ViewGroup) {
                    for (i in 0 until rootView.childCount) {
                        val child = rootView.getChildAt(i)
                        Log.d("MainActivity", "Child $i: $child (id: ${child.id})")
                        if (child.id == R.id.background_image_view) {
                            bgView = child as? ImageView
                            Log.d("MainActivity", "Found ImageView manually: $bgView")
                            break
                        }
                        // Try to use ScrollView as background by setting its background drawable
                        if (child is android.widget.ScrollView && bgView == null) {
                            Log.d("MainActivity", "Found ScrollView, will use it for background")
                            Log.d("MainActivity", "ScrollView current background: ${child.background}")
                            bgView = ImageView(this).apply {
                                // Don't add this to hierarchy, just use it for resource management
                                setImageResource(R.drawable.shiva_bg)
                            }
                            // We'll handle the ScrollView background separately
                            scrollViewForBackground = child

                            // Clear any existing background on ScrollView first
                            child.background = null
                            child.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            child.invalidate()
                            Log.d("MainActivity", "Cleared ScrollView background")

                            // Set initial background on ScrollView (respect dark mode)
                            val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            val darkModeEnabled = prefs.getBoolean(KEY_DARK_MODE_ENABLED, false)

                            if (darkModeEnabled) {
                                // In dark mode, set solid black background
                                child.setBackgroundColor(android.graphics.Color.BLACK)
                                Log.d("MainActivity", "Set initial black background for dark mode")
                            } else {
                                // In light mode, set the first background image
                                val initialDrawable = resources.getDrawable(R.drawable.shiva_bg)
                                child.background = initialDrawable
                                Log.d("MainActivity", "Set initial background on ScrollView: $initialDrawable")
                            }
                            child.invalidate()
                            break
                        }
                    }
                }
            }

            // Approach 5: If still not found, create the ImageView programmatically
            if (bgView == null) {
                Log.d("MainActivity", "Creating ImageView programmatically")
                bgView = ImageView(this).apply {
                    id = R.id.background_image_view
                    layoutParams = android.widget.FrameLayout.LayoutParams(
                        android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                        android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                    ).apply {
                        // Ensure proper layout constraints
                        width = android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                        height = android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageResource(R.drawable.shiva_bg)

                    // Set minimum dimensions to ensure it's visible
                    minimumWidth = 1
                    minimumHeight = 1
                }

                // Try to add it to the root view
                if (contentView != null) {
                    (contentView as? android.widget.FrameLayout)?.addView(bgView, 0)
                    Log.d("MainActivity", "Added ImageView programmatically to FrameLayout")

                    // Ensure the ImageView is visible and bring it to front if needed
                    bgView.visibility = android.view.View.VISIBLE
                    bgView.bringToFront()
                    bgView.elevation = 10f // Ensure it's on top
                    Log.d("MainActivity", "Set ImageView visibility to VISIBLE, brought to front, and set elevation")

                    // Debug the ImageView properties
                    Log.d("MainActivity", "ImageView visibility: ${bgView.visibility}")
                    Log.d("MainActivity", "ImageView width: ${bgView.width}, height: ${bgView.height}")
                    Log.d("MainActivity", "ImageView drawable: ${bgView.drawable}")
                    Log.d("MainActivity", "ContentView children count: ${(contentView as? android.view.ViewGroup)?.childCount ?: 0}")

                    // Force layout and invalidate
                    bgView.requestLayout()
                    bgView.invalidate()
                    contentView.requestLayout()
                    contentView.invalidate()

                    // Force measure and layout
                    bgView.measure(
                        android.view.View.MeasureSpec.makeMeasureSpec(contentView.width, android.view.View.MeasureSpec.EXACTLY),
                        android.view.View.MeasureSpec.makeMeasureSpec(contentView.height, android.view.View.MeasureSpec.EXACTLY)
                    )
                    bgView.layout(0, 0, contentView.width, contentView.height)

                    // Final debug after layout
                    bgView.post {
                        Log.d("MainActivity", "ImageView after layout - width: ${bgView.width}, height: ${bgView.height}")
                        Log.d("MainActivity", "ImageView after layout - visibility: ${bgView.visibility}")
                    }
                } else {
                    // Fallback: add to the ScrollView's parent if possible
                    if (contentView is android.view.ViewGroup && contentView.childCount > 0) {
                        val scrollView = contentView.getChildAt(0) as? android.widget.ScrollView
                        if (scrollView != null) {
                            val scrollParent = scrollView.parent as? android.widget.FrameLayout
                            scrollParent?.addView(bgView, 0)
                            Log.d("MainActivity", "Added ImageView programmatically to ScrollView parent")

                            // Ensure the ImageView is visible
                            bgView.visibility = android.view.View.VISIBLE
                            bgView.bringToFront()
                        }
                    }
                }
            }

            if (bgView != null) {
                backgroundImageView = bgView
            }

            // Debug: Check if ImageView was found
            if (::backgroundImageView.isInitialized && backgroundImageView != null) {
                Log.d("MainActivity", "Background ImageView initialized successfully")
            } else {
                Log.e("MainActivity", "Background ImageView not initialized!")
                return
            }

            // Initialize background slideshow
            setupBackgroundSlideshow()

            // Initialize AdMob
            MobileAds.initialize(this) {}

            // Load banner ads
            try {
                val adRequest = AdRequest.Builder().build()
                adView.loadAd(adRequest)
                topAdView.loadAd(adRequest)
            } catch (e: Exception) {
                Log.e("AdMobError", "Failed to load ad: ${e.message}", e) // Log error
            }

            // Load interstitial ad
            loadInterstitialAd()

            // Load preferences
            val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            totalMeditationTimeMillis = prefs.getLong(KEY_TOTAL_TIME, 0L)
            selectedBellResId = prefs.getInt(KEY_BELL_SOUND, R.raw.bell)
            selectedBackgroundResId = prefs.getInt("KEY_BACKGROUND_SOUND", 0)

            // Heart rate setting will be loaded in settings dialog

            // Load achievement data
            loadAchievementData()

            updateTotalTimeText()

            // Load selected bell from prefs
            //val chooseBellButton = findViewById<Button>(R.id.button_choose_bell)
            //chooseBellButton.setOnClickListener {
            //    val builder = AlertDialog.Builder(this)
            //    builder.setTitle("Choose Bell Sound")
            //    val bellOptions = arrayOf(
            //        "Bell 1", "Bell 2", "Bell 3", "Bell 4", "Bell 5", "Bell 6"
            //    )
            //    // Use the actual resource IDs that match your filenames
            //    val bellResIds = arrayOf(
            //        R.raw.bell_1, // bell_1.wav
            //        R.raw.bell_2, // bell_2.wav
            //        R.raw.bell_3, // bell_3.wav
            //        R.raw.bell_4, // bell_4.wav
            //        R.raw.bell_5, // bell_5.wav
            //        R.raw.bell_6  // bell_6.wav
            //    )
            //    builder.setSingleChoiceItems(bellOptions, 0) { dialog, which ->
            //        try {
            //            // Play preview
            //            previewPlayer?.release()
            //            previewPlayer = MediaPlayer.create(this, bellResIds[which])
            //            if (previewPlayer != null) {
            //                previewPlayer?.setVolume(1.0f, 1.0f)
            //                previewPlayer?.start()
            //                // Save selection
            //                selectedBellResId = bellResIds[which]
            //                prefs.edit().putInt(KEY_BELL_SOUND, selectedBellResId).apply()
            //            } else {
            //                Log.e("MainActivity", "Failed to create preview player for bell: $which")
            //            }
            //        } catch (e: Exception) {
            //            Log.e("MainActivity", "Error playing bell preview: ${e.message}", e)
            //        }
            //    }
            //    builder.setPositiveButton("OK") { dialog, _ ->
            //        previewPlayer?.release()
            //        dialog.dismiss()
            //    }
            //    builder.setOnDismissListener {
            //        previewPlayer?.release()
            //    }
            //    builder.show()
            //}

            val chooseSoundOptionsButton = findViewById<Button>(R.id.button_choose_sound_options)
            chooseSoundOptionsButton.setOnClickListener {
                showSettingsDialog()
            }

            // Add achievements button
            val achievementsButton = findViewById<Button>(R.id.button_achievements)
            achievementsButton.setOnClickListener {
                showAchievementsDialog()
            }

            startPauseButton.setOnClickListener {
                if (isRunning) {
                    // Pausing - stop the timer
                    stopTimerService()
                    wasPaused = true
                    startPauseButton.text = "Start"
                    resetButton.visibility = View.VISIBLE
                } else {
                    val timeInput = timeEditText.text.toString()
                    if (timeInput.isNotEmpty()) {
                        // If resuming from pause, use current remaining time; otherwise use input
                        if (!wasPaused || timeInMilliSeconds <= 0) {
                            timeInMilliSeconds = timeInput.toLong() * 60000L

                            // Reset heart rate measurement state for new session
                            heartRateMeasured = false
                            startHeartRate = 0
                            startHRV = 0.0
                            endHeartRate = 0
                            endHRV = 0.0
                        }

                        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        val heartRateEnabled = prefs.getBoolean(KEY_HEART_RATE_ENABLED, false)
                        if (heartRateEnabled) {
                            // Check camera permission first
                            if (checkCameraPermission()) {
                                startHeartRateMeasurement()
                            } else {
                                requestCameraPermission()
                            }
                        } else {
                            startMeditationSession()
                        }
                    }
                }
            }
            resetButton.setOnClickListener {
                stopTimerService()
                resetTimerUI()
            }
            // Register receiver
            try {
                LocalBroadcastManager.getInstance(this).registerReceiver(
                    timerReceiver,
                    IntentFilter().apply {
                        addAction(TIMER_FINISHED_ACTION)
                        addAction(TIMER_TICK_ACTION)
                    }
                )
            } catch (e: Exception) {
                Log.e("MainActivity", "Error registering broadcast receiver: ${e.message}", e)
            }

            Log.d("MainActivity", "Views initialized successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing views: ${e.message}", e)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && !::backgroundImageView.isInitialized) {
            Log.d("MainActivity", "Window focus changed, trying to initialize views again")
            initializeViews()
        }
    }

    private fun startTimerService(time: Long, bellResId: Int) {
        val intent = Intent(this, MeditationTimerService::class.java).apply {
            action = MeditationTimerService.ACTION_START
            putExtra(MeditationTimerService.EXTRA_TIME_MILLIS, time)
            putExtra(MeditationTimerService.EXTRA_BELL_RES_ID, bellResId)
            putExtra(MeditationTimerService.EXTRA_BACKGROUND_RES_ID, selectedBackgroundResId)
        }
        startService(intent)
        isRunning = true
        startPauseButton.text = "Pause"
        resetButton.visibility = View.INVISIBLE
    }
    private fun stopTimerService() {
        val intent = Intent(this, MeditationTimerService::class.java).apply {
            action = MeditationTimerService.ACTION_STOP
        }
        startService(intent)
        isRunning = false
    }
    private fun resetTimerUI() {
        timerText.text = "00:00"
        startPauseButton.text = "Start"
        resetButton.visibility = View.INVISIBLE
        timeInMilliSeconds = 0L
        wasPaused = false
    }

    private fun updateTimerText(remainingTime: Long = 0L) {
        val timeToShow = if (remainingTime > 0) remainingTime else timeInMilliSeconds
        val minutes = (timeToShow / 1000) / 60
        val seconds = (timeToShow / 1000) % 60
        timerText.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun loadAchievementData() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0)
        longestStreak = prefs.getInt(KEY_LONGEST_STREAK, 0)
        lastMeditationDate = prefs.getString(KEY_LAST_MEDITATION_DATE, "") ?: ""
        totalHoursMeditated = prefs.getInt(KEY_TOTAL_HOURS, 0)
        achievementsUnlocked = prefs.getStringSet(KEY_ACHIEVEMENTS, setOf())?.toMutableSet() ?: mutableSetOf()
    }

    private fun saveAchievementData() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_CURRENT_STREAK, currentStreak)
            .putInt(KEY_LONGEST_STREAK, longestStreak)
            .putString(KEY_LAST_MEDITATION_DATE, lastMeditationDate)
            .putInt(KEY_TOTAL_HOURS, totalHoursMeditated)
            .putStringSet(KEY_ACHIEVEMENTS, achievementsUnlocked)
            .apply()
    }

    private fun updateStreak() {
        val calendar = Calendar.getInstance()
        val today = String.format("%04d-%02d-%02d", 
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH))
        
        if (lastMeditationDate.isEmpty()) {
            // First meditation
            currentStreak = 1
        } else {
            try {
                val lastDateParts = lastMeditationDate.split("-")
                val lastYear = lastDateParts[0].toInt()
                val lastMonth = lastDateParts[1].toInt() - 1 // Calendar months are 0-based
                val lastDay = lastDateParts[2].toInt()
                
                val lastCalendar = Calendar.getInstance()
                lastCalendar.set(lastYear, lastMonth, lastDay)
                
                val todayCalendar = Calendar.getInstance()
                todayCalendar.set(Calendar.HOUR_OF_DAY, 0)
                todayCalendar.set(Calendar.MINUTE, 0)
                todayCalendar.set(Calendar.SECOND, 0)
                todayCalendar.set(Calendar.MILLISECOND, 0)
                
                lastCalendar.set(Calendar.HOUR_OF_DAY, 0)
                lastCalendar.set(Calendar.MINUTE, 0)
                lastCalendar.set(Calendar.SECOND, 0)
                lastCalendar.set(Calendar.MILLISECOND, 0)
                
                val diffInMillis = todayCalendar.timeInMillis - lastCalendar.timeInMillis
                val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)
                
                if (diffInDays == 1L) {
                    // Consecutive day
                    currentStreak++
                } else if (diffInDays > 1L) {
                    // Streak broken
                    currentStreak = 1
                }
                // If same day (diffInDays == 0), don't change streak
            } catch (e: Exception) {
                // If parsing fails, start new streak
                currentStreak = 1
            }
        }
        
        lastMeditationDate = today
        
        if (currentStreak > longestStreak) {
            longestStreak = currentStreak
        }
        
        saveAchievementData()
    }

    private fun checkAchievements() {
        val newAchievements = mutableListOf<String>()
        
        // Hour-based achievements
        if (totalHoursMeditated >= 1 && !achievementsUnlocked.contains("first_hour")) {
            achievementsUnlocked.add("first_hour")
            newAchievements.add("🌅 First Hour - Welcome to your meditation journey!")
        }
        if (totalHoursMeditated >= 5 && !achievementsUnlocked.contains("dedicated_beginner")) {
            achievementsUnlocked.add("dedicated_beginner")
            newAchievements.add("🌟 Dedicated Beginner - 5 hours of mindfulness!")
        }
        if (totalHoursMeditated >= 10 && !achievementsUnlocked.contains("mindful_explorer")) {
            achievementsUnlocked.add("mindful_explorer")
            newAchievements.add("🧘‍♀️ Mindful Explorer - 10 hours of inner peace!")
        }
        if (totalHoursMeditated >= 25 && !achievementsUnlocked.contains("meditation_master")) {
            achievementsUnlocked.add("meditation_master")
            newAchievements.add("👑 Meditation Master - 25 hours of wisdom!")
        }
        if (totalHoursMeditated >= 50 && !achievementsUnlocked.contains("zen_sage")) {
            achievementsUnlocked.add("zen_sage")
            newAchievements.add("✨ Zen Sage - 50 hours of enlightenment!")
        }
        if (totalHoursMeditated >= 100 && !achievementsUnlocked.contains("enlightened_one")) {
            achievementsUnlocked.add("enlightened_one")
            newAchievements.add("🕉️ Enlightened One - 100 hours of transcendence!")
        }
        
        // Streak-based achievements
        if (currentStreak >= 3 && !achievementsUnlocked.contains("consistent_practitioner")) {
            achievementsUnlocked.add("consistent_practitioner")
            newAchievements.add("🔥 Consistent Practitioner - 3-day streak!")
        }
        if (currentStreak >= 7 && !achievementsUnlocked.contains("weekly_warrior")) {
            achievementsUnlocked.add("weekly_warrior")
            newAchievements.add("⚡ Weekly Warrior - 7-day streak!")
        }
        if (currentStreak >= 30 && !achievementsUnlocked.contains("monthly_master")) {
            achievementsUnlocked.add("monthly_master")
            newAchievements.add("🌙 Monthly Master - 30-day streak!")
        }
        if (currentStreak >= 100 && !achievementsUnlocked.contains("century_streak")) {
            achievementsUnlocked.add("century_streak")
            newAchievements.add("💎 Century Streak - 100-day streak!")
        }
        
        // Show achievement notifications
        newAchievements.forEach { achievement ->
            showAchievementNotification(achievement)
        }
        
        if (newAchievements.isNotEmpty()) {
            saveAchievementData()
        }
    }

    private lateinit var achievementPopupManager: AchievementPopupManager
    
    private fun showAchievementNotification(achievement: String) {
        // Check if achievement popup manager is initialized
        if (!::achievementPopupManager.isInitialized) {
            Log.w("MainActivity", "Achievement popup manager not initialized, skipping notification")
            return
        }
        
        // Extract achievement key from the achievement string
        val achievementKey = when {
            achievement.contains("First Hour") -> "first_hour"
            achievement.contains("Dedicated Beginner") -> "dedicated_beginner"
            achievement.contains("Mindful Explorer") -> "mindful_explorer"
            achievement.contains("Meditation Master") -> "meditation_master"
            achievement.contains("Zen Sage") -> "zen_sage"
            achievement.contains("Enlightened One") -> "enlightened_one"
            achievement.contains("Consistent Practitioner") -> "consistent_practitioner"
            achievement.contains("Weekly Warrior") -> "weekly_warrior"
            achievement.contains("Monthly Master") -> "monthly_master"
            achievement.contains("Century Streak") -> "century_streak"
            else -> "unknown"
        }
        
        // Show beautiful popup
        achievementPopupManager.showAchievementPopup(achievementKey, achievement)
    }

    private fun updateTotalTimeText() {
        val hours = totalMeditationTimeMillis / (1000 * 60 * 60)
        val minutes = (totalMeditationTimeMillis % (1000 * 60 * 60)) / (1000 * 60)
        totalTimeText.text = String.format(Locale.getDefault(), "Total: %02dh %02dm", hours, minutes)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            // Stop background slideshow
            backgroundHandler.removeCallbacks(backgroundRunnable)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error stopping background slideshow: ${e.message}", e)
        }

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(timerReceiver)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error unregistering broadcast receiver: ${e.message}", e)
        }
    }

    private fun showAchievementsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.achievements_dialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Set dialog width to 90% of screen width
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        
        // Get dialog views
        val closeButton = dialog.findViewById<Button>(R.id.button_close_achievements)
        val statsContainer = dialog.findViewById<LinearLayout>(R.id.stats_container)
        val achievementsContainer = dialog.findViewById<LinearLayout>(R.id.achievements_container)
        
        // Update stats
        val currentStreakText = dialog.findViewById<TextView>(R.id.text_current_streak)
        val longestStreakText = dialog.findViewById<TextView>(R.id.text_longest_streak)
        val totalHoursText = dialog.findViewById<TextView>(R.id.text_total_hours)
        val achievementsCountText = dialog.findViewById<TextView>(R.id.text_achievements_count)
        
        currentStreakText.text = "$currentStreak"
        longestStreakText.text = "$longestStreak"
        totalHoursText.text = "$totalHoursMeditated"
        achievementsCountText.text = "${achievementsUnlocked.size}/10"
        
        // Clear previous achievements
        achievementsContainer.removeAllViews()
        
        // Add achievements
        if (achievementsUnlocked.isNotEmpty()) {
            achievementsUnlocked.forEach { achievementKey ->
                val achievementView = createAchievementView(achievementKey, true)
                achievementsContainer.addView(achievementView)
            }
        } else {
            val noAchievementsText = TextView(this).apply {
                text = "Complete your first meditation to unlock achievements!"
                textSize = 16f
                setTextColor(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    resources.getColor(R.color.shiva_ash_gray, null)
                } else {
                    @Suppress("DEPRECATION")
                    resources.getColor(R.color.shiva_ash_gray)
                })
                gravity = Gravity.CENTER
                setPadding(32, 32, 32, 32)
            }
            achievementsContainer.addView(noAchievementsText)
        }
        
        // Add locked achievements
        val allAchievements = listOf(
            "first_hour", "dedicated_beginner", "mindful_explorer", "meditation_master",
            "zen_sage", "enlightened_one", "consistent_practitioner", "weekly_warrior",
            "monthly_master", "century_streak"
        )
        
        allAchievements.forEach { achievementKey ->
            if (!achievementsUnlocked.contains(achievementKey)) {
                val achievementView = createAchievementView(achievementKey, false)
                achievementsContainer.addView(achievementView)
            }
        }
        
        closeButton.setOnClickListener {
            dialog.dismiss()
            showInterstitialAdIfAvailable()
        }
        
        // Add a test button for debugging achievements (you can remove this later)
        val testButton = dialog.findViewById<Button>(R.id.button_test_achievements)
        testButton?.setOnClickListener {
            // Simulate some progress for testing
            totalHoursMeditated += 1
            currentStreak += 1
            if (currentStreak > longestStreak) {
                longestStreak = currentStreak
            }
            checkAchievements()
            saveAchievementData()
            
            // Refresh the dialog
            dialog.dismiss()
            showAchievementsDialog()
        }
        
        dialog.show()
    }
    
        private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            "ca-app-pub-2722920301958819/7531366385", // Interstitial ad unit ID
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d("AdMob", "Interstitial ad loaded successfully")
                    
                    // Set up the full screen content callback
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d("AdMob", "Interstitial ad was dismissed")
                            interstitialAd = null
                            // Load the next ad
                            loadInterstitialAd()
                        }
                        
                                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.e("AdMob", "Interstitial ad failed to show: ${adError.message}")
                        interstitialAd = null
                    }
                        
                        override fun onAdShowedFullScreenContent() {
                            Log.d("AdMob", "Interstitial ad showed full screen content")
                        }
                    }
                }
                
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e("AdMob", "Interstitial ad failed to load: ${loadAdError.message}")
                    interstitialAd = null
                }
            }
        )
    }
    
    private fun showInterstitialAdIfAvailable() {
        interstitialAd?.let { ad ->
            ad.show(this)
        } ?: run {
            // No ad available, try to load one for next time
            loadInterstitialAd()
        }
    }
    
    private fun createAchievementView(achievementKey: String, isUnlocked: Boolean): View {
        val achievementView = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 12, 16, 12)
            background = if (isUnlocked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    resources.getDrawable(R.drawable.achievement_unlocked_background, null)
                } else {
                    @Suppress("DEPRECATION")
                    resources.getDrawable(R.drawable.achievement_unlocked_background)
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    resources.getDrawable(R.drawable.achievement_locked_background, null)
                } else {
                    @Suppress("DEPRECATION")
                    resources.getDrawable(R.drawable.achievement_locked_background)
                }
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        }
        
        // Achievement icon
        val iconView = TextView(this).apply {
            text = getAchievementIcon(achievementKey)
            textSize = 24f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(48, 48)
        }
        
        // Achievement text
        val textView = TextView(this).apply {
            text = getAchievementDisplayName(achievementKey)
            textSize = 14f
            setTextColor(if (isUnlocked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    resources.getColor(R.color.shiva_text_white, null)
                } else {
                    @Suppress("DEPRECATION")
                    resources.getColor(R.color.shiva_text_white)
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    resources.getColor(R.color.shiva_ash_gray, null)
                } else {
                    @Suppress("DEPRECATION")
                    resources.getColor(R.color.shiva_ash_gray)
                }
            })
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 0, 0, 0)
            }
        }
        
        // Lock icon for locked achievements
        if (!isUnlocked) {
            val lockIcon = TextView(this).apply {
                text = "🔒"
                textSize = 16f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(32, 32)
            }
            achievementView.addView(iconView)
            achievementView.addView(textView)
            achievementView.addView(lockIcon)
        } else {
            achievementView.addView(iconView)
            achievementView.addView(textView)
        }
        
        return achievementView
    }

    private fun getAchievementIcon(achievementKey: String): String {
        return when (achievementKey) {
            "first_hour" -> "🌅"
            "dedicated_beginner" -> "🌟"
            "mindful_explorer" -> "🧘‍♀️"
            "meditation_master" -> "👑"
            "zen_sage" -> "✨"
            "enlightened_one" -> "🕉️"
            "consistent_practitioner" -> "🔥"
            "weekly_warrior" -> "⚡"
            "monthly_master" -> "🌙"
            "century_streak" -> "💎"
            else -> "🏅"
        }
    }

    private fun showSettingsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.settings_dialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Set dialog width to 90% of screen width
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        
        // Get dialog views
        val closeButton = dialog.findViewById<Button>(R.id.button_close_settings)
        val bellSoundOption = dialog.findViewById<LinearLayout>(R.id.bell_sound_option)
        val backgroundSoundOption = dialog.findViewById<LinearLayout>(R.id.background_sound_option)
        val resetTotalTimeOption = dialog.findViewById<LinearLayout>(R.id.reset_total_time_option)
        val heartRateToggle = dialog.findViewById<SwitchCompat>(R.id.heart_rate_toggle)
        val darkModeToggle = dialog.findViewById<SwitchCompat>(R.id.dark_mode_toggle)
        
        // Load and set heart rate toggle state
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val heartRateEnabled = prefs.getBoolean(KEY_HEART_RATE_ENABLED, false)
        heartRateToggle.isChecked = heartRateEnabled
        val darkModeEnabled = prefs.getBoolean(KEY_DARK_MODE_ENABLED, false)
        darkModeToggle?.isChecked = darkModeEnabled
        
        // Set heart rate toggle listener
        heartRateToggle.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_HEART_RATE_ENABLED, isChecked).apply()
        }
        darkModeToggle?.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_DARK_MODE_ENABLED, isChecked).apply()
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            if (AppCompatDelegate.getDefaultNightMode() != mode) {
                AppCompatDelegate.setDefaultNightMode(mode)
            }
        }
        
        closeButton.setOnClickListener {
            dialog.dismiss()
        }
        
        bellSoundOption.setOnClickListener {
            dialog.dismiss()
            showBellSoundPicker()
        }
        
        backgroundSoundOption.setOnClickListener {
            dialog.dismiss()
            showBackgroundSoundPicker()
        }
        
        resetTotalTimeOption.setOnClickListener {
            dialog.dismiss()
            showResetTotalTimeConfirmation()
        }
        
        dialog.show()
    }
    
    private fun showResetTotalTimeConfirmation() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Reset Total Time")
        builder.setMessage("Are you sure you want to reset your total meditation time and achievements? This action cannot be undone.")
        
        builder.setPositiveButton("Reset") { dialog, _ ->
            totalMeditationTimeMillis = 0L
            totalHoursMeditated = 0
            achievementsUnlocked.clear()
            currentStreak = 0
            longestStreak = 0
            lastMeditationDate = ""
            val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            prefs.edit()
                .putLong(KEY_TOTAL_TIME, 0L)
                .putInt(KEY_TOTAL_HOURS, 0)
                .putStringSet(KEY_ACHIEVEMENTS, setOf())
                .putInt(KEY_CURRENT_STREAK, 0)
                .putInt(KEY_LONGEST_STREAK, 0)
                .putString(KEY_LAST_MEDITATION_DATE, "")
                .apply()
            updateTotalTimeText()
            dialog.dismiss()
        }
        
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        
        builder.show()
    }

    private fun showBellSoundPicker() {
        val dialogView = layoutInflater.inflate(R.layout.bell_sound_selection_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val bellResIds = arrayOf(
            R.raw.bell_1, R.raw.bell_2, R.raw.bell_3, R.raw.bell_4, R.raw.bell_5, R.raw.bell_6
        )
        val currentBellIndex = bellResIds.indexOf(selectedBellResId).coerceAtLeast(0)
        val currentBellText = dialogView.findViewById<TextView>(R.id.text_current_bell_sound)
        currentBellText.text = when (currentBellIndex) {
            0 -> "Ding"
            1 -> "Dong"
            2 -> "Ting"
            3 -> "Ring"
            4 -> "Chime"
            5 -> "Bong"
            else -> "Bell ${currentBellIndex + 1}"
        }

        // Setup bell buttons with highlighting
        setupBellButtons(dialogView, prefs, bellResIds, currentBellText, dialog)

        // Close button
        dialogView.findViewById<Button>(R.id.button_close_bell_sound_dialog).setOnClickListener {
            previewPlayer?.release()
            dialog.dismiss()
        }


        dialog.setOnDismissListener { previewPlayer?.release() }
        dialog.show()
    }

    private fun showBackgroundSoundPicker() {
        val dialogView = layoutInflater.inflate(R.layout.background_sound_selection_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val currentBgIndex = backgroundResIds.indexOf(selectedBackgroundResId).coerceAtLeast(0)
        val currentSoundText = dialogView.findViewById<TextView>(R.id.text_current_bg_sound)
        currentSoundText.text = backgroundSoundNames.getOrElse(currentBgIndex) { "Complete Silence" }

        // Setup sound buttons
        setupSoundButtons(dialogView, prefs, dialog, currentSoundText)

        // Close button
        dialogView.findViewById<Button>(R.id.button_close_bg_sound_dialog).setOnClickListener {
            previewBackgroundPlayer?.release()
            dialog.dismiss()
        }

        // Stop preview button
        dialogView.findViewById<Button>(R.id.button_stop_preview).setOnClickListener {
            previewBackgroundPlayer?.release()
        }

        // Confirm button
        dialogView.findViewById<Button>(R.id.button_confirm_bg_sound).setOnClickListener {
            previewBackgroundPlayer?.release()
            dialog.dismiss()
        }

        dialog.setOnDismissListener { previewBackgroundPlayer?.release() }
        dialog.show()
    }

    private fun setupSoundButtons(dialogView: View, prefs: SharedPreferences, dialog: AlertDialog, currentSoundText: TextView) {
        val ambientContainer = dialogView.findViewById<LinearLayout>(R.id.ambient_sounds_container)
        val guidedContainer = dialogView.findViewById<LinearLayout>(R.id.guided_sounds_container)

        // Clear existing buttons
        ambientContainer.removeAllViews()
        guidedContainer.removeAllViews()

        // Create buttons for ambient sounds
        for (i in ambientSoundNames.indices) {
            val button = createSoundButton(ambientSoundNames[i], ambientResIds[i], i, prefs, currentSoundText, ambientContainer, guidedContainer)
            ambientContainer.addView(button)
        }

        // Create buttons for guided meditations
        for (i in guidedSoundNames.indices) {
            val globalIndex = ambientSoundNames.size + i
            val button = createSoundButton(guidedSoundNames[i], guidedResIds[i], globalIndex, prefs, currentSoundText, ambientContainer, guidedContainer)
            guidedContainer.addView(button)
        }
    }

    private fun createSoundButton(soundName: String, soundResId: Int, globalIndex: Int, prefs: SharedPreferences, currentSoundText: TextView, ambientContainer: LinearLayout, guidedContainer: LinearLayout): Button {
        return Button(this).apply {
            text = soundName.replace("_", " ").capitalizeWords()
            setBackgroundResource(R.drawable.sound_button_background)
            setTextColor(resources.getColor(R.color.shiva_text_white))
            textSize = 14f
            isAllCaps = false
            gravity = Gravity.CENTER
            minimumHeight = 0
            minHeight = 70
            setPadding(16, 12, 16, 12)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 4, 8, 4)
            }

            // Highlight selected sound
            if (soundResId == selectedBackgroundResId) {
                setBackgroundResource(R.drawable.sound_button_highlighted)
                setTextColor(resources.getColor(R.color.shiva_text_dark))
            }

            setOnClickListener {
                try {
                    previewBackgroundPlayer?.release()
                    if (soundResId != 0) {
                        previewBackgroundPlayer = MediaPlayer.create(this@MainActivity, soundResId)
                        // Only loop specific ambient sounds
                        val isLoopableAmbient = soundName in arrayOf("tibetan_chant", "aum_mantra", "birds", "jungle_rain")
                        previewBackgroundPlayer?.isLooping = isLoopableAmbient
                        previewBackgroundPlayer?.setVolume(1.0f, 1.0f)
                        previewBackgroundPlayer?.start()
                    }
                    selectedBackgroundResId = soundResId
                    currentSoundText.text = soundName.replace("_", " ").capitalizeWords()

                    // Update button highlighting
                    updateAllSoundButtonHighlights(ambientContainer, guidedContainer)

                    prefs.edit().putInt("KEY_BACKGROUND_SOUND", selectedBackgroundResId).apply()
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error playing sound preview: ${e.message}", e)
                }
            }
        }
    }

    private fun updateAllSoundButtonHighlights(ambientContainer: LinearLayout, guidedContainer: LinearLayout) {
        // Update all ambient sound buttons
        for (i in 0 until ambientContainer.childCount) {
            val button = ambientContainer.getChildAt(i) as Button
            val soundIndex = i
            if (ambientResIds[soundIndex] == selectedBackgroundResId) {
                button.setBackgroundResource(R.drawable.sound_button_highlighted)
                button.setTextColor(resources.getColor(R.color.shiva_text_dark))
            } else {
                button.setBackgroundResource(R.drawable.sound_button_background)
                button.setTextColor(resources.getColor(R.color.shiva_text_white))
            }
        }

        // Update all guided sound buttons
        for (i in 0 until guidedContainer.childCount) {
            val button = guidedContainer.getChildAt(i) as Button
            val soundIndex = i
            if (guidedResIds[soundIndex] == selectedBackgroundResId) {
                button.setBackgroundResource(R.drawable.sound_button_highlighted)
                button.setTextColor(resources.getColor(R.color.shiva_text_dark))
            } else {
                button.setBackgroundResource(R.drawable.sound_button_background)
                button.setTextColor(resources.getColor(R.color.shiva_text_white))
            }
        }
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { it.capitalize() }
    }

    private fun setupBellButtons(dialogView: View, prefs: SharedPreferences, bellResIds: Array<Int>, currentBellText: TextView, dialog: AlertDialog) {
        val bellButtons = arrayOf(
            R.id.btn_bell_1, R.id.btn_bell_2, R.id.btn_bell_3,
            R.id.btn_bell_4, R.id.btn_bell_5, R.id.btn_bell_6
        )

        for (i in bellButtons.indices) {
            val button = dialogView.findViewById<Button>(bellButtons[i])

            // Set background and text color based on selection
            if (bellResIds[i] == selectedBellResId) {
                button.setBackgroundResource(R.drawable.bell_button_highlighted)
                button.setTextColor(resources.getColor(android.R.color.black))
            } else {
                button.setBackgroundResource(R.drawable.bell_button_background)
                button.setTextColor(resources.getColor(R.color.shiva_text_white))
            }

            button.setOnClickListener {
                try {
                    // Check if this bell is already selected and playing
                    val isCurrentlySelected = bellResIds[i] == selectedBellResId
                    
                    // Stop any currently playing preview
                    previewPlayer?.release()
                    
                    // If this is the same bell that's already selected, just stop the preview
                    if (isCurrentlySelected) {
                        previewPlayer = null
                        return@setOnClickListener
                    }
                    
                    // Play new bell preview
                    previewPlayer = MediaPlayer.create(this, bellResIds[i])
                    previewPlayer?.setVolume(1.0f, 1.0f)
                    previewPlayer?.start()
                    selectedBellResId = bellResIds[i]
                    currentBellText.text = when (i) {
                        0 -> "Ding"
                        1 -> "Dong"
                        2 -> "Ting"
                        3 -> "Ring"
                        4 -> "Chime"
                        5 -> "Bong"
                        else -> "Bell ${i + 1}"
                    }

                    // Update button highlighting
                    for (j in bellButtons.indices) {
                        val otherButton = dialogView.findViewById<Button>(bellButtons[j])
                        if (j == i) {
                            otherButton.setBackgroundResource(R.drawable.bell_button_highlighted)
                            otherButton.setTextColor(resources.getColor(android.R.color.black))
                        } else {
                            otherButton.setBackgroundResource(R.drawable.bell_button_background)
                            otherButton.setTextColor(resources.getColor(R.color.shiva_text_white))
                        }
                    }

                    prefs.edit().putInt(KEY_BELL_SOUND, selectedBellResId).apply()
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error playing bell preview: ${e.message}", e)
                }
            }
        }
    }

    private fun setupSoundButton(dialogView: View, buttonId: Int, soundIndex: Int, prefs: SharedPreferences, currentSoundText: TextView) {
        dialogView.findViewById<Button>(buttonId)?.setOnClickListener {
            try {
                previewBackgroundPlayer?.release()
                if (backgroundResIds[soundIndex] != 0) {
                    previewBackgroundPlayer = MediaPlayer.create(this, backgroundResIds[soundIndex])
                    previewBackgroundPlayer?.isLooping = true
                    previewBackgroundPlayer?.setVolume(1.0f, 1.0f)
                    previewBackgroundPlayer?.start()
                }
                selectedBackgroundResId = backgroundResIds[soundIndex]
                currentSoundText.text = backgroundSoundNames.getOrElse(soundIndex) { "Complete Silence" }
                prefs.edit().putInt("KEY_BACKGROUND_SOUND", selectedBackgroundResId).apply()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error playing sound preview: ${e.message}", e)
            }
        }
    }

    private fun getAchievementDisplayName(achievementKey: String): String {
        return when (achievementKey) {
            "first_hour" -> "🌅 First Hour - Welcome to your meditation journey!"
            "dedicated_beginner" -> "🌟 Dedicated Beginner - 5 hours of mindfulness!"
            "mindful_explorer" -> "🧘‍♀️ Mindful Explorer - 10 hours of inner peace!"
            "meditation_master" -> "👑 Meditation Master - 25 hours of wisdom!"
            "zen_sage" -> "✨ Zen Sage - 50 hours of enlightenment!"
            "enlightened_one" -> "🕉️ Enlightened One - 100 hours of transcendence!"
            "consistent_practitioner" -> "🔥 Consistent Practitioner - 3-day streak!"
            "weekly_warrior" -> "⚡ Weekly Warrior - 7-day streak!"
            "monthly_master" -> "🌙 Monthly Master - 30-day streak!"
            "century_streak" -> "💎 Century Streak - 100-day streak!"
            else -> "🏅 $achievementKey"
        }
    }
    
    // Heart Rate Measurement Methods
    private fun checkCameraPermission(): Boolean {
        try {
            return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            Log.e("MainActivity", "Error checking camera permission: ${e.message}", e)
            return false
        }
    }
    
    private fun requestCameraPermission() {
        try {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CODE_PERMISSIONS
            )
        } catch (e: Exception) {
            Log.e("MainActivity", "Error requesting camera permission: ${e.message}", e)
            // If permission request fails, start meditation without heart rate measurement
            startMeditationSession()
        }
    }
    
    private fun startHeartRateMeasurement() {
        val intent = Intent(this, SimpleHeartRateActivity::class.java).apply {
            putExtra("is_end_measurement", false)
        }
        heartRateLauncher.launch(intent)
    }
    
    private fun startMeditationSession() {
        sessionStartTime = System.currentTimeMillis()
        startTimerService(timeInMilliSeconds, selectedBellResId)
    }
    
    private fun measureEndHeartRate() {
        val intent = Intent(this, SimpleHeartRateActivity::class.java).apply {
            putExtra("is_end_measurement", true)
        }
        heartRateLauncher.launch(intent)
    }
    
    private fun showRelaxationSummary() {
        val intent = Intent(this, RelaxationSummaryActivity::class.java).apply {
            putExtra("start_heart_rate", startHeartRate)
            putExtra("start_hrv", startHRV)
            putExtra("end_heart_rate", endHeartRate)
            putExtra("end_hrv", endHRV)
            putExtra("session_duration", lastSessionDuration)
        }
        startActivity(intent)
    }
    

    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startHeartRateMeasurement()
            } else {
                // Permission denied, start meditation without heart rate measurement
                startMeditationSession()
            }
        }
    }

    private fun setupBackgroundSlideshow() {
        backgroundHandler = Handler(Looper.getMainLooper())

        backgroundRunnable = object : Runnable {
            override fun run() {
                try {
                    // Check if ImageView is initialized
                    if (!::backgroundImageView.isInitialized) {
                        Log.e("MainActivity", "Background ImageView not initialized, reinitializing...")
                        backgroundImageView = findViewById(R.id.background_image_view)
                        if (backgroundImageView == null) {
                            Log.e("MainActivity", "Failed to find background ImageView")
                            return
                        }
                    }

                    // Cycle to next background image
                    currentBackgroundIndex = (currentBackgroundIndex + 1) % backgroundImages.size
                    val nextImageResId = backgroundImages[currentBackgroundIndex]

                    // Check if dark mode is enabled - if so, use solid black background
                    val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    val darkModeEnabled = prefs.getBoolean(KEY_DARK_MODE_ENABLED, false)

                    if (darkModeEnabled) {
                        // In dark mode, set solid black background and skip slideshow
                        Log.d("MainActivity", "Dark mode enabled, setting solid black background")
                        val blackColor = android.graphics.Color.BLACK

                        if (scrollViewForBackground != null) {
                            scrollViewForBackground?.setBackgroundColor(blackColor)
                            scrollViewForBackground?.invalidate()

                            val contentView = window.decorView.findViewById<View>(android.R.id.content)
                            if (contentView != null) {
                                contentView.setBackgroundColor(blackColor)
                                contentView.invalidate()
                            }
                        } else if (::backgroundImageView.isInitialized) {
                            backgroundImageView.setBackgroundColor(blackColor)
                            backgroundImageView.invalidate()
                        }

                        Log.d("MainActivity", "Set solid black background for dark mode")
                        return // Skip the rest of the slideshow logic
                    }

                    // Check if the resource exists and set the background
                    try {
                        // Prioritize ScrollView approach since ImageView has layout issues
                        if (scrollViewForBackground != null) {
                            Log.d("MainActivity", "Using ScrollView for background change (preferred method)")
                            val drawable = resources.getDrawable(nextImageResId)
                            scrollViewForBackground?.background = drawable
                            scrollViewForBackground?.invalidate()
                            scrollViewForBackground?.requestLayout()

                            // Also try setting on the root content view for better visibility
                            val contentView = window.decorView.findViewById<View>(android.R.id.content)
                            if (contentView != null) {
                                contentView.background = drawable
                                contentView.invalidate()
                                contentView.requestLayout()
                                Log.d("MainActivity", "Also set background on content view")
                            }

                            Log.d("MainActivity", "Background changed to: $nextImageResId (index: $currentBackgroundIndex) via ScrollView")
                        } else if (::backgroundImageView.isInitialized) {
                            // Fallback to ImageView if ScrollView not available
                            Log.d("MainActivity", "ScrollView not available, trying ImageView fallback")
                            backgroundImageView.setImageResource(nextImageResId)
                            Log.d("MainActivity", "Background changed to: $nextImageResId (index: $currentBackgroundIndex) via ImageView")

                            // Debug the ImageView after setting the image
                            Log.d("MainActivity", "ImageView drawable after change: ${backgroundImageView.drawable}")
                            Log.d("MainActivity", "ImageView visibility after change: ${backgroundImageView.visibility}")
                            Log.d("MainActivity", "ImageView width: ${backgroundImageView.width}, height: ${backgroundImageView.height}")
                        } else {
                            Log.e("MainActivity", "Neither ImageView nor ScrollView available for background")
                            return
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Failed to load background image: $nextImageResId, ${e.message}")
                        // Try the next image
                        currentBackgroundIndex = (currentBackgroundIndex + 1) % backgroundImages.size
                        return
                    }

                    // Schedule next background change
                    backgroundHandler.postDelayed(this, backgroundChangeInterval)

                    // Force a layout update to ensure the image change is visible
                    if (::backgroundImageView.isInitialized) {
                        backgroundImageView.requestLayout()
                        backgroundImageView.invalidate()
                    }
                    scrollViewForBackground?.requestLayout()
                    scrollViewForBackground?.invalidate()
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error changing background: ${e.message}", e)
                }
            }
        }

        Log.d("MainActivity", "Setting up background slideshow with ${backgroundImages.size} images")

        // Start the background slideshow immediately for the first change
        backgroundHandler.postDelayed(backgroundRunnable, 2000L) // Start after 2 seconds
    }
}