package dev.compose.messenger.feature.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.compose.messenger.core.common.model.Message
import dev.compose.messenger.core.common.model.Sender
import dev.compose.messenger.feature.chat.data.ChatRepository
import dev.compose.messenger.feature.conversations.data.ConversationRepository
import dev.compose.messenger.feature.conversations.domain.Participant
import dev.compose.messenger.feature.friends.data.FriendRepository
import dev.compose.messenger.feature.friends.domain.Friend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository,
    private val conversationRepository: ConversationRepository,
    private val friendRepository: FriendRepository,
    private val conversationId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getMessages(conversationId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
        loadConversationDetail()
    }

    private fun loadConversationDetail() {
        viewModelScope.launch {
            conversationRepository.getConversationDetail(conversationId).onSuccess { detail ->
                _uiState.update {
                    it.copy(
                        conversationTitle = detail.title,
                        isGroup = detail.isGroup,
                        participants = detail.participants
                    )
                }
            }
        }
    }

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.DraftChanged -> _uiState.update { it.copy(draft = event.draft) }
            ChatEvent.SendClicked -> sendMessage()
            ChatEvent.LoadFriendsForInvite -> loadFriendsForInvite()
            is ChatEvent.InviteFriend -> inviteFriend(event.userId)
        }
    }

    private fun sendMessage() {
        val text = _uiState.value.draft.trim()
        if (text.isEmpty()) return

        _uiState.update { it.copy(draft = "") }

        viewModelScope.launch {
            repository.sendMessage(conversationId, text)
        }
    }

    private fun loadFriendsForInvite() {
        viewModelScope.launch {
            friendRepository.getFriends().collect { friends ->
                _uiState.update { it.copy(availableFriendsToInvite = friends) }
            }
        }
    }

    private fun inviteFriend(userId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(inviteError = null) }
            conversationRepository.inviteToGroup(conversationId, userId)
                .onSuccess { loadConversationDetail() }
                .onFailure { e -> _uiState.update { it.copy(inviteError = e.message) } }
        }
    }
}

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val draft: String = "",
    val typingSender: Sender? = null,
    val isLoading: Boolean = false,
    val conversationTitle: String = "Chat",
    val isGroup: Boolean = false,
    val participants: List<Participant> = emptyList(),
    val availableFriendsToInvite: List<Friend> = emptyList(),
    val inviteError: String? = null
)

sealed interface ChatEvent {
    data class DraftChanged(val draft: String) : ChatEvent
    data object SendClicked : ChatEvent
    data object LoadFriendsForInvite : ChatEvent
    data class InviteFriend(val userId: Long) : ChatEvent
}
