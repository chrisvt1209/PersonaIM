package dev.compose.messenger.feature.chat.presentation.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.compose.messenger.feature.chat.presentation.components.TypingIndicator
import dev.compose.messenger.feature.chat.presentation.ChatEntry
import dev.compose.messenger.feature.chat.presentation.TranscriptSizes
import dev.compose.messenger.feature.chat.presentation.util.drawConnectingLine
import kotlinx.collections.immutable.ImmutableList

@Composable
fun MessageList(
    entries: ImmutableList<ChatEntry>,
    showTypingIndicator: Boolean,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val totalItemCount by remember { derivedStateOf { listState.layoutInfo.totalItemsCount } }

    LaunchedEffect(totalItemCount) {
        if (totalItemCount == 0) return@LaunchedEffect

        val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull() ?: return@LaunchedEffect
        if (lastVisibleItem.index == totalItemCount - 1) {
            listState.animateScrollBy(
                value = lastVisibleItem.size.toFloat() + listState.layoutInfo.afterContentPadding,
                animationSpec = tween(durationMillis = 280),
            )
        } else {
            listState.animateScrollToItem(totalItemCount - 1)
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(TranscriptSizes.EntrySpacing),
        state = listState,
        contentPadding = WindowInsets(top = 8.dp, bottom = 16.dp)
            .add(WindowInsets(bottom = 16.dp))
            .asPaddingValues()
            .let { padding ->
                PaddingValues(
                    start = 8.dp,
                    top = padding.calculateTopPadding(),
                    end = 8.dp,
                    bottom = padding.calculateBottomPadding(),
                )
            },
        modifier = modifier.fillMaxSize(),
    ) {
        itemsIndexed(
            items = entries,
            key = { _, entry -> entry.message.id },
        ) { index, entry ->
            MessageItem(
                entry = entry,
                modifier = Modifier.drawConnectingLine(entry, entries.getOrNull(index + 1))
            )
        }

        if (showTypingIndicator) {
            item(key = "typing-indicator") {
                TypingIndicator()
            }
        }
    }
}
