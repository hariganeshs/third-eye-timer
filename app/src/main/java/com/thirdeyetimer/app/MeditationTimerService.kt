package com.thirdeyetimer.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.util.Log
import android.content.pm.ServiceInfo

class MeditationTimerService : Service() {
    companion object {
        const val CHANNEL_ID = "MeditationTimerChannel"
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_TIME_MILLIS = "EXTRA_TIME_MILLIS"
        const val EXTRA_BELL_RES_ID = "EXTRA_BELL_RES_ID"
        const val EXTRA_BACKGROUND_RES_ID = "EXTRA_BACKGROUND_RES_ID"
        const val EXTRA_GUIDED_RES_ID = "EXTRA_GUIDED_RES_ID"
        const val TIMER_FINISHED_ACTION = "com.thirdeyetimer.app.TIMER_FINISHED"
        const val TIMER_TICK_ACTION = "com.thirdeyetimer.app.TIMER_TICK"
        const val EXTRA_REMAINING_TIME = "EXTRA_REMAINING_TIME"
    }

    private var countdownTimer: CountDownTimer? = null
    private var mediaPlayer: MediaPlayer? = null
    private var backgroundPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_START) {
            val timeMillis = intent.getLongExtra(EXTRA_TIME_MILLIS, 0L)
            val bellResId = intent.getIntExtra(EXTRA_BELL_RES_ID, 0)
            val backgroundResId = intent.getIntExtra(EXTRA_BACKGROUND_RES_ID, 0)
            val guidedResId = intent.getIntExtra(EXTRA_GUIDED_RES_ID, 0)
            startForegroundServiceWithTimer(timeMillis, bellResId, backgroundResId, guidedResId)
        } else if (intent?.action == ACTION_STOP) {
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun startForegroundServiceWithTimer(timeMillis: Long, bellResId: Int, backgroundResId: Int, guidedResId: Int = 0) {
        createNotificationChannel()
        val notification = buildNotification("Meditation timer running...")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(1, notification)
            }
        } catch (e: Exception) {
            Log.e("MeditationTimerService", "Error starting foreground service: ${e.message}", e)
            // Fallback to regular foreground service
            startForeground(1, notification)
        }

        // Start background sound or guided meditation if selected
        backgroundPlayer?.release()
        
        // Priority: guided meditation first, then background sound
        val soundToPlay = if (guidedResId != 0) guidedResId else backgroundResId
        
        if (soundToPlay != 0) {
            try {
                Log.d("MeditationTimerService", "Starting playback for resource: $soundToPlay (guided: ${guidedResId != 0})")
                backgroundPlayer = MediaPlayer.create(this, soundToPlay)
                
                if (backgroundPlayer != null) {
                    // Only loop ambient sounds, not guided meditations
                    backgroundPlayer?.isLooping = shouldLoopBackgroundSound(soundToPlay)
                    backgroundPlayer?.setVolume(1.0f, 1.0f)
                    backgroundPlayer?.start()
                    Log.d("MeditationTimerService", "Successfully started playback")
                } else {
                    Log.e("MeditationTimerService", "Failed to create MediaPlayer for resource: $soundToPlay")
                }
            } catch (e: Exception) {
                Log.e("MeditationTimerService", "Error playing background/guided sound: ${e.message}", e)
            }
        } else {
            Log.d("MeditationTimerService", "No background or guided meditation sound selected (silent meditation)")
        }

        countdownTimer?.cancel()
        countdownTimer = object : CountDownTimer(timeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Send broadcast with remaining time
                val intent = Intent(TIMER_TICK_ACTION)
                intent.putExtra(EXTRA_REMAINING_TIME, millisUntilFinished)
                LocalBroadcastManager.getInstance(this@MeditationTimerService).sendBroadcast(intent)
            }
            override fun onFinish() {
                Log.d("MeditationTimerService", "Timer finished! Playing bell sound...")
                playBell(bellResId)
                // Stop background sound
                backgroundPlayer?.stop()
                backgroundPlayer?.release()
                backgroundPlayer = null
                // Send broadcast to MainActivity
                val intent = Intent(TIMER_FINISHED_ACTION)
                LocalBroadcastManager.getInstance(this@MeditationTimerService).sendBroadcast(intent)
                Log.d("MeditationTimerService", "Timer finished broadcast sent")
                // Delay stopping the service to allow bell sound to play
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    Log.d("MeditationTimerService", "Stopping service after bell sound")
                    stopSelf()
                }, 3000) // Wait 3 seconds for bell sound to complete
            }
        }.start()
    }

    private fun playBell(bellResId: Int) {
        Log.d("MeditationTimerService", "Attempting to play bell sound with resId: $bellResId")
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, bellResId)
            if (mediaPlayer != null) {
                Log.d("MeditationTimerService", "MediaPlayer created successfully")
                mediaPlayer?.setVolume(1.0f, 1.0f)
                mediaPlayer?.setOnCompletionListener {
                    Log.d("MeditationTimerService", "Bell sound finished playing")
                }
                mediaPlayer?.setOnErrorListener { mp, what, extra ->
                    Log.e("MeditationTimerService", "MediaPlayer error: what=$what, extra=$extra")
                    playDefaultBell()
                    true
                }
                mediaPlayer?.start()
                Log.d("MeditationTimerService", "Bell sound started playing")
            } else {
                Log.e("MeditationTimerService", "Failed to create MediaPlayer for bell sound: $bellResId")
                // Fallback to default bell sound
                playDefaultBell()
            }
        } catch (e: Exception) {
            Log.e("MeditationTimerService", "Error playing bell sound: ${e.message}", e)
            // Fallback to default bell sound
            playDefaultBell()
        }
    }

    private fun playDefaultBell() {
        Log.d("MeditationTimerService", "Playing default bell sound")
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, R.raw.bell)
            if (mediaPlayer != null) {
                Log.d("MeditationTimerService", "Default MediaPlayer created successfully")
                mediaPlayer?.setVolume(1.0f, 1.0f)
                mediaPlayer?.setOnCompletionListener {
                    Log.d("MeditationTimerService", "Default bell sound finished playing")
                }
                mediaPlayer?.setOnErrorListener { mp, what, extra ->
                    Log.e("MeditationTimerService", "Default MediaPlayer error: what=$what, extra=$extra")
                    true
                }
                mediaPlayer?.start()
                Log.d("MeditationTimerService", "Default bell sound started playing")
            } else {
                Log.e("MeditationTimerService", "Failed to create default MediaPlayer")
            }
        } catch (e: Exception) {
            Log.e("MeditationTimerService", "Error playing default bell sound: ${e.message}", e)
        }
    }

    private fun shouldLoopBackgroundSound(resId: Int): Boolean {
        // Define ambient sound resource IDs that should loop
        val ambientSoundIds = arrayOf(
            R.raw.tibetan_chant,
            R.raw.aum_mantra,
            R.raw.birds,
            R.raw.jungle_rain
        )

        return ambientSoundIds.contains(resId)
    }

    private fun buildNotification(content: String): Notification {
        val stopIntent = Intent(this, MeditationTimerService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Meditation Timer")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .addAction(android.R.drawable.ic_delete, "Stop", stopPendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Meditation Timer Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer?.cancel()
        countdownTimer = null
        
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("MeditationTimerService", "Error releasing mediaPlayer: ${e.message}", e)
        }
        
        try {
            backgroundPlayer?.stop()
            backgroundPlayer?.release()
            backgroundPlayer = null
        } catch (e: Exception) {
            Log.e("MeditationTimerService", "Error releasing backgroundPlayer: ${e.message}", e)
        }
    }
}