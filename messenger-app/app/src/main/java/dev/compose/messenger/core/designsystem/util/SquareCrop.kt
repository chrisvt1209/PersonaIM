package dev.compose.messenger.core.designsystem.util

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.geometry.Offset

/** Smallest scale at which [bitmapWidth]x[bitmapHeight] fully covers a [viewportSize] square. */
fun coverFitScale(bitmapWidth: Int, bitmapHeight: Int, viewportSize: Float): Float {
    if (bitmapWidth <= 0 || bitmapHeight <= 0 || viewportSize <= 0f) return 1f
    return maxOf(viewportSize / bitmapWidth, viewportSize / bitmapHeight)
}

/** Clamps a pan [offset] so the bitmap (drawn at [totalScale]) keeps fully covering the [viewportSize] square. */
fun clampCropOffset(
    offset: Offset,
    bitmapWidth: Int,
    bitmapHeight: Int,
    totalScale: Float,
    viewportSize: Float
): Offset {
    val scaledWidth = bitmapWidth * totalScale
    val scaledHeight = bitmapHeight * totalScale
    val maxX = ((scaledWidth - viewportSize) / 2f).coerceAtLeast(0f)
    val maxY = ((scaledHeight - viewportSize) / 2f).coerceAtLeast(0f)
    return Offset(offset.x.coerceIn(-maxX, maxX), offset.y.coerceIn(-maxY, maxY))
}

/**
 * Renders the square region of [source] currently visible inside a [viewportSize] viewport
 * (with the bitmap centered, panned by [offset] and scaled by [totalScale]) into a new
 * [outputSize]x[outputSize] bitmap.
 */
fun cropToSquare(
    source: Bitmap,
    totalScale: Float,
    offset: Offset,
    viewportSize: Float,
    outputSize: Int = 512
): Bitmap {
    val result = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val outputScale = outputSize / viewportSize

    canvas.translate(outputSize / 2f, outputSize / 2f)
    canvas.translate(offset.x * outputScale, offset.y * outputScale)
    canvas.scale(totalScale * outputScale, totalScale * outputScale)
    canvas.drawBitmap(source, -source.width / 2f, -source.height / 2f, null)

    return result
}
