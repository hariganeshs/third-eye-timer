package com.thirdeyetimer.app.utils

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import java.io.File
import java.io.IOException
import kotlin.math.log10

/**
 * AudioUtils
 * 
 * Helper to monitor microphone amplitude for the Scream Jar feature.
 * Calculates decibels from amplitude.
 */
class AudioUtils(private val context: Context? = null) {
    
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var tempFile: File? = null
    
    @SuppressLint("MissingPermission") // Permission must be checked by caller
    fun startListening(): Boolean {
        if (isRecording) return true
        
        try {
            // Create a temp file for recording (required for MediaRecorder)
            // This is needed because /dev/null doesn't work reliably on Android emulators
            tempFile = if (context != null) {
                File(context.cacheDir, "scream_temp.3gp")
            } else {
                File.createTempFile("scream_temp", ".3gp")
            }
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context!!)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(tempFile?.absolutePath)
                prepare()
                start()
                isRecording = true
                return true
            }
        } catch (e: IOException) {
            Log.e("AudioUtils", "prepare() failed", e)
            cleanup()
            return false
        } catch (e: Exception) {
            Log.e("AudioUtils", "start() failed", e)
            cleanup()
            return false
        }
        return false
    }
    
    private fun cleanup() {
        mediaRecorder?.release()
        mediaRecorder = null
        isRecording = false
        tempFile?.delete()
        tempFile = null
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
            // Clean up temp file
            tempFile?.delete()
            tempFile = null
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
