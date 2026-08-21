package dev.compose.messenger

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.get

@Composable
fun rememberChatAppState(): ChatAppState {
  val scope = rememberCoroutineScope()
  return remember(scope) { ChatAppState(scope) }
}

@Stable
class ChatAppState internal constructor(
  private val coroutineScope: CoroutineScope,
) {
  private var nextMessageId by mutableLongStateOf(1L)

  val conversations = conversationSeeds().map { seed ->
    ConversationState(seed) { nextId() }
  }

  private val drafts = mutableStateMapOf<String, String>().apply {
    conversations.forEach { put(it.id, "") }
  }

  var selectedConversationId by mutableStateOf(conversations.first().id)
    private set

  var season by mutableStateOf(conversations.first().backgroundSeason)
    private set

  val selectedConversation: ConversationState
    get() = conversations.first { it.id == selectedConversationId }

  val draft: String
    get() = drafts[selectedConversationId].orEmpty()

  fun selectConversation(conversationId: String) {
    if (conversationId == selectedConversationId) return

    selectedConversationId = conversationId
    selectedConversation.unreadCount = 0
    season = selectedConversation.backgroundSeason
  }

  fun updateDraft(text: String) {
    drafts[selectedConversationId] = text.take(MaxDraftLength)
  }

  fun changeSeason(season: Season) {
    this.season = season
  }

  fun sendMessage() {
    val conversation = selectedConversation
    val trimmedDraft = draft.trim()
    if (trimmedDraft.isEmpty()) return

    drafts[conversation.id] = ""
    conversation.unreadCount = 0
    conversation.messages += Message(
      id = nextId(),
      sender = Sender.Ren,
      text = trimmedDraft,
    )

    enqueueReplies(conversation)
  }

  private fun enqueueReplies(conversation: ConversationState) {
    conversation.pendingReplyBursts++

    if (conversation.replyJob?.isActive == true) return

    conversation.replyJob = coroutineScope.launch {
      while (conversation.pendingReplyBursts > 0) {
        conversation.pendingReplyBursts--
        val replies = conversation.nextReplies()
        for (reply in replies) {
          conversation.typingSender = reply.sender
          delay(TypingDelayMillis)
          conversation.typingSender = null

          conversation.messages += Message(
            id = nextId(),
            sender = reply.sender,
            text = reply.text,
          )

          if (conversation.id != selectedConversationId) {
            conversation.unreadCount++
          }

          delay(BetweenReplyDelayMillis)
        }
      }

      conversation.typingSender = null
      conversation.replyJob = null
    }
  }

  private fun nextId(): Long = nextMessageId++
}

@Stable
class ConversationState internal constructor(
  seed: ConversationSeed,
  nextMessageId: () -> Long,
) {
  val id = seed.id
  val title = seed.title
  val subtitle = seed.subtitle
  val participants = seed.participants
  val accentColor = seed.accentColor
  val backgroundSeason = seed.backgroundSeason
  private val autoReplies = seed.autoReplies

  val messages = mutableStateListOf<Message>().apply {
    seed.openingMessages.forEach { message ->
      add(
        Message(
          id = nextMessageId(),
          sender = message.sender,
          text = message.text,
        )
      )
    }
  }

  var unreadCount by mutableIntStateOf(0)
  var typingSender by mutableStateOf<Sender?>(null)
  internal var pendingReplyBursts by mutableIntStateOf(0)
  internal var replyJob: Job? = null
  private var nextReplyIndex by mutableIntStateOf(0)

  val participantNames: String
    get() = participants.joinToString(" / ") { it.displayName }

  val lastMessagePreview: String
    get() = typingSender?.let { "${it.displayName} is typing..." }
      ?: messages.lastOrNull()?.text
      ?: subtitle

  fun messagesSnapshot(): ImmutableList<Message> = messages.toImmutableList()

  internal fun nextReplies(): ImmutableList<MessageTemplate> {
    if (autoReplies.isEmpty()) return emptyList<MessageTemplate>().toImmutableList()

    val replies = autoReplies[nextReplyIndex]
    nextReplyIndex = (nextReplyIndex + 1) % autoReplies.size
    return replies
  }
}

private const val TypingDelayMillis = 1100L
private const val BetweenReplyDelayMillis = 220L
private const val MaxDraftLength = 280
