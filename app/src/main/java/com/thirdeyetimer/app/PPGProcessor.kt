package com.thirdeyetimer.app

import android.util.Log
import kotlin.math.*
import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType

/**
 * Professional PPG (Photoplethysmography) signal processor
 * Updated: Lower sample rate to 3Hz (log intervals ~300ms), min samples 30, use green if R saturated, amplify signal, loosen peaks, add timestamps for intervals
 */
class PPGProcessor {

    companion object {
        private const val TAG = "PPGProcessor"

        // Signal processing parameters - UPDATED
        private const val SAMPLE_RATE = 3.0 // Hz (based on ~300-400ms log intervals)
        private const val MIN_HEART_RATE = 30.0 // BPM - Lower for very slow heart rates
        private const val MAX_HEART_RATE = 150.0 // BPM - Higher for very fast heart rates
        private const val MIN_SAMPLES = 30 // 10 seconds at 3 Hz - Start processing earlier
        private const val FFT_SIZE = 512 // Power of 2 for efficient FFT

        // Filter parameters
        private const val LOW_CUTOFF = 0.67 // Hz (40 BPM)
        private const val HIGH_CUTOFF = 2.0 // Hz (120 BPM)

        // Peak detection parameters - Very sensitive for better detection
        private const val MIN_PEAK_HEIGHT_RATIO = 0.05 // Very low for maximum sensitivity
        private const val MIN_PEAK_DISTANCE = 2 // Minimum 2 samples between peaks (~0.7 seconds at 3Hz)
        private const val PEAK_PROMINENCE_RATIO = 0.05 // Very low for maximum sensitivity

        // Saturation threshold for channel switch
        private const val SATURATION_THRESHOLD = 220.0 // If mean R > this, switch to green (overexposed red)
        private const val LOW_SIGNAL_THRESHOLD = 50.0 // If mean R < this, average R+G for better signal
        private const val SIGNAL_AMPLIFICATION = 50.0 // Increased for low variation

        // Interval validation
        private const val MIN_INTERVAL = (60000 / MAX_HEART_RATE).toLong() // ~400ms for 150 BPM
        private const val MAX_INTERVAL = (60000 / MIN_HEART_RATE).toLong() // ~2000ms for 30 BPM
    }

    private val rawRed = mutableListOf<Double>() // Red values
    private val rawGreen = mutableListOf<Double>() // Green values for fallback
    private val timestamps = mutableListOf<Long>() // Timestamp per sample for accurate intervals
    private val filteredSignal = mutableListOf<Double>()
    private val heartRateHistory = mutableListOf<Int>()

    private var signalQuality = 0.0
    private var noiseLevel = 0.0
    private var signalStrength = 0.0

    // FFT transformer
    private val fftTransformer = FastFourierTransformer(DftNormalization.STANDARD)

    /**
     * Add new PPG sample from RGB frame
     */
    fun addSample(red: Double, green: Double, blue: Double): PPGResult {
        val currentTime = System.currentTimeMillis()
        rawRed.add(red)
        rawGreen.add(green)
        timestamps.add(currentTime)
        Log.d(TAG, "Added sample: R=$red, G=$green, B=$blue, time=$currentTime, total samples=${rawRed.size}")

        // Keep buffer size manageable
        if (rawRed.size > FFT_SIZE * 2) {
            rawRed.removeAt(0)
            rawGreen.removeAt(0)
            timestamps.removeAt(0)
        }

        // Process signal if we have enough samples
        return if (rawRed.size >= MIN_SAMPLES) {
            processSignal()
        } else {
            PPGResult(
                heartRate = 0,
                hrv = 0.0,
                quality = 0.0,
                samples = rawRed.size,
                isStable = false
            )
        }
    }

    /**
     * Legacy method for backward compatibility
     */
    fun addSample(ppgValue: Double): PPGResult {
        return addSample(ppgValue, ppgValue, ppgValue)
    }

