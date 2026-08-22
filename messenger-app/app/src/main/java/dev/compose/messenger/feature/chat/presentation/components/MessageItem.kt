package dev.compose.messenger.feature.chat.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.compose.messenger.R
import dev.compose.messenger.core.designsystem.theme.OptimaNova
import dev.compose.messenger.core.designsystem.util.asOutline
import dev.compose.messenger.feature.chat.presentation.ChatEntry
import dev.compose.messenger.feature.chat.presentation.TranscriptSizes

@Composable
fun MessageItem(
    entry: ChatEntry,
    modifier: Modifier = Modifier
) {
    if (entry.message.sender.displayName == "You") {
        ReplyItem(entry, modifier)
    } else {
        EntryItem(entry, modifier)
    }
}

@Composable
private fun EntryItem(
    entry: ChatEntry,
    modifier: Modifier = Modifier,
) {
    EntryLayout(
        avatar = { Avatar(entry) },
        textBox = { TextBox(entry) },
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .then(modifier)
    )
}

@Composable
private fun EntryLayout(
    avatar: @Composable () -> Unit,
    textBox: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Layout(
        content = {
            avatar()
            textBox()
        },
        modifier = modifier,
    ) { (avatarMeasurable, textMeasurable), constraints ->
        val textBoxOverlap = 18.dp.roundToPx()
        val textBoxTopPadding = 4.dp.roundToPx()

        val avatarPlaceable = avatarMeasurable.measure(constraints)
        val textMaxWidth = constraints.maxWidth - avatarPlaceable.width + textBoxOverlap
        val textConstraints = constraints.copy(maxWidth = textMaxWidth)
        val textPlaceable = textMeasurable.measure(textConstraints)

        val textWithPadding = textPlaceable.height + textBoxTopPadding
        val width = avatarPlaceable.width + textPlaceable.width - textBoxOverlap
        val height = maxOf(avatarPlaceable.height, textWithPadding)
        layout(width, height) {
            avatarPlaceable.place(0, 0)
            val textBoxX = avatarPlaceable.width - textBoxOverlap
            val textBoxY = if (textWithPadding > avatarPlaceable.height) {
                textBoxTopPadding
            } else {
                height - textPlaceable.height - 6.dp.roundToPx()
            }

            textPlaceable.place(textBoxX, textBoxY)
        }
    }
}

@Composable
private fun ReplyItem(
    entry: ChatEntry,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.CenterEnd,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .then(modifier)
    ) {
        Text(
            text = entry.message.text,
            color = Color.Black,
            fontFamily = OptimaNova,
            fontSize = 18.sp,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .drawWithCache {
                    val outerBox = asOutline(replyOuterBox())
                    val innerBox = asOutline(replyInnerBox())

                    onDrawBehind {
                        scale(
                            scaleX = entry.messageHorizontalScale.value,
                            scaleY = entry.messageVerticalScale.value,
                            pivot = Offset(x = size.width - 20.dp.toPx(), y = size.height - 20.dp.toPx()),
                        ) {
                            drawOutline(outerBox, color = Color.White)
                            drawOutline(innerBox, color = Color.Black)
                        }
                    }
                }
                .alpha(entry.messageTextAlpha.value)
                .padding(start = 28.dp, top = 16.dp, end = 44.dp, bottom = 16.dp)
        )
    }
}

@Composable
private fun TextBox(entry: ChatEntry) {
    Box {
        Text(
            text = entry.message.text,
            color = Color.White,
            fontFamily = OptimaNova,
            modifier = Modifier
                .drawWithCache {
                    val outerBoxStem = asOutline(outerStem())
                    val outerBox = asOutline(outerBox())
                    val innerBoxStem = asOutline(innerStem())
                    val innerBox = asOutline(innerBox())

                    onDrawBehind {
                        scale(
                            scaleX = entry.messageHorizontalScale.value,
                            scaleY = entry.messageVerticalScale.value,
                            pivot = Offset(x = 20.dp.toPx(), y = getStemY(size.height)),
                        ) {
                            drawOutline(outerBox, color = Color.White)
                        }

                        drawOutline(outerBoxStem, color = Color.White)
                        drawOutline(innerBoxStem, color = Color.Black)

                        scale(
                            scaleX = entry.messageHorizontalScale.value,
                            scaleY = entry.messageVerticalScale.value,
                            pivot = Offset(x = 20.dp.toPx(), y = getStemY(size.height)),
                        ) {
                            drawOutline(innerBox, color = Color.Black)
                        }
                    }
                }
                .alpha(entry.messageTextAlpha.value)
                .padding(start = 42.dp, top = 20.dp, end = 32.dp, bottom = 20.dp)
        )

        if (entry.drawPunctuation) {
            Image(
                painter = painterResource(R.drawable.question_mark),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-12).dp, y = (-16).dp)
                    .scale(entry.punctuationScale.value)
            )
        }
    }
}

