package com.thirdeyetimer.app

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class AchievementPopupManager(private val context: Context) {
    
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater = LayoutInflater.from(context)
    private val handler = Handler(Looper.getMainLooper())
    
    companion object {
        private const val POPUP_DURATION = 4000L // 4 seconds
        private const val ANIMATION_DURATION = 800L
        private const val BOUNCE_DURATION = 600L
    }
    
    fun showAchievementPopup(achievementKey: String, achievementName: String) {
        // Create the popup view
        val popupView = createPopupView(achievementKey, achievementName)
        
        // Set up window parameters
        val params = WindowManager.LayoutParams().apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 100 // Distance from top
        }
        
        // Add view to window
        windowManager.addView(popupView, params)
        
        // Start entrance animation
        startEntranceAnimation(popupView)
        
        // Set up continue button
        val continueButton = popupView.findViewById<Button>(R.id.achievement_continue_button)
        continueButton.setOnClickListener {
            startExitAnimation(popupView, params)
        }
        
        // Auto-dismiss after duration
        handler.postDelayed({
            if (popupView.parent != null) {
                startExitAnimation(popupView, params)
            }
        }, POPUP_DURATION)
    }
    
    private fun createPopupView(achievementKey: String, achievementName: String): View {
        val popupView = layoutInflater.inflate(R.layout.achievement_popup, null)
        
        // Set achievement details
        val nameTextView = popupView.findViewById<TextView>(R.id.achievement_name)
        val descriptionTextView = popupView.findViewById<TextView>(R.id.achievement_description)
        val iconImageView = popupView.findViewById<ImageView>(R.id.achievement_icon)
        
        nameTextView.text = getAchievementDisplayName(achievementKey)
        descriptionTextView.text = getAchievementDescription(achievementKey)
        
        // Set icon based on achievement type
        iconImageView.setImageResource(getAchievementIcon(achievementKey))
        
        return popupView
    }
    
    private fun startEntranceAnimation(popupView: View) {
        // Initial state - off screen and scaled down
        popupView.alpha = 0f
        popupView.scaleX = 0.3f
        popupView.scaleY = 0.3f
        popupView.translationY = -200f
        
        // Create animation set
        val animatorSet = AnimatorSet()
        
        // Slide in and fade in
        val slideIn = ObjectAnimator.ofFloat(popupView, "translationY", -200f, 0f)
        val fadeIn = ObjectAnimator.ofFloat(popupView, "alpha", 0f, 1f)
        val scaleIn = ObjectAnimator.ofFloat(popupView, "scaleX", 0.3f, 1.1f)
        val scaleInY = ObjectAnimator.ofFloat(popupView, "scaleY", 0.3f, 1.1f)
        
        // Bounce effect
        val bounceX = ObjectAnimator.ofFloat(popupView, "scaleX", 1.1f, 1f)
        val bounceY = ObjectAnimator.ofFloat(popupView, "scaleY", 1.1f, 1f)
        bounceX.interpolator = BounceInterpolator()
        bounceY.interpolator = BounceInterpolator()
        
        // Glow effect on icon
        val iconView = popupView.findViewById<ImageView>(R.id.achievement_icon)
        val glowAnimator = ValueAnimator.ofFloat(0f, 1f, 0f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animator ->
                val value = animator.animatedValue as Float
                iconView.alpha = 0.7f + (value * 0.3f)
            }
        }
        
        // Play animations
        animatorSet.playTogether(slideIn, fadeIn, scaleIn, scaleInY)
        animatorSet.duration = ANIMATION_DURATION
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        
        animatorSet.start()
        
        // Play bounce after entrance
        handler.postDelayed({
            val bounceSet = AnimatorSet()
            bounceSet.playTogether(bounceX, bounceY)
            bounceSet.duration = BOUNCE_DURATION
            bounceSet.start()
        }, ANIMATION_DURATION)
        
        // Start glow animation
        glowAnimator.start()
    }
    
    private fun startExitAnimation(popupView: View, params: WindowManager.LayoutParams) {
        val animatorSet = AnimatorSet()
        
        // Slide out and fade out
        val slideOut = ObjectAnimator.ofFloat(popupView, "translationY", 0f, -200f)
        val fadeOut = ObjectAnimator.ofFloat(popupView, "alpha", 1f, 0f)
        val scaleOut = ObjectAnimator.ofFloat(popupView, "scaleX", 1f, 0.8f)
        val scaleOutY = ObjectAnimator.ofFloat(popupView, "scaleY", 1f, 0.8f)
        
        animatorSet.playTogether(slideOut, fadeOut, scaleOut, scaleOutY)
        animatorSet.duration = ANIMATION_DURATION
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        
        animatorSet.start()
        
        // Remove view after animation
        handler.postDelayed({
            try {
                if (popupView.parent != null) {
                    windowManager.removeView(popupView)
                }
            } catch (e: Exception) {
                // View might already be removed
            }
        }, ANIMATION_DURATION)
    }
    
    private fun getAchievementDisplayName(achievementKey: String): String {
        return when (achievementKey) {
            "first_hour" -> "🌅 First Hour"
            "dedicated_beginner" -> "🌟 Dedicated Beginner"
            "mindful_explorer" -> "🧘‍♀️ Mindful Explorer"
            "meditation_master" -> "👑 Meditation Master"
            "zen_sage" -> "✨ Zen Sage"
            "enlightened_one" -> "🕉️ Enlightened One"
            "consistent_practitioner" -> "🔥 Consistent Practitioner"
            "weekly_warrior" -> "⚡ Weekly Warrior"
            "monthly_master" -> "🌙 Monthly Master"
            "century_streak" -> "💎 Century Streak"
            else -> "🏅 Achievement"
        }
    }
    
    private fun getAchievementDescription(achievementKey: String): String {
        return when (achievementKey) {
            "first_hour" -> "Welcome to your meditation journey!"
            "dedicated_beginner" -> "5 hours of mindfulness completed!"
            "mindful_explorer" -> "10 hours of inner peace achieved!"
            "meditation_master" -> "25 hours of wisdom unlocked!"
            "zen_sage" -> "50 hours of enlightenment reached!"
            "enlightened_one" -> "100 hours of transcendence!"
            "consistent_practitioner" -> "3-day streak maintained!"
            "weekly_warrior" -> "7-day streak accomplished!"
            "monthly_master" -> "30-day streak achieved!"
            "century_streak" -> "100-day streak milestone!"
            else -> "Achievement unlocked!"
        }
    }
    
    private fun getAchievementIcon(achievementKey: String): Int {
        return when (achievementKey) {
            "first_hour", "dedicated_beginner", "mindful_explorer" -> R.drawable.ic_achievement_trophy
            "meditation_master", "zen_sage", "enlightened_one" -> R.drawable.ic_achievement_trophy
            "consistent_practitioner", "weekly_warrior", "monthly_master", "century_streak" -> R.drawable.ic_achievement_trophy
            else -> R.drawable.ic_achievement_trophy
        }
    }
} 