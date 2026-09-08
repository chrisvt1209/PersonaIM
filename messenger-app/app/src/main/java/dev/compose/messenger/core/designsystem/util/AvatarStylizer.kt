package dev.compose.messenger.core.designsystem.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

private const val AVATAR_SIZE = 256

/** Resizes to a fixed square, then converts to a pure black/white ink-style avatar via Otsu threshold + Floyd-Steinberg dithering. */
fun stylizeAvatar(source: Bitmap): Bitmap {
    val resized = Bitmap.createScaledBitmap(source, AVATAR_SIZE, AVATAR_SIZE, true)
    val gray = toGrayscale(resized)

    val width = gray.width
    val height = gray.height
    val pixels = IntArray(width * height)
    gray.getPixels(pixels, 0, width, 0, 0, width, height)

    val luminance = IntArray(pixels.size) { Color.red(pixels[it]) }
    val threshold = otsuThreshold(luminance)
    val dithered = floydSteinbergDither(luminance, width, height, threshold)

    val outPixels = IntArray(pixels.size) { i ->
        if (dithered[i] == 0) Color.BLACK else Color.WHITE
    }
    val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    result.setPixels(outPixels, 0, width, 0, 0, width, height)
    return result
}

private fun toGrayscale(bitmap: Bitmap): Bitmap {
    val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val matrix = ColorMatrix().apply { setSaturation(0f) }
    val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(matrix) }
    canvas.drawBitmap(bitmap, 0f, 0f, paint)
    return output
}

/** Otsu's method: picks the luminance threshold (0..255) that maximizes between-class variance. */
fun otsuThreshold(luminance: IntArray): Int {
    val histogram = IntArray(256)
    for (value in luminance) histogram[value]++

    val total = luminance.size
    var sum = 0.0
    for (t in 0..255) sum += t * histogram[t]

    var sumBackground = 0.0
    var weightBackground = 0
    var maxVariance = 0.0
    var threshold = 0

    for (t in 0..255) {
        weightBackground += histogram[t]
        if (weightBackground == 0) continue
        val weightForeground = total - weightBackground
        if (weightForeground == 0) break

        sumBackground += t * histogram[t]
        val meanBackground = sumBackground / weightBackground
        val meanForeground = (sum - sumBackground) / weightForeground

        val variance = weightBackground.toDouble() * weightForeground *
            (meanBackground - meanForeground) * (meanBackground - meanForeground)

        // >= (not >): when several thresholds tie for the max variance - e.g. any value in an
        // empty gap between two clusters - prefer the highest one, since floydSteinbergDither
        // classifies with a strict "<", so the threshold must sit above the dark cluster's own
        // value to include it in the black class rather than sitting on top of it.
        if (variance >= maxVariance) {
            maxVariance = variance
            threshold = t
        }
    }
    return threshold
}

/** Error-diffusion dither to pure black (0) / white (255) using the classic Floyd-Steinberg weights. */
fun floydSteinbergDither(luminance: IntArray, width: Int, height: Int, threshold: Int): IntArray {
    val buffer = DoubleArray(luminance.size) { luminance[it].toDouble() }
    val output = IntArray(luminance.size)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val i = y * width + x
            val old = buffer[i]
            val new = if (old < threshold) 0 else 255
            output[i] = new
            val error = old - new

            if (x + 1 < width) buffer[i + 1] += error * 7.0 / 16.0
            if (x - 1 >= 0 && y + 1 < height) buffer[i + width - 1] += error * 3.0 / 16.0
            if (y + 1 < height) buffer[i + width] += error * 5.0 / 16.0
            if (x + 1 < width && y + 1 < height) buffer[i + width + 1] += error * 1.0 / 16.0
        }
    }
    return output
}
