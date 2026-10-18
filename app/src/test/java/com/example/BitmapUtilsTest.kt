package com.example

import android.graphics.Bitmap
import android.graphics.Color
import com.example.util.BitmapUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BitmapUtilsTest {

    @Test
    fun testCreatePlaceholderBitmap() {
        val bmp = BitmapUtils.createPlaceholderBitmap(400, 300, "ABDULBASIT SPACE")
        assertNotNull(bmp)
        assertEquals(400, bmp.width)
        assertEquals(300, bmp.height)
    }

    @Test
    fun testRotateBitmap() {
        val bmp = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888)
        val rotated = BitmapUtils.rotateBitmap(bmp, 90f)
        assertEquals(100, rotated.width)
        assertEquals(200, rotated.height)
    }

    @Test
    fun testExtractPalette() {
        val bmp = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(Color.RED)
        val palette = BitmapUtils.extractPalette(bmp, 3)
        assertTrue(palette.isNotEmpty())
    }

    @Test
    fun testApplyFilterNormalReturnsSame() {
        val bmp = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
        val filtered = BitmapUtils.applyFilter(bmp, BitmapUtils.FilterType.NORMAL)
        assertEquals(bmp, filtered)
    }
}
