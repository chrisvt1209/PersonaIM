package dev.compose.messenger.core.designsystem.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import dev.compose.messenger.core.designsystem.util.clampCropOffset
import dev.compose.messenger.core.designsystem.util.coverFitScale
import dev.compose.messenger.core.designsystem.util.cropToSquare

private const val MAX_USER_SCALE = 4f

/** Pan/pinch-zoom state for cropping [bitmap] into the square region visible in a [viewportSizePx] viewport. */
class SquareCropState(val bitmap: Bitmap, val viewportSizePx: Float) {
    private val baseScale = coverFitScale(bitmap.width, bitmap.height, viewportSizePx)

    var userScale by mutableFloatStateOf(1f)
        private set
    var offset by mutableStateOf(Offset.Zero)
        private set

    val totalScale: Float get() = baseScale * userScale

    fun onGesture(pan: Offset, zoom: Float) {
        userScale = (userScale * zoom).coerceIn(1f, MAX_USER_SCALE)
        offset = clampCropOffset(offset + pan, bitmap.width, bitmap.height, totalScale, viewportSizePx)
    }

    fun cropResult(outputSize: Int = 512): Bitmap =
        cropToSquare(bitmap, totalScale, offset, viewportSizePx, outputSize)
}

@Composable
fun rememberSquareCropState(bitmap: Bitmap, viewportSizePx: Float): SquareCropState =
    remember(bitmap, viewportSizePx) { SquareCropState(bitmap, viewportSizePx) }

/** Square pan/pinch-zoom crop viewport over [state]'s bitmap. */
@Composable
fun SquareCropCanvas(state: SquareCropState, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    Box(
        modifier = modifier
            .size(with(density) { state.viewportSizePx.toDp() })
            .clipToBounds()
            .pointerInput(state) {
                detectTransformGestures { _, pan, zoom, _ ->
                    state.onGesture(pan, zoom)
                }
            }
    ) {
        Image(
            bitmap = state.bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    scaleX = state.totalScale
                    scaleY = state.totalScale
                    translationX = state.offset.x
                    translationY = state.offset.y
                }
        )
    }
}
