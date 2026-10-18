package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

object BitmapUtils {

    enum class FilterType(val kurdishName: String) {
        NORMAL("ئاسایی"),
        GRAYSCALE("ڕەش و سپی"),
        SEPIA("سێپیا - کۆن"),
        INVERT("پێچەوانەکردنەوە"),
        CYBERPUNK("سایبەرپەنک - شین و وەنەوشەیی"),
        WARM_GLOW("گەرم - ڕۆژئاوابوون"),
        COOL_NIGHT("شەوەزەنگ - سارد"),
        DRAMATIC("کۆنتراستی بەرز")
    }

    fun applyColorAdjustments(
        src: Bitmap,
        brightness: Float, // -100 to 100
        contrast: Float,   // 0.5 to 2.0
        saturation: Float  // 0.0 to 2.0
    ): Bitmap {
        val result = Bitmap.createBitmap(src.width, src.height, src.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Saturation
        val satMatrix = ColorMatrix().apply { setSaturation(saturation) }

        // 2. Contrast & Brightness
        // formula: scale = contrast; translate = (1 - contrast) * 128 + brightness
        val scale = contrast
        val translate = (1f - contrast) * 128f + brightness
        val cbMatrix = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )

        val finalMatrix = ColorMatrix().apply {
            postConcat(satMatrix)
            postConcat(cbMatrix)
        }

        paint.colorFilter = ColorMatrixColorFilter(finalMatrix)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return result
    }

    fun applyFilter(src: Bitmap, filter: FilterType): Bitmap {
        if (filter == FilterType.NORMAL) return src

        val result = Bitmap.createBitmap(src.width, src.height, src.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val matrix = when (filter) {
            FilterType.NORMAL -> ColorMatrix()
            FilterType.GRAYSCALE -> ColorMatrix().apply { setSaturation(0f) }
            FilterType.SEPIA -> ColorMatrix().apply {
                set(
                    floatArrayOf(
                        0.393f, 0.769f, 0.189f, 0f, 0f,
                        0.349f, 0.686f, 0.168f, 0f, 0f,
                        0.272f, 0.534f, 0.131f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            FilterType.INVERT -> ColorMatrix(
                floatArrayOf(
                    -1f, 0f, 0f, 0f, 255f,
                    0f, -1f, 0f, 0f, 255f,
                    0f, 0f, -1f, 0f, 255f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterType.CYBERPUNK -> ColorMatrix(
                floatArrayOf(
                    1.2f, 0f, 0.4f, 0f, 20f,
                    0f, 0.8f, 0.2f, 0f, -10f,
                    0.5f, 0.2f, 1.5f, 0f, 40f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterType.WARM_GLOW -> ColorMatrix(
                floatArrayOf(
                    1.3f, 0f, 0f, 0f, 30f,
                    0f, 1.1f, 0f, 0f, 15f,
                    0f, 0f, 0.8f, 0f, -20f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterType.COOL_NIGHT -> ColorMatrix(
                floatArrayOf(
                    0.8f, 0f, 0f, 0f, -20f,
                    0f, 0.9f, 0f, 0f, 0f,
                    0f, 0f, 1.4f, 0f, 35f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            FilterType.DRAMATIC -> ColorMatrix(
                floatArrayOf(
                    1.4f, 0f, 0f, 0f, -30f,
                    0f, 1.4f, 0f, 0f, -30f,
                    0f, 0f, 1.4f, 0f, -30f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }

        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return result
    }

    fun rotateBitmap(src: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }

    fun flipBitmap(src: Bitmap, horizontal: Boolean): Bitmap {
        val matrix = Matrix().apply {
            if (horizontal) postScale(-1f, 1f, src.width / 2f, src.height / 2f)
            else postScale(1f, -1f, src.width / 2f, src.height / 2f)
        }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }

    fun addTextOverlay(
        src: Bitmap,
        text: String,
        textColor: Int = Color.WHITE,
        textSize: Float = 48f,
        addBackgroundBanner: Boolean = true
    ): Bitmap {
        val result = src.copy(src.config ?: Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            this.textSize = textSize
            textAlign = Paint.Align.CENTER
            setShadowLayer(4f, 2f, 2f, Color.BLACK)
        }

        val x = canvas.width / 2f
        val y = canvas.height * 0.85f

        if (addBackgroundBanner) {
            val bounds = Rect()
            paint.getTextBounds(text, 0, text.length, bounds)
            val padding = 24f
            val bgPaint = Paint().apply {
                color = Color.parseColor("#99000000")
            }
            canvas.drawRect(
                x - bounds.width() / 2f - padding,
                y - bounds.height() - padding,
                x + bounds.width() / 2f + padding,
                y + padding,
                bgPaint
            )
        }

        canvas.drawText(text, x, y, paint)
        return result
    }

    fun extractPalette(bitmap: Bitmap, count: Int = 5): List<Int> {
        // Downscale bitmap for fast sampling
        val small = Bitmap.createScaledBitmap(bitmap, 30, 30, false)
        val colors = mutableMapOf<Int, Int>()

        for (x in 0 until small.width step 2) {
            for (y in 0 until small.height step 2) {
                val pixel = small.getPixel(x, y)
                // Discard transparent
                if (Color.alpha(pixel) > 200) {
                    // Quantize colors by rounding RGB to nearest 32
                    val r = (Color.red(pixel) / 32) * 32
                    val g = (Color.green(pixel) / 32) * 32
                    val b = (Color.blue(pixel) / 32) * 32
                    val quantized = Color.rgb(r, g, b)
                    colors[quantized] = (colors[quantized] ?: 0) + 1
                }
            }
        }

        return colors.entries.sortedByDescending { it.value }
            .take(count)
            .map { it.key }
            .ifEmpty { listOf(Color.BLACK, Color.DKGRAY, Color.GRAY, Color.LTGRAY, Color.WHITE) }
    }

    fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String = "space_${System.currentTimeMillis()}.png"): File {
        val dir = File(context.filesDir, "saved_images")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }

    fun createPlaceholderBitmap(width: Int = 800, height: Int = 600, label: String = "ABDULBASIT SPACE"): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#0F172A"))

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#38BDF8")
            textSize = 40f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(label, width / 2f, height / 2f, paint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("وێنەی بنەڕەتی بارکراو بۆ دەستکاریکردن", width / 2f, height / 2f + 50f, subPaint)

        return bitmap
    }
}
