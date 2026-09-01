package dev.compose.messenger.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import dev.compose.messenger.core.common.model.AppBackgroundColor

@Composable
fun BackgroundColorMenu(
    hostElement: @Composable () -> Unit,
    onBackgroundColorChange: (AppBackgroundColor) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPopup by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.90f else 1f, label = "scale")

    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = { showPopup = true },
                ),
        ) {
            hostElement()
        }

        BackgroundColorPopupMenu(
            show = showPopup,
            onDismissRequest = { showPopup = false },
            onBackgroundColorChange = onBackgroundColorChange,
        )
    }
}

@Composable
private fun BackgroundColorPopupMenu(
    show: Boolean,
    onDismissRequest: () -> Unit,
    onBackgroundColorChange: (AppBackgroundColor) -> Unit,
) {
    if (!show) return

    Popup(onDismissRequest = onDismissRequest) {
        Row(
            modifier = Modifier
                .menuBackground()
                .padding(horizontal = 40.dp, vertical = 16.dp)
        ) {
            AppBackgroundColor.entries.forEach { backgroundColor ->
                BackgroundColorOption(
                    backgroundColor = backgroundColor,
                    onClick = {
                        onDismissRequest()
                        onBackgroundColorChange(backgroundColor)
                    },
                )
            }
        }
    }
}

@Composable
private fun BackgroundColorOption(backgroundColor: AppBackgroundColor, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor.color)
            .border(width = 2.dp, color = Color.Black, shape = CircleShape)
            .clickable { onClick() }
    )
}