    /**
     * Main signal processing pipeline with professional techniques
     */
    private fun processSignal(): PPGResult {
        try {
            // Decide channel based on saturation or low signal
            val meanRed = rawRed.average()
            val rawSignal = when {
                meanRed > SATURATION_THRESHOLD -> {
                    Log.w(TAG, "Red channel saturated (mean=$meanRed) - switching to green")
                    rawGreen
                }
                meanRed < LOW_SIGNAL_THRESHOLD -> {
                    Log.w(TAG, "Red channel underexposed (mean=$meanRed) - averaging R+G")
                    rawRed.mapIndexed { index, r -> (r + rawGreen[index]) / 2.0 }
                }
                else -> rawRed
            }

            // Log raw variance for debug
            val rawMean = rawSignal.average()
            val rawVariance = rawSignal.map { (it - rawMean) * (it - rawMean) }.average()
            Log.d(TAG, "Raw signal variance: $rawVariance, mean: $rawMean")

            // 1. Apply proper Butterworth bandpass filter
            var filtered = applyButterworthBandpassFilter(rawSignal)
            // Amplify for low variation
            filtered = filtered.map { it * SIGNAL_AMPLIFICATION }
            filteredSignal.clear()
            filteredSignal.addAll(filtered)
            Log.d(TAG, "Filtered signal size: ${filtered.size}, min=${filtered.minOrNull()}, max=${filtered.maxOrNull()}, avg=${filtered.average()}")

            // 2. Calculate signal quality metrics
            calculateSignalQuality()
            if (signalQuality < 0.1 && rawRed.size > 60) {
                Log.w(TAG, "Low signal quality - check finger placement, cover camera/flash fully, or adjust exposure in camera setup")
            }

            // Local vars for this window
            val localBeatIntervals = mutableListOf<Long>()
            val localPeakTimes = mutableListOf<Long>()

            // 3. Detect heart rate using time-domain peak detection
            val timeDomainHR = detectHeartRateTimeDomain(localBeatIntervals, localPeakTimes)
            if (timeDomainHR == 0) Log.w(TAG, "No HR detected in time domain - no peaks found")

            // 4. Detect heart rate using frequency-domain FFT analysis
            val freqDomainHR = detectHeartRateFrequencyDomain()
            if (freqDomainHR == 0) Log.w(TAG, "No HR detected in frequency domain - no dominant freq")

            // 5. Combine estimates using weighted average
            var finalHeartRate = combineHeartRateEstimates(timeDomainHR, freqDomainHR).toDouble()

            // 6. Sanity check: ensure heart rate is within realistic bounds
            if (finalHeartRate < MIN_HEART_RATE || finalHeartRate > MAX_HEART_RATE) {
                Log.w(TAG, "Sanity check failed: heart rate $finalHeartRate BPM is outside realistic range")
                finalHeartRate = 0.0
            }

            // Add to history if valid
            if (finalHeartRate > 0) {
                heartRateHistory.add(finalHeartRate.toInt())
                if (heartRateHistory.size > 10) heartRateHistory.removeAt(0)
            }

            // 7. Calculate HRV using RMSSD from local intervals
            val hrv = calculateHRV(localBeatIntervals)

            // 8. Determine stability
            val isStable = heartRateHistory.size >= 5 && signalQuality > 0.7

            return PPGResult(
                heartRate = finalHeartRate.toInt(),
                hrv = hrv,
                quality = signalQuality,
                samples = rawRed.size,
                isStable = isStable
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error processing PPG signal: ${e.message}")
            return PPGResult(
                heartRate = 0,
                hrv = 0.0,
                quality = 0.0,
                samples = rawRed.size,
                isStable = false
            )
        }
    }

    /**
     * Apply Butterworth bandpass filter using approximation with Apache Commons Math
     */
    private fun applyButterworthBandpassFilter(rawSignal: List<Double>): List<Double> {
        if (rawSignal.size < 10) return rawSignal

        try {
            // Remove DC component first
            val mean = rawSignal.average()
            val detrended = rawSignal.map { it - mean }

            // Apply low-pass filter
            val lowPassFiltered = applyLowPassFilter(detrended, HIGH_CUTOFF)

            // Apply high-pass filter
            val bandPassFiltered = applyHighPassFilter(lowPassFiltered, LOW_CUTOFF)

            // Apply additional smoothing for noise reduction
            return applySmoothingFilter(bandPassFiltered)

        } catch (e: Exception) {
            Log.e(TAG, "Error applying bandpass filter: ${e.message}")
            return rawSignal
        }
    }

    /**
     * Apply low-pass filter using Butterworth approximation
     */
    private fun applyLowPassFilter(signal: List<Double>, cutoffFreq: Double): List<Double> {
        if (signal.size < 3) return signal

        val filtered = mutableListOf<Double>()
        filtered.add(signal[0]) // First sample unchanged

        // Simple Butterworth-like filter (first order approximation)
        val alpha = 1.0 / (1.0 + 2.0 * PI * cutoffFreq / SAMPLE_RATE)

        for (i in 1 until signal.size) {
            val filteredValue = alpha * signal[i] + (1.0 - alpha) * filtered[i-1]
            filtered.add(filteredValue)
        }

        return filtered
    }

    /**
     * Apply high-pass filter using Butterworth approximation
     */
    private fun applyHighPassFilter(signal: List<Double>, cutoffFreq: Double): List<Double> {
        if (signal.size < 3) return signal

        val filtered = mutableListOf<Double>()
        filtered.add(signal[0]) // First sample unchanged

        // Simple Butterworth-like filter (first order approximation)
        val alpha = 1.0 / (1.0 + 2.0 * PI * cutoffFreq / SAMPLE_RATE)

        for (i in 1 until signal.size) {
            val filteredValue = alpha * (filtered[i-1] + signal[i] - signal[i-1])
            filtered.add(filteredValue)
        }

        return filtered
    }

    /**
     * Apply smoothing filter to reduce high-frequency noise
     */
    private fun applySmoothingFilter(signal: List<Double>): List<Double> {
        if (signal.size < 3) return signal

        val smoothed = mutableListOf<Double>()
        smoothed.add(signal[0]) // First sample

        // Apply 3-point moving average
        for (i in 1 until signal.size - 1) {
            val smoothedValue = (signal[i-1] + signal[i] + signal[i+1]) / 3.0
            smoothed.add(smoothedValue)
        }

        smoothed.add(signal.last()) // Last sample
        return smoothed
    }

    /**
     * Calculate signal quality metrics using professional standards
     * UPDATED: Made more sensitive by lowering divisor
     */
    private fun calculateSignalQuality() {
        if (filteredSignal.size < 20) return

        try {
            // Use more samples for better quality assessment
            val recentSignal = filteredSignal.takeLast(75) // ~25 seconds at 3Hz

            // Calculate signal strength (standard deviation)
            val mean = recentSignal.average()
            val variance = recentSignal.map { (it - mean) * (it - mean) }.average()
            signalStrength = sqrt(variance)

            // Calculate noise level (high-frequency components)
            val differences = mutableListOf<Double>()
            for (i in 1 until recentSignal.size) {
                differences.add(abs(recentSignal[i] - recentSignal[i-1]))
            }
            noiseLevel = differences.average()

            // Calculate signal quality (SNR-like metric) - Made more sensitive
            signalQuality = if (noiseLevel > 0.0) {
                minOf(1.0, signalStrength / (noiseLevel * 1.0)) // Lowered to 1.0 for more sensitivity
            } else {
                0.0
            }

            // Additional quality check: check for motion artifacts
            val motionArtifact = detectMotionArtifacts(recentSignal)
            if (motionArtifact) {
                signalQuality *= 0.5 // Reduce quality if motion detected
            }

            Log.d(TAG, "Signal Quality: ${String.format("%.3f", signalQuality)}, Strength: ${String.format("%.3f", signalStrength)}, Noise: ${String.format("%.3f", noiseLevel)}, Samples: ${rawRed.size}")

        } catch (e: Exception) {
            Log.e(TAG, "Error calculating signal quality: ${e.message}")
            signalQuality = 0.0
        }
    }

    /**
     * Detect motion artifacts in the signal
     */
    private fun detectMotionArtifacts(signal: List<Double>): Boolean {
        if (signal.size < 20) return false

        // Calculate signal variability
        val mean = signal.average()
        val variance = signal.map { (it - mean) * (it - mean) }.average()
        val stdDev = sqrt(variance)

        // Check for sudden large changes (motion artifacts)
        var largeChanges = 0
        for (i in 1 until signal.size) {
            if (abs(signal[i] - signal[i-1]) > stdDev * 3) {
                largeChanges++
            }
        }

        // If more than 10% of samples have large changes, consider it motion
        return (largeChanges.toDouble() / signal.size) > 0.1
    }

    /**
     * Detect heart rate using time-domain peak detection with professional algorithm
     */
    private fun detectHeartRateTimeDomain(localBeatIntervals: MutableList<Long>, localPeakTimes: MutableList<Long>): Int {
        if (filteredSignal.size < 15) return 0 // Need at least 5 seconds at 3Hz

        try {
            // Use recent signal for peak detection
            val recentSignal = filteredSignal.takeLast(75) // ~25 seconds at 3Hz
            val recentTimestamps = timestamps.takeLast(75) // Corresponding timestamps
            val signalRange = recentSignal.maxOrNull()!! - recentSignal.minOrNull()!!
            val minPeakHeight = recentSignal.average() + (signalRange * MIN_PEAK_HEIGHT_RATIO)

            // Find peaks
            val peaks = findPeaksWithProminence(recentSignal, minPeakHeight, MIN_PEAK_DISTANCE)
            Log.d(TAG, "Detected ${peaks.size} peaks in time domain")

            // Get sorted peak times
            val peakTimePairs = peaks.map { recentTimestamps[it] }.sorted()

            if (peakTimePairs.size >= 2) {
                for (i in 1 until peakTimePairs.size) {
                    val interval = peakTimePairs[i] - peakTimePairs[i-1]
                    if (interval > 0 && interval >= MIN_INTERVAL && interval <= MAX_INTERVAL) {
                        localBeatIntervals.add(interval)
                        localPeakTimes.add(peakTimePairs[i])  // Add the second peak time
                    } else {
                        Log.d(TAG, "Rejecting invalid interval: ${interval}ms")
                    }
                }
            }

            if (localBeatIntervals.isEmpty()) return 0

            val localBpmList = localBeatIntervals.map { 60000 / it.toInt() }

                         heartRateHistory.addAll(localBpmList)
             if (heartRateHistory.size > 20) {
                 val temp = heartRateHistory.takeLast(20).toMutableList()
                 heartRateHistory.clear()
                 heartRateHistory.addAll(temp)
             }

            return localBpmList.average().toInt()

        } catch (e: Exception) {
            Log.e(TAG, "Error in time-domain heart rate detection: ${e.message}")
            return 0
        }
    }

    /**
     * Find peaks in signal using professional algorithm with prominence
     * IMPROVED: Added prominence calculation for better peak detection
     */
    private fun findPeaksWithProminence(signal: List<Double>, minHeight: Double, minDistance: Int): List<Int> {
        val peaks = mutableListOf<Int>()

        for (i in 1 until signal.size - 1) {
            if (signal[i] > signal[i-1] && signal[i] > signal[i+1] && signal[i] > minHeight) {
                // Calculate peak prominence
                val prominence = calculatePeakProminence(signal, i)
                val signalRange = signal.maxOrNull()!! - signal.minOrNull()!!

                if (prominence > signalRange * PEAK_PROMINENCE_RATIO) {
                    // Check minimum distance from previous peak
                    if (peaks.isEmpty() || (i - peaks.last()) >= minDistance) {
                        peaks.add(i)
                    }
                }
            }
        }

        return peaks
    }

    /**
     * Calculate peak prominence (height relative to surrounding valleys)
     */
    private fun calculatePeakProminence(signal: List<Double>, peakIndex: Int): Double {
        if (peakIndex <= 0 || peakIndex >= signal.size - 1) return 0.0

        val peakValue = signal[peakIndex]

        // Find left valley
        var leftValley = peakValue
        for (i in peakIndex - 1 downTo maxOf(0, peakIndex - 20)) {
            if (signal[i] < leftValley) {
                leftValley = signal[i]
            }
        }

        // Find right valley
        var rightValley = peakValue
        for (i in peakIndex + 1 until minOf(signal.size, peakIndex + 20)) {
            if (signal[i] < rightValley) {
                rightValley = signal[i]
            }
        }

        // Prominence is the minimum of left and right drops
        return peakValue - max(leftValley, rightValley)
    }

    /**
     * Detect heart rate using frequency-domain FFT analysis with Apache Commons Math
     * IMPROVED: Better frequency resolution and peak finding
     */
    private fun detectHeartRateFrequencyDomain(): Int {
        if (filteredSignal.size < FFT_SIZE / 2) return 0

        try {
            // Prepare signal for FFT
            val signal = filteredSignal.takeLast(FFT_SIZE).toMutableList()
            while (signal.size < FFT_SIZE) {
                signal.add(0.0)
            }

            // Convert to array for FFT
            val signalArray = signal.toDoubleArray()

            // Apply window function to reduce spectral leakage
            val windowedSignal = applyHammingWindow(signalArray)

            // Perform FFT
            val fftResult = fftTransformer.transform(windowedSignal, TransformType.FORWARD)

            // Find dominant frequency in heart rate range
            val dominantFreq = findDominantFrequencyImproved(fftResult)

            // Convert frequency to BPM
            val bpm = dominantFreq * 60.0

            return if (bpm >= MIN_HEART_RATE && bpm <= MAX_HEART_RATE) {
                bpm.toInt()
            } else {
                0
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in FFT analysis: ${e.message}")
            return 0
        }
    }

    /**
     * Apply Hamming window to reduce spectral leakage
     */
    private fun applyHammingWindow(signal: DoubleArray): DoubleArray {
        val windowed = DoubleArray(signal.size)
        for (i in signal.indices) {
            val window = 0.54 - 0.46 * cos(2.0 * PI * i / (signal.size - 1))
            windowed[i] = signal[i] * window
        }
        return windowed
    }

    /**
     * Find dominant frequency in heart rate range - IMPROVED
     * Better peak finding with local maxima detection
     */
    private fun findDominantFrequencyImproved(fftResult: Array<Complex>): Double {
        var maxMagnitude = 0.0
        var dominantFreq = 0.0

        // Only check frequencies in heart rate range
        val lowBin = (LOW_CUTOFF * FFT_SIZE / SAMPLE_RATE).toInt()
        val highBin = (HIGH_CUTOFF * FFT_SIZE / SAMPLE_RATE).toInt()

        // Find local maxima in the frequency range
        val magnitudes = mutableListOf<Pair<Int, Double>>()

        for (i in lowBin..highBin) {
            if (i < fftResult.size / 2) { // Only check first half (real frequencies)
                val magnitude = fftResult[i].abs()
                magnitudes.add(Pair(i, magnitude))
            }
        }

        // Find the highest local maximum with minimum threshold
        val avgMagnitude = magnitudes.map { it.second }.average()
        val minThreshold = avgMagnitude * 1.5 // Minimum threshold for peak detection

        for (i in 1 until magnitudes.size - 1) {
            if (magnitudes[i].second > magnitudes[i-1].second &&
                magnitudes[i].second > magnitudes[i+1].second &&
                magnitudes[i].second > minThreshold) {
                if (magnitudes[i].second > maxMagnitude) {
                    maxMagnitude = magnitudes[i].second
                    dominantFreq = magnitudes[i].first.toDouble() * SAMPLE_RATE / FFT_SIZE
                }
            }
        }

        return dominantFreq
    }

    /**
     * Combine time-domain and frequency-domain estimates
     */
    private fun combineHeartRateEstimates(timeDomainHR: Int, freqDomainHR: Int): Int {
        return when {
            timeDomainHR > 0 && freqDomainHR > 0 -> {
                // Weighted average based on signal quality
                val timeWeight = signalQuality
                val freqWeight = 1.0 - signalQuality
                (timeDomainHR * timeWeight + freqDomainHR * freqWeight).toInt()
            }
            timeDomainHR > 0 -> timeDomainHR
            freqDomainHR > 0 -> freqDomainHR
            else -> 0
        }
         }
 
     /**
      * Calculate Heart Rate Variability (RMSSD) - professional standard
      */
     private fun calculateHRV(beatIntervals: List<Long>): Double {
         if (beatIntervals.size < 5) return 0.0 // Need at least 5 intervals for reliable HRV
 
         try {
             val differences = mutableListOf<Double>()
             for (i in 1 until beatIntervals.size) {
                 differences.add((beatIntervals[i] - beatIntervals[i-1]).toDouble())
             }
 
             // Calculate RMSSD (Root Mean Square of Successive Differences)
             val rmssd = sqrt(differences.map { it * it }.average())
             return rmssd
 
         } catch (e: Exception) {
             Log.e(TAG, "Error calculating HRV: ${e.message}")
             return 0.0
         }
     }
 
     fun clear() {
        rawRed.clear()
        rawGreen.clear()
        timestamps.clear()
        filteredSignal.clear()
        heartRateHistory.clear()
        signalQuality = 0.0
        noiseLevel = 0.0
        signalStrength = 0.0
    }

    fun getSignalStats(): SignalStats {
        return SignalStats(
            samples = rawRed.size,
            quality = signalQuality,
            strength = signalStrength,
            noise = noiseLevel,
            heartRateHistory = heartRateHistory.toList(),
            beatIntervals = listOf()
        )
    }
}

/**
 * Result of PPG processing
 */
data class PPGResult(
    val heartRate: Int,
    val hrv: Double,
    val quality: Double,
    val samples: Int,
    val isStable: Boolean
)

/**
 * Signal statistics for debugging
 */
data class SignalStats(
    val samples: Int,
    val quality: Double,
    val strength: Double,
    val noise: Double,
    val heartRateHistory: List<Int>,
    val beatIntervals: List<Long>
)