package dev.compose.messenger.feature.chat.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.compose.messenger.core.common.model.Message
import dev.compose.messenger.core.common.model.Sender
import dev.compose.messenger.feature.chat.presentation.TranscriptSizes.MaxLineShift
import dev.compose.messenger.feature.chat.presentation.TranscriptSizes.MaxLineWidth
import dev.compose.messenger.feature.chat.presentation.TranscriptSizes.MinLineShift
import dev.compose.messenger.feature.chat.presentation.TranscriptSizes.MinLineWidth
import dev.compose.messenger.feature.chat.presentation.TranscriptSizes.RenMessageCenter
import dev.compose.messenger.core.common.util.randomBetween
import dev.compose.messenger.core.common.util.BetterEaseOutBack
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun rememberTranscriptState(
    conversationKey: String,
    messages: List<Message>,
): TranscriptState {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val transcriptState = remember(density) { TranscriptState(density, coroutineScope) }

    LaunchedEffect(conversationKey, messages) {
        transcriptState.sync(conversationKey, messages.toImmutableList())
    }

    return transcriptState
}

/** A message in the transcript with everything needed for rendering. */
data class ChatEntry(
    val message: Message,
    val lineCoordinates: LineCoordinates,
    val drawPunctuation: Boolean,
    val lineProgress: State<Float>,
    val avatarBackgroundScale: State<Float>,
    val avatarForegroundScale: State<Float>,
    val messageHorizontalScale: State<Float>,
    val messageVerticalScale: State<Float>,
    val messageTextAlpha: State<Float>,
    val punctuationScale: State<Float>,
)

data class LineCoordinates(
    val leftPoint: Offset,
    val rightPoint: Offset,
)

@Stable
class TranscriptState internal constructor(
    private val density: Density,
    private val coroutineScope: CoroutineScope,
) {
    private var activeConversationKey = ""
    private val entryStates = mutableListOf<EntryState>()
    private val _entries = mutableStateOf<ImmutableList<ChatEntry>>(persistentListOf())
    val entries: ImmutableList<ChatEntry> by _entries

    fun sync(
        conversationKey: String,
        messages: ImmutableList<Message>,
    ) {
        val shouldRebuild = conversationKey != activeConversationKey ||
                messages.size < entryStates.size ||
                entryStates.map { it.message.id } != messages.take(entryStates.size).map { it.id }

        if (shouldRebuild) {
            activeConversationKey = conversationKey
            rebuildImmediately(messages)
            return
        }

        if (messages.size == entryStates.size) {
            _entries.value = entryStates.map { it.toEntry() }.toImmutableList()
            return
        }

        messages.drop(entryStates.size).forEach { message ->
            appendAnimated(message)
        }
        _entries.value = entryStates.map { it.toEntry() }.toImmutableList()
    }

    private fun rebuildImmediately(messages: ImmutableList<Message>) {
        entryStates.clear()

        messages.forEachIndexed { index, message ->
            entryStates += createEntryState(
                index = index,
                message = message,
                animate = false,
            )

            if (index > 0) {
                finalizeEntryState(entryStates[index - 1], animate = false)
            }
        }

        _entries.value = entryStates.map { it.toEntry() }.toImmutableList()
    }

    private fun appendAnimated(message: Message) {
        val position = entryStates.size
        entryStates += createEntryState(
            index = position,
            message = message,
            animate = true,
        )

        if (entryStates.size > 1) {
            finalizeEntryState(entryStates[entryStates.lastIndex - 1], animate = true)
        }
    }

    private fun createEntryState(
        index: Int,
        message: Message,
        animate: Boolean,
    ): EntryState = with(density) {
        val width = randomBetween(MinLineWidth.toPx(), MaxLineWidth.toPx())

        val lineCoordinates = when (message.sender) {
            Sender.Ren -> {
                val leftX = RenMessageCenter.x.toPx() - (width / 2f)
                val y = RenMessageCenter.y.toPx()
                LineCoordinates(
                    leftPoint = Offset(leftX, y),
                    rightPoint = Offset(leftX + width, y),
                )
            }

            else -> {
                val leftX = (TranscriptSizes.AvatarSize.width.toPx() / 2f) - (width / 2f)
                val y = TranscriptSizes.AvatarSize.height.toPx() / 2f
                LineCoordinates(
                    leftPoint = Offset(leftX, y),
                    rightPoint = Offset(leftX + width, y),
                )
            }
        }

        return EntryState(
            position = index,
            message = message,
            lineProgress = Animatable(initialValue = 0f),
            avatarBackgroundScale = Animatable(initialValue = if (animate) 0.6f else 1f),
            avatarForegroundScale = Animatable(initialValue = if (animate) 0f else 1f),
            messageHorizontalScale = Animatable(initialValue = if (animate) 0.3f else 1f),
            messageVerticalScale = Animatable(initialValue = if (animate) 0.8f else 1f),
            messageTextAlpha = Animatable(initialValue = if (animate) 0f else 1f),
            punctuationScale = Animatable(
                initialValue = if (animate || !message.text.endsWith('?')) 0f else 1f,
            ),
            lineCoordinates = lineCoordinates,
        ).also { entryState ->
            if (!animate) return@also

            coroutineScope.launch {
                entryState.avatarBackgroundScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = BetterEaseOutBack,
                    ),
                )
            }

            coroutineScope.launch {
                delay(160L.milliseconds)
                entryState.avatarForegroundScale.snapTo(0.8f)
                entryState.avatarForegroundScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 150,
                        easing = BetterEaseOutBack,
                    ),
                )
            }

            coroutineScope.launch {
                entryState.messageHorizontalScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 180,
                        easing = BetterEaseOutBack,
                    ),
                )
            }

            coroutineScope.launch {
                entryState.messageVerticalScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 180,
                        easing = BetterEaseOutBack,
                    ),
                )
            }

            coroutineScope.launch {
                delay(100L.milliseconds)
                entryState.messageTextAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 130),
                )
            }

            if (message.text.endsWith('?')) {
                coroutineScope.launch {
                    delay(130L.milliseconds)
                    entryState.punctuationScale.snapTo(0.4f)
                    entryState.punctuationScale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 100),
                    )
                }
            }
        }
    }

    private fun finalizeEntryState(
        entryState: EntryState,
        animate: Boolean,
    ) = with(density) {
        val direction = if (entryState.position % 2 == 0) 1f else -1f
        val horizontalShift = when {
            entryState.position > 0 -> randomBetween(MinLineShift.toPx(), MaxLineShift.toPx()) * direction
            else -> 0f
        }
        val horizontalOffset = when (entryState.message.sender) {
            Sender.Ren -> Offset.Zero
            else -> Offset(horizontalShift, 0f)
        }

        entryState.lineCoordinates = entryState.lineCoordinates.copy(
            leftPoint = entryState.lineCoordinates.leftPoint + horizontalOffset,
            rightPoint = entryState.lineCoordinates.rightPoint + horizontalOffset,
        )

        if (animate) {
            coroutineScope.launch {
                entryState.lineProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 180),
                )
            }
        } else {
            coroutineScope.launch {
                entryState.lineProgress.snapTo(1f)
            }
        }
    }
}

