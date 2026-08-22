package dev.compose.messenger.core.designsystem.util

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.unit.dp

fun Modifier.personaPanelBackground(
    accentColor: Color,
    fillColor: Color,
): Modifier = drawBehind {
    val outer = GenericShape { size, _ ->
        moveTo(0f, 12.dp.toPx())
        lineTo(size.width - 24.dp.toPx(), 0f)
        lineTo(size.width, size.height - 14.dp.toPx())
        lineTo(22.dp.toPx(), size.height)
        close()
    }

    val inner = GenericShape { size, _ ->
        moveTo(10.dp.toPx(), 18.dp.toPx())
        lineTo(size.width - 28.dp.toPx(), 8.dp.toPx())
        lineTo(size.width - 10.dp.toPx(), size.height - 18.dp.toPx())
        lineTo(28.dp.toPx(), size.height - 8.dp.toPx())
        close()
    }

    val accent = GenericShape { size, _ ->
        moveTo(12.dp.toPx(), 24.dp.toPx())
        lineTo(44.dp.toPx(), 18.dp.toPx())
        lineTo(38.dp.toPx(), size.height - 28.dp.toPx())
        lineTo(6.dp.toPx(), size.height - 20.dp.toPx())
        close()
    }

    drawOutline(asOutline(outer), Color.Black)
    drawOutline(asOutline(inner), fillColor)
    drawOutline(asOutline(accent), accentColor)
}

fun Modifier.personaBadgeBackground(
    color: Color,
): Modifier = drawBehind {
    val outer = GenericShape { size, _ ->
        moveTo(0f, 4.dp.toPx())
        lineTo(size.width - 10.dp.toPx(), 0f)
        lineTo(size.width, size.height - 4.dp.toPx())
        lineTo(10.dp.toPx(), size.height)
        close()
    }

    val inner = GenericShape { size, _ ->
        moveTo(4.dp.toPx(), 7.dp.toPx())
        lineTo(size.width - 12.dp.toPx(), 4.dp.toPx())
        lineTo(size.width - 4.dp.toPx(), size.height - 6.dp.toPx())
        lineTo(12.dp.toPx(), size.height - 4.dp.toPx())
        close()
    }

    drawOutline(asOutline(outer), Color.Black)
    drawOutline(asOutline(inner), color)
}

fun Modifier.composerBackground(): Modifier = drawBehind {
    val outer = GenericShape { size, _ ->
        moveTo(0f, 18.dp.toPx())
        lineTo(size.width - 30.dp.toPx(), 0f)
        lineTo(size.width, size.height - 12.dp.toPx())
        lineTo(22.dp.toPx(), size.height)
        close()
    }

    val inner = GenericShape { size, _ ->
        moveTo(12.dp.toPx(), 22.dp.toPx())
        lineTo(size.width - 34.dp.toPx(), 8.dp.toPx())
        lineTo(size.width - 12.dp.toPx(), size.height - 16.dp.toPx())
        lineTo(30.dp.toPx(), size.height - 6.dp.toPx())
        close()
    }

    drawOutline(asOutline(outer), Color.Black)
    drawOutline(asOutline(inner), Color.White)
}

fun Modifier.sendButtonBackground(): Modifier = drawBehind {
    val outer = GenericShape { size, _ ->
        moveTo(0f, 8.dp.toPx())
        lineTo(size.width - 16.dp.toPx(), 0f)
        lineTo(size.width, size.height - 10.dp.toPx())
        lineTo(18.dp.toPx(), size.height)
        close()
    }

    val inner = GenericShape { size, _ ->
        moveTo(8.dp.toPx(), 12.dp.toPx())
        lineTo(size.width - 18.dp.toPx(), 6.dp.toPx())
        lineTo(size.width - 8.dp.toPx(), size.height - 12.dp.toPx())
        lineTo(22.dp.toPx(), size.height - 4.dp.toPx())
        close()
    }

    drawOutline(asOutline(outer), Color.White)
    drawOutline(asOutline(inner), Color.Black)
}
