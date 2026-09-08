package dev.compose.messenger.core.designsystem.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AvatarStylizerInstrumentedTest {

    @Test
    fun stylizeAvatar_producesA256SquareOfPureBlackAndWhitePixels() {
        val source = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).apply {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val gray = if (x < width / 2) 30 else 220
                    setPixel(x, y, Color.rgb(gray, gray, gray))
                }
            }
        }

        val result = stylizeAvatar(source)

        assertEquals(256, result.width)
        assertEquals(256, result.height)

        val pixels = IntArray(result.width * result.height)
        result.getPixels(pixels, 0, result.width, 0, 0, result.width, result.height)
        assertTrue(pixels.all { it == Color.BLACK || it == Color.WHITE })
    }
}
