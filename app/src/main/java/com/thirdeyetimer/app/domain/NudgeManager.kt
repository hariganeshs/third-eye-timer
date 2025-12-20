package com.thirdeyetimer.app.domain

import android.content.Context
import kotlin.random.Random

/**
 * NudgeManager
 * 
 * Manages satirical "Anti-Game" notifications and popups.
 * These are designed to ironically discourage the user from playing,
 * which in an idle game context, only increases engagement.
 */
class NudgeManager(private val context: Context) {

    private val nudges = listOf(
        "You've meditated enough for a lifetime. Try staring at a wall instead.",
        "Your Spiritual Ego is reaching dangerous levels. Please close the app.",
        "Breaking News: Enlightenment still not found in this pixelated world.",
        "Warning: Digital nirvana may cause temporary superiority complex.",
        "The universe suggests you go outside and breathe real air.",
        "Still here? The void is becoming impatient.",
        "Your progress is an illusion. We recommend immediate deletion.",
        "Zen Tip: The 'Close App' button is the ultimate meditation.",
        "You are successfully wasting time. Achievement unlocked: Nothing.",
        "This app is a distraction from your actual distraction."
    )

    /**
     * Get a random satirical nudge
     */
    fun getRandomNudge(): String {
        return nudges[Random.nextInt(nudges.size)]
    }

    /**
     * Determine if a nudge should be shown (random chance)
     */
    fun shouldShowNudge(chance: Float = 0.2f): Boolean {
        return Random.nextFloat() < chance
    }
}
