package dev.compose.messenger.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.compose.messenger.core.designsystem.theme.AnnColor
import dev.compose.messenger.core.designsystem.theme.PersonaRed
import dev.compose.messenger.core.designsystem.theme.RyujiColor
import dev.compose.messenger.core.designsystem.theme.YusukeColor
import dev.compose.messenger.core.designsystem.util.asOutline
import kotlin.math.abs
import kotlin.math.roundToInt

private val NativeAvatarSize = DpSize(110.dp, 90.dp)
private val AvatarPalette = listOf(PersonaRed, AnnColor, RyujiColor, YusukeColor)

/** Stable pseudo-random pick from the persona palette, keyed so the same person always lands on the same color. */
fun randomAvatarColor(key: String): Color {
    if (key.isEmpty()) return AvatarPalette.first()
    val index = abs(key.hashCode()) % AvatarPalette.size
    return AvatarPalette[index]
}

/**
 * The angular black/white/colored persona-style avatar frame used across chat, conversations and friends.
 * Drawn at its native 110x90dp design size and scaled down to fit [size], so the shape stays proportioned.
 */
@Composable
fun PersonaAvatar(
    drawableRes: Int,
    backgroundColor: Color,
    size: Dp,
    modifier: Modifier = Modifier,
    backgroundScale: Float = 1f,
    foregroundScale: Float = 1f,
) {
    val displayScale = size / NativeAvatarSize.height
    val density = LocalDensity.current

    Box(
        modifier = modifier.scaledToFit(displayScale * backgroundScale),
    ) {
        Box(
            modifier = Modifier
                .size(NativeAvatarSize)
                .drawBehind {
                    drawOutline(asOutline(avatarBlackBox()), Color.Black)
                    drawOutline(asOutline(avatarWhiteBox()), Color.White)
                    drawOutline(asOutline(avatarColoredBox()), backgroundColor)
                }
                .clip(with(density) { avatarClipBox() })
        ) {
            Image(
                painter = painterResource(drawableRes),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 8.dp)
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 1.15f)
                        scaleX = foregroundScale
                        scaleY = foregroundScale
                    }
            )
        }
    }
}

/**
 * Measures its single child at its natural (unconstrained) size, then reports a scaled-down
 * footprint to the real parent and paints the child scaled to fit it. Unlike [Modifier.scale],
 * this never lets an outer fixed size clamp the child's measurement - which would otherwise
 * squash the hand-drawn avatar shape (its coordinates are absolute, not relative to its box).
 */
private fun Modifier.scaledToFit(scale: Float): Modifier = this.layout { measurable, _ ->
    val placeable = measurable.measure(Constraints())
    val width = (placeable.width * scale).roundToInt()
    val height = (placeable.height * scale).roundToInt()
    layout(width, height) {
        placeable.placeWithLayer(0, 0) {
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin(0f, 0f)
        }
    }
}

private fun Density.avatarColoredBox(): Shape = GenericShape { _, _ ->
    moveTo(22.5.dp.toPx(), 28.dp.toPx())
    lineTo(94.4.dp.toPx(), 31.4.dp.toPx())
    lineTo(104.3.dp.toPx(), 67.5.dp.toPx())
    lineTo(40.dp.toPx(), 76.6.dp.toPx())
    close()
}

private fun Density.avatarWhiteBox(): Shape = GenericShape { _, _ ->
    moveTo(16.4.dp.toPx(), 20.5.dp.toPx())
    lineTo(96.7.dp.toPx(), 30.4.dp.toPx())
    lineTo(106.4.dp.toPx(), 70.dp.toPx())
    lineTo(37.8.dp.toPx(), 80.4.dp.toPx())
    close()
}

private fun Density.avatarBlackBox(): Shape = GenericShape { _, _ ->
    moveTo(0f, 17.dp.toPx())
    lineTo(100.5.dp.toPx(), 27.2.dp.toPx())
    lineTo(110.dp.toPx(), 72.7.dp.toPx())
    lineTo(33.4.dp.toPx(), 90.dp.toPx())
    close()
}

private fun Density.avatarClipBox(): Shape = GenericShape { _, _ ->
    moveTo(10.3.dp.toPx(), (-5.6).dp.toPx())
    lineTo(114.7.dp.toPx(), (-5.6).dp.toPx())
    lineTo(114.7.dp.toPx(), 65.6.dp.toPx())
    lineTo(40.dp.toPx(), 76.6.dp.toPx())
    close()
}