object TranscriptSizes {
    val AvatarSize = DpSize(110.dp, 90.dp)
    val EntrySpacing = 16.dp
    val RenMessageCenter = DpOffset(x = 60.dp, y = 28.dp)
    val MinLineShift = 16.dp
    val MaxLineShift = 48.dp
    val MinLineWidth = 44.dp
    val MaxLineWidth = 60.dp

    fun getTopDrawingOffset(scope: CacheDrawScope, entry: ChatEntry): Offset = with(scope) {
        return when (entry.message.sender) {
            Sender.Ren -> {
                val horizontalShift = size.width - (RenMessageCenter.x.toPx() * 2f)
                Offset(x = horizontalShift, y = 0f)
            }

            else -> Offset.Zero
        }
    }

    fun getBottomDrawingOffset(scope: CacheDrawScope, entry: ChatEntry): Offset = with(scope) {
        val verticalShift = size.height + EntrySpacing.toPx()
        return when (entry.message.sender) {
            Sender.Ren -> {
                val horizontalShift = size.width - (RenMessageCenter.x.toPx() * 2f)
                Offset(x = horizontalShift, y = verticalShift)
            }

            else -> Offset(x = 0f, y = verticalShift)
        }
    }
}

private class EntryState(
    val position: Int,
    val message: Message,
    val lineProgress: Animatable<Float, AnimationVector1D>,
    val avatarBackgroundScale: Animatable<Float, AnimationVector1D>,
    val avatarForegroundScale: Animatable<Float, AnimationVector1D>,
    val messageHorizontalScale: Animatable<Float, AnimationVector1D>,
    val messageVerticalScale: Animatable<Float, AnimationVector1D>,
    val messageTextAlpha: Animatable<Float, AnimationVector1D>,
    val punctuationScale: Animatable<Float, AnimationVector1D>,
    var lineCoordinates: LineCoordinates,
)

private fun EntryState.toEntry() = ChatEntry(
    message = message,
    lineCoordinates = lineCoordinates,
    drawPunctuation = message.text.endsWith('?'),
    avatarBackgroundScale = avatarBackgroundScale.asState(),
    avatarForegroundScale = avatarForegroundScale.asState(),
    messageHorizontalScale = messageHorizontalScale.asState(),
    messageVerticalScale = messageVerticalScale.asState(),
    messageTextAlpha = messageTextAlpha.asState(),
    punctuationScale = punctuationScale.asState(),
    lineProgress = lineProgress.asState(),
)
