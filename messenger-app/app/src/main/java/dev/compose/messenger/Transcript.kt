package dev.compose.messenger

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
import kotlinx.collections.immutable.ImmutableList

/**
 * Scrollable list of messages - including the background line connecting each entry.
 */
@Composable
fun Transcript(
  entries: ImmutableList<Entry>,
  showTypingIndicator: Boolean,
  modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()
  val totalItemCount by remember { derivedStateOf { listState.layoutInfo.totalItemsCount } }

  LaunchedEffect(totalItemCount) {
    if (totalItemCount == 0) return@LaunchedEffect

    val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull() ?: return@LaunchedEffect
    // animateScrollToItem isn't super smooth, so if the newly added item is visible, then animate
    // manually with a tween.
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
      if (entry.message.sender == Sender.Ren) {
        Reply(
          entry = entry,
          modifier = Modifier.drawConnectingLine(entry, entries.getOrNull(index + 1))
        )
      } else {
          Entry(
              entry,
              modifier = Modifier.drawConnectingLine(entry, entries.getOrNull(index + 1))
          )
      }
    }

    if (showTypingIndicator) {
      item(key = "typing-indicator") {
        TypingIndicator()
      }
    }
  }
}
