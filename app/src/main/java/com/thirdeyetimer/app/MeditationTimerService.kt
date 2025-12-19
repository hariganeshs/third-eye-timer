package com.thirdeyetimer.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.util.Log
import android.content.pm.ServiceInfo
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

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
        const val TIMER_UPDATE_DURATION_ACTION = "com.thirdeyetimer.app.TIMER_UPDATE_DURATION"
        const val EXTRA_REMAINING_TIME = "EXTRA_REMAINING_TIME"
    }

    private var countdownTimer: CountDownTimer? = null
    private var exoPlayer: ExoPlayer? = null
    private var bellPlayer: ExoPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        // Initialize ExoPlayers
        exoPlayer = ExoPlayer.Builder(this).build()
        bellPlayer = ExoPlayer.Builder(this).build()
    }

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
            startForeground(1, notification)
        }

        // Stop any previous playback
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems()
        
        // Priority: guided meditation first, then background sound
        val soundToPlay = if (guidedResId != 0) guidedResId else backgroundResId
        var durationToUse = timeMillis
        
        if (soundToPlay != 0) {
            try {
                Log.d("MeditationTimerService", "Starting ExoPlayer for resource: $soundToPlay (guided: ${guidedResId != 0})")
                
                val uri = "android.resource://$packageName/$soundToPlay"
                val mediaItem = MediaItem.fromUri(uri)
                
                exoPlayer?.setMediaItem(mediaItem)
                
                // Set AudioAttributes
                val contentType = if (guidedResId != 0) C.AUDIO_CONTENT_TYPE_SPEECH else C.AUDIO_CONTENT_TYPE_MUSIC
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(contentType)
                    .build()
                exoPlayer?.setAudioAttributes(audioAttributes, true)
                
                // Looping logic
                if (guidedResId != 0) {
                    exoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
                } else {
                    // Ambient sounds loop
                    if (shouldLoopBackgroundSound(soundToPlay)) {
                        exoPlayer?.repeatMode = Player.REPEAT_MODE_ONE
                    } else {
                        exoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
                    }
                }
                
                exoPlayer?.prepare()
                

                
                // No need to sync duration anymore - we trust the metadata duration
                // The audio will simply end when it ends, and silence/ambient will continue
                // until the timer finishes.
                
                exoPlayer?.play()
                Log.d("MeditationTimerService", "ExoPlayer started")
            } catch (e: Exception) {
                Log.e("MeditationTimerService", "Error playing background/guided sound: ${e.message}", e)
            }
        } else {
            Log.d("MeditationTimerService", "No background or guided meditation sound selected (silent meditation)")
        }

        // Initial timer start (will be restarted if duration update happens)
        startCountdownTimer(timeMillis, bellResId)
    }

    private fun startCountdownTimer(durationMillis: Long, bellResId: Int) {
        countdownTimer?.cancel()
        countdownTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val intent = Intent(TIMER_TICK_ACTION)
                intent.putExtra(EXTRA_REMAINING_TIME, millisUntilFinished)
                LocalBroadcastManager.getInstance(this@MeditationTimerService).sendBroadcast(intent)
            }
            override fun onFinish() {
                Log.d("MeditationTimerService", "Timer finished! Playing bell sound...")
                playBell(bellResId)
                
                exoPlayer?.stop()
                
                val intent = Intent(TIMER_FINISHED_ACTION)
                LocalBroadcastManager.getInstance(this@MeditationTimerService).sendBroadcast(intent)
                
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    stopSelf()
                }, 3000)
            }
        }.start()
    }

    private fun playBell(bellResId: Int) {
        Log.d("MeditationTimerService", "Attempting to play bell sound with ExoPlayer: $bellResId")
        try {
            bellPlayer?.stop()
            bellPlayer?.clearMediaItems()
            
            val soundId = if (bellResId != 0) bellResId else R.raw.bell_1
            val uri = "android.resource://$packageName/$soundId"
            val mediaItem = MediaItem.fromUri(uri)
            
            bellPlayer?.setMediaItem(mediaItem)
            bellPlayer?.prepare()
            bellPlayer?.play()
        } catch (e: Exception) {
            Log.e("MeditationTimerService", "Error playing bell sound: ${e.message}", e)
        }
    }

    private fun shouldLoopBackgroundSound(resId: Int): Boolean {
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
        
        exoPlayer?.release()
        exoPlayer = null
        
        bellPlayer?.release()
        bellPlayer = null
    }
}

