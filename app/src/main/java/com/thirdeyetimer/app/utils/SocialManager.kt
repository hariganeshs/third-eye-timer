package com.thirdeyetimer.app.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object SocialManager {

    fun shareImage(context: Context, bitmap: Bitmap, text: String) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs() // don't forget to make the directory
            val stream = FileOutputStream("$cachePath/share_card.png") // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val imagePath = File(context.cacheDir, "images")
            val newFile = File(imagePath, "share_card.png")
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "com.thirdeyetimer.app.fileprovider",
                newFile
            )

            if (contentUri != null) {
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
                shareIntent.setDataAndType(contentUri, context.contentResolver.getType(contentUri))
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
                shareIntent.putExtra(Intent.EXTRA_TEXT, text)
                shareIntent.type = "image/png"
                context.startActivity(Intent.createChooser(shareIntent, "Share your Zen"))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun generateBragCardBitmap(
        context: Context,
        username: String,
        streakDays: Int,
        totalMinutes: Long,
        level: String,
        karma: Int
    ): Bitmap {
        val width = 1080
        val height = 1350 // 4:5 aspect ratio (Instagram friendly)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        // Background
        val paint = android.graphics.Paint()
        val bgGradient = android.graphics.LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            android.graphics.Color.parseColor("#0F0F23"), // BackgroundStart
            android.graphics.Color.parseColor("#312E81"), // PrimaryContainer
            android.graphics.Shader.TileMode.CLAMP
        )
        paint.shader = bgGradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Border
        val borderPaint = android.graphics.Paint()
        borderPaint.style = android.graphics.Paint.Style.STROKE
        borderPaint.strokeWidth = 20f
        borderPaint.shader = android.graphics.LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            android.graphics.Color.parseColor("#F59E0B"), // Accent
            android.graphics.Color.parseColor("#6366F1"), // Primary
            android.graphics.Shader.TileMode.CLAMP
        )
        canvas.drawRect(40f, 40f, width.toFloat() - 40f, height.toFloat() - 40f, borderPaint)

        // Title
        val textPaint = android.graphics.Paint()
        textPaint.color = android.graphics.Color.parseColor("#A5B4FC") // TextTertiary
        textPaint.textSize = 60f
        textPaint.textAlign = android.graphics.Paint.Align.CENTER
        textPaint.isAntiAlias = true
        textPaint.letterSpacing = 0.2f
        canvas.drawText("THIRD EYE TIMER", width / 2f, 200f, textPaint)

        // Level
        textPaint.textSize = 140f
        textPaint.color = android.graphics.Color.WHITE
        textPaint.letterSpacing = 0.1f
        textPaint.shader = android.graphics.LinearGradient(
            0f, 0f, width.toFloat(), 0f,
            android.graphics.Color.parseColor("#FBBF24"), // AccentLight
            android.graphics.Color.parseColor("#14B8A6"), // Secondary
            android.graphics.Shader.TileMode.CLAMP
        )
        canvas.drawText(level.uppercase(), width / 2f, 350f, textPaint)

        // Karma
        textPaint.shader = null
        textPaint.color = android.graphics.Color.parseColor("#E0E7FF") // TextSecondary
        textPaint.textSize = 80f
        textPaint.letterSpacing = 0f
        canvas.drawText("$karma Karma Points", width / 2f, 450f, textPaint)

        // Center Icon (Hollow Circle)
        val circlePaint = android.graphics.Paint()
        circlePaint.style = android.graphics.Paint.Style.STROKE
        circlePaint.strokeWidth = 15f
        circlePaint.color = android.graphics.Color.parseColor("#6366F1")
        circlePaint.isAntiAlias = true
        circlePaint.shader = android.graphics.SweepGradient(
            width / 2f, height / 2f,
            intArrayOf(
                android.graphics.Color.parseColor("#6366F1"),
                android.graphics.Color.parseColor("#14B8A6"),
                android.graphics.Color.parseColor("#F59E0B"),
                android.graphics.Color.parseColor("#6366F1")
            ),
            null
        )
        canvas.drawCircle(width / 2f, height / 2f, 250f, circlePaint)

        // Icon Text
        textPaint.textSize = 200f
        textPaint.textAlign = android.graphics.Paint.Align.CENTER
        // Note: Emojis might not render perfectly with simple Paint on all devices, but usually works
        // Use a simpler symbol if needed, or stick to text
        canvas.drawText("🧘", width / 2f, height / 2f + 70f, textPaint)

        // Stats
        drawStat(canvas, "Streak", "$streakDays Days", "🔥", width / 4f, height - 300f)
        drawStat(canvas, "Total Zen", "${totalMinutes / 60} Hours", "⏳", width * 3 / 4f, height - 300f)

        // Divider
        val dividerPaint = android.graphics.Paint()
        dividerPaint.color = android.graphics.Color.parseColor("#33FFFFFF")
        dividerPaint.strokeWidth = 5f
        canvas.drawLine(width / 2f, height - 350f, width / 2f, height - 200f, dividerPaint)

        // Footer
        textPaint.textSize = 40f
        textPaint.color = android.graphics.Color.parseColor("#6B7280") // TextMuted
        canvas.drawText("Find your inner peace with Third Eye Timer", width / 2f, height - 100f, textPaint)

        return bitmap
    }

    private fun drawStat(canvas: android.graphics.Canvas, label: String, value: String, icon: String, x: Float, y: Float) {
        val paint = android.graphics.Paint()
        paint.textAlign = android.graphics.Paint.Align.CENTER
        paint.isAntiAlias = true

        // Icon
        paint.textSize = 80f
        canvas.drawText(icon, x, y - 100f, paint)

        // Value
        paint.textSize = 90f
        paint.color = android.graphics.Color.WHITE
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        canvas.drawText(value, x, y, paint)

        // Label
        paint.textSize = 50f
        paint.color = android.graphics.Color.parseColor("#A5B4FC")
        paint.typeface = android.graphics.Typeface.DEFAULT
        canvas.drawText(label, x, y + 60f, paint)
    }
}
