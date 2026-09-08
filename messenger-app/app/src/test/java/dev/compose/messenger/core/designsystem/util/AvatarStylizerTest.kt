package dev.compose.messenger.core.designsystem.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AvatarStylizerTest {

    @Test
    fun `otsuThreshold on a bimodal histogram lands above the dark cluster's own value`() {
        val darkCluster = IntArray(50) { 20 }
        val lightCluster = IntArray(50) { 220 }
        val luminance = darkCluster + lightCluster

        val threshold = otsuThreshold(luminance)

        // Every value strictly between the clusters maximizes between-class variance equally;
        // among those ties otsuThreshold prefers the highest, so it must land above 20 (in the
        // dithered picture, a strict "<" comparison needs the threshold to sit above the dark
        // cluster's own value, not on top of it, or those pixels would misclassify as white).
        assertTrue("threshold=$threshold should sit above the dark cluster's value", threshold > 20)
        assertTrue("threshold=$threshold should sit below the light cluster's value", threshold < 220)
    }

    @Test
    fun `otsuThreshold on a uniform image stays at zero`() {
        val luminance = IntArray(100) { 128 }

        val threshold = otsuThreshold(luminance)

        assertEquals(0, threshold)
    }

    @Test
    fun `floydSteinbergDither maps every pixel to pure black or white`() {
        val luminance = intArrayOf(10, 250, 90, 180, 5, 60, 200, 130, 40)
        val width = 3
        val height = 3

        val dithered = floydSteinbergDither(luminance, width, height, threshold = 128)

        assertTrue(dithered.all { it == 0 || it == 255 })
    }

    @Test
    fun `floydSteinbergDither keeps a solid dark image entirely black`() {
        val luminance = IntArray(16) { 10 }

        val dithered = floydSteinbergDither(luminance, width = 4, height = 4, threshold = 128)

        assertTrue(dithered.all { it == 0 })
    }

    @Test
    fun `floydSteinbergDither keeps a solid light image entirely white`() {
        val luminance = IntArray(16) { 245 }

        val dithered = floydSteinbergDither(luminance, width = 4, height = 4, threshold = 128)

        assertTrue(dithered.all { it == 255 })
    }
}