private fun Density.outerStem(): Shape = GenericShape { size, _ ->
    val verticalOrigin = getStemY(size.height)
    moveTo(0f, verticalOrigin - 19.2.dp.toPx())
    lineTo(19.5.dp.toPx(), verticalOrigin - 37.2.dp.toPx())
    lineTo(20.8.dp.toPx(), verticalOrigin - 31.5.dp.toPx())
    lineTo(32.4.dp.toPx(), verticalOrigin - 39.3.dp.toPx())
    lineTo(30.6.dp.toPx(), verticalOrigin - 15.8.dp.toPx())
    lineTo(11.7.dp.toPx(), verticalOrigin - 12.6.dp.toPx())
    lineTo(10.dp.toPx(), verticalOrigin - 20.dp.toPx())
    close()
}

private fun Density.innerStem(): Shape = GenericShape { size, _ ->
    val verticalOrigin = getStemY(size.height)
    moveTo(4.6.dp.toPx(), verticalOrigin - 22.2.dp.toPx())
    lineTo(17.dp.toPx(), verticalOrigin - 33.2.dp.toPx())
    lineTo(19.3.dp.toPx(), verticalOrigin - 28.1.dp.toPx())
    lineTo(34.4.dp.toPx(), verticalOrigin - 36.5.dp.toPx())
    lineTo(34.dp.toPx(), verticalOrigin - 21.4.dp.toPx())
    lineTo(14.4.dp.toPx(), verticalOrigin - 18.6.dp.toPx())
    lineTo(12.8.dp.toPx(), verticalOrigin - 25.4.dp.toPx())
    close()
}

private fun Density.getStemY(textBoxHeight: Float): Float {
    val avatarHeight = TranscriptSizes.AvatarSize.height.toPx()
    return if (textBoxHeight + 4.dp.toPx() > avatarHeight) {
        avatarHeight - 16.dp.toPx()
    } else {
        textBoxHeight - 5.dp.toPx()
    }
}

private fun Density.outerBox(): Shape = GenericShape { size, _ ->
    moveTo(31.7.dp.toPx(), 3.1.dp.toPx())
    lineTo(size.width, 0f)
    lineTo(size.width - 23.dp.toPx(), size.height)
    lineTo(15.6.dp.toPx(), size.height - 8.dp.toPx())
    close()
}

private fun Density.innerBox(): Shape = GenericShape { size, _ ->
    moveTo(33.dp.toPx(), 7.7.dp.toPx())
    lineTo(size.width - 13.dp.toPx(), 3.7.dp.toPx())
    lineTo(size.width - 25.7.dp.toPx(), size.height - 4.6.dp.toPx())
    lineTo(20.4.dp.toPx(), size.height - 12.dp.toPx())
    close()
}

private fun Density.replyOuterBox(): Shape = GenericShape { size, _ ->
    moveTo(0f, 6.dp.toPx())
    lineTo(size.width - 22.dp.toPx(), 0f)
    lineTo(size.width, size.height - 12.dp.toPx())
    lineTo(14.dp.toPx(), size.height)
    close()
}

private fun Density.replyInnerBox(): Shape = GenericShape { size, _ ->
    moveTo(10.dp.toPx(), 10.dp.toPx())
    lineTo(size.width - 26.dp.toPx(), 6.dp.toPx())
    lineTo(size.width - 10.dp.toPx(), size.height - 18.dp.toPx())
    lineTo(20.dp.toPx(), size.height - 8.dp.toPx())
    close()
}
