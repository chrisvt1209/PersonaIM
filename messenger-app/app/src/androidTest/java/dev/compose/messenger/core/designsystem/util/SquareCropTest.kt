package dev.compose.messenger.core.designsystem.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SquareCropTest {

    @Test
    fun cropToSquare_centeredNoZoom_returnsRequestedOutputSize() {
        val source = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.RED)
        }
        val viewportSize = 100f
        val scale = coverFitScale(source.width, source.height, viewportSize)

        val result = cropToSquare(source, scale, Offset.Zero, viewportSize, outputSize = 128)

        assertEquals(128, result.width)
        assertEquals(128, result.height)
    }

    @Test
    fun cropToSquare_centeredOnUniformImage_isEntirelyThatColor() {
        val source = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.BLUE)
        }
        val viewportSize = 100f
        val scale = coverFitScale(source.width, source.height, viewportSize)

        val result = cropToSquare(source, scale, Offset.Zero, viewportSize, outputSize = 64)

        val pixels = IntArray(result.width * result.height)
        result.getPixels(pixels, 0, result.width, 0, 0, result.width, result.height)
        assertEquals(setOf(Color.BLUE), pixels.toSet())
    }

    @Test
    fun clampCropOffset_keepsBitmapCoveringTheViewport() {
        val clamped = clampCropOffset(
            offset = Offset(1000f, 1000f),
            bitmapWidth = 200,
            bitmapHeight = 200,
            totalScale = 1f,
            viewportSize = 100f
        )

        assertEquals(50f, clamped.x)
        assertEquals(50f, clamped.y)
    }
}
