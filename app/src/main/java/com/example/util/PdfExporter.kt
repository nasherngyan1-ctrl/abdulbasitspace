package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    fun generateProjectPdf(
        context: Context,
        title: String,
        description: String,
        category: String,
        bitmap: Bitmap? = null,
        metadata: Map<String, String> = emptyMap()
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (595 x 842 pt)
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Header Background Banner
        val headerPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
        }
        canvas.drawRect(0f, 0f, 595f, 90f, headerPaint)

        // Header Title in Cyan
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#38BDF8")
            textSize = 22f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(title, 550f, 45f, titlePaint)

        // Subheader branding
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 12f
            textAlign = Paint.Align.RIGHT
        }
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("ABDULBASIT SPACE • پۆلێن: $category • ڕێکەوت: $dateStr", 550f, 70f, brandPaint)

        var currentY = 120f

        // Optional Bitmap
        if (bitmap != null) {
            val maxImgWidth = 515f
            val maxImgHeight = 260f
            val scale = minOf(maxImgWidth / bitmap.width, maxImgHeight / bitmap.height)
            val destW = bitmap.width * scale
            val destH = bitmap.height * scale
            val destX = 550f - destW

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, destW.toInt(), destH.toInt(), true)
            canvas.drawBitmap(scaledBitmap, destX, currentY, null)
            currentY += destH + 25f
        }

        // Section: Description
        val sectionTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 16f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("ناوەڕۆک و زانیارییەکان:", 550f, currentY, sectionTitlePaint)
        currentY += 24f

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 12f
            textAlign = Paint.Align.RIGHT
        }

        // Wrap text roughly for lines
        val lines = description.chunked(70)
        for (line in lines) {
            if (currentY > 780f) break
            canvas.drawText(line, 550f, currentY, bodyPaint)
            currentY += 18f
        }

        currentY += 15f

        // Section: Metadata table if present
        if (metadata.isNotEmpty() && currentY < 750f) {
            canvas.drawText("وردەکارییە تەکنیکییەکان:", 550f, currentY, sectionTitlePaint)
            currentY += 20f

            for ((key, value) in metadata) {
                if (currentY > 800f) break
                val rowText = "$key : $value"
                canvas.drawText(rowText, 550f, currentY, bodyPaint)
                currentY += 16f
            }
        }

        // Footer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("دروستکراوە لە ڕێگەی ABDULBASIT SPACE • ئەپی مۆبایلی تایبەت", 595f / 2f, 825f, footerPaint)

        document.finishPage(page)

        val dir = File(context.filesDir, "exported_pdfs")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "doc_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }
}
