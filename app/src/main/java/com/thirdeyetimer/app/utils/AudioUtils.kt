package com.thirdeyetimer.app.utils

import android.annotation.SuppressLint
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import java.io.IOException
import kotlin.math.log10

/**
 * AudioUtils
 * 
 * Helper to monitor microphone amplitude for the Scream Jar feature.
 * Calculates decibels from amplitude.
 */
class AudioUtils {
    
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    
    @SuppressLint("MissingPermission") // Permission must be checked by caller
    fun startListening(): Boolean {
        if (isRecording) return true
        
        mediaRecorder = MediaRecorder().apply {
            try {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile("/dev/null") // Don't actually save the file
                prepare()
                start()
                isRecording = true
                return true
            } catch (e: IOException) {
                Log.e("AudioUtils", "prepare() failed", e)
                return false
            } catch (e: Exception) {
                Log.e("AudioUtils", "start() failed", e)
                return false
            }
        }
        return false
    }
    
    fun stopListening() {
        if (!isRecording) return
        
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.e("AudioUtils", "stop() failed", e)
        } finally {
            mediaRecorder = null
            isRecording = false
        }
    }
    
    fun getAmplitude(): Int {
        return try {
            mediaRecorder?.maxAmplitude ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Get decibels (approximate)
     * 0 to ~90+
     */
    fun getDb(): Double {
        val amplitude = getAmplitude()
        if (amplitude == 0) return 0.0
        
        // 32767 is max amplitude for 16-bit audio
        // Using a reference amplitude of 1 for simplicity in this context
        return 20 * log10(amplitude.toDouble())
    }
}
