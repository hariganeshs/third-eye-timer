package com.thirdeyetimer.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

class RelaxationSummaryActivity : AppCompatActivity() {
    
    private lateinit var summaryTitle: TextView
    private lateinit var relaxationScoreValue: TextView
    private lateinit var relaxationScoreMessage: TextView
    private lateinit var startHeartRateValue: TextView
    private lateinit var endHeartRateValue: TextView
    private lateinit var heartRateChangeValue: TextView
    private lateinit var continueButton: Button
    
    companion object {
        private const val TAG = "RelaxationSummary"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_relaxation_summary)
        
        // Initialize views
        summaryTitle = findViewById(R.id.summary_title)
        relaxationScoreValue = findViewById(R.id.relaxation_score_value)
        relaxationScoreMessage = findViewById(R.id.relaxation_score_message)
        startHeartRateValue = findViewById(R.id.start_heart_rate_value)
        endHeartRateValue = findViewById(R.id.end_heart_rate_value)
        heartRateChangeValue = findViewById(R.id.heart_rate_change_value)
        continueButton = findViewById(R.id.continue_button)
        
        // Get data from intent
        val startHeartRate = intent.getIntExtra("start_heart_rate", 0)
        val endHeartRate = intent.getIntExtra("end_heart_rate", 0)
        val sessionDuration = intent.getLongExtra("session_duration", 0L)
        
        // Calculate changes
        val heartRateChange = startHeartRate - endHeartRate  // Positive = HR decreased (good)
        
        // Calculate relaxation score (0-15 scale)
        val relaxationScore = calculateRelaxationScore(
            startHeartRate, endHeartRate, sessionDuration
        )
        
        // Update UI
        updateRelaxationScore(relaxationScore)
        updateHeartRateDisplay(startHeartRate, endHeartRate, heartRateChange)
        
        // Set up continue button
        continueButton.setOnClickListener {
            finish()
        }
        
        Log.d(TAG, "Relaxation Summary: HR $startHeartRate→$endHeartRate (${heartRateChange}), Score: $relaxationScore")
    }
    
    private fun calculateRelaxationScore(
        startHR: Int, 
        endHR: Int, 
        duration: Long
    ): Int {
        // Validate input data
        if (startHR <= 0 || endHR <= 0) {
            Log.w(TAG, "Invalid heart rate data: startHR=$startHR, endHR=$endHR")
            return 0
        }
        
        var score = 0
        
        // Heart rate reduction (0-10 points)
        val hrReduction = startHR - endHR
        if (hrReduction > 0) {
            score += minOf(10, hrReduction) // Max 10 points for HR reduction
            Log.d(TAG, "HR reduction: $hrReduction BPM = +$score points")
        }
        
        // Session duration bonus (0-5 points)
        val durationMinutes = duration / 60000L
        if (durationMinutes >= 5) {
            val durationPoints = (durationMinutes / 5).toInt()
            score += minOf(5, durationPoints) // 1 point per 5 minutes
            Log.d(TAG, "Duration: ${durationMinutes} minutes = +$durationPoints points")
        }
        
        Log.d(TAG, "Total relaxation score: $score")
        return score
    }
    
    private fun updateRelaxationScore(score: Int) {
        relaxationScoreValue.text = score.toString()
        
        val message = when {
            score >= 15 -> "Excellent! You're very relaxed"
            score >= 10 -> "Great job! You're well relaxed"
            score >= 5 -> "Good progress! You're relaxed"
            score >= 1 -> "Nice start! Keep practicing"
            else -> "Keep practicing meditation"
        }
        
        relaxationScoreMessage.text = message
    }
    
    private fun updateHeartRateDisplay(startHR: Int, endHR: Int, change: Int) {
        startHeartRateValue.text = "$startHR BPM"
        endHeartRateValue.text = "$endHR BPM"
        
        val changeText = if (change > 0) {
            "-$change BPM" // Heart rate decreased (good)
        } else if (change < 0) {
            "+${abs(change)} BPM" // Heart rate increased (bad)
        } else {
            "0 BPM"
        }
        
        heartRateChangeValue.text = changeText
        
        // Color coding for heart rate change
        val color = if (change > 0) {
            android.graphics.Color.parseColor("#4CAF50") // Green for reduction
        } else if (change < 0) {
            android.graphics.Color.parseColor("#F44336") // Red for increase
        } else {
            android.graphics.Color.parseColor("#FFC107") // Yellow for no change
        }
        
        heartRateChangeValue.setTextColor(color)
    }
    

} 