package dev.compose.messenger.feature.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.compose.messenger.core.common.model.Message
import dev.compose.messenger.core.common.model.Sender
import dev.compose.messenger.core.datastore.PreferencesManager
import dev.compose.messenger.feature.chat.data.ChatRepository
import dev.compose.messenger.feature.conversations.data.ConversationRepository
import dev.compose.messenger.feature.conversations.domain.Participant
import dev.compose.messenger.feature.friends.data.FriendRepository
import dev.compose.messenger.feature.friends.domain.Friend
import dev.compose.messenger.feature.profile.data.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository,
    private val conversationRepository: ConversationRepository,
    private val friendRepository: FriendRepository,
    private val profileRepository: ProfileRepository,
    private val preferencesManager: PreferencesManager,
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
                val myUserId = preferencesManager.currentUserId.first()
                val myRole = detail.participants.firstOrNull { it.userId == myUserId }?.role
                _uiState.update {
                    it.copy(
                        conversationTitle = detail.title,
                        isGroup = detail.isGroup,
                        participants = detail.participants,
                        myUserId = myUserId,
                        myRole = myRole
                    )
                }
                loadParticipantAvatars(detail.participants)
            }
        }
    }

    private fun loadParticipantAvatars(participants: List<Participant>) {
        viewModelScope.launch {
            val avatars = participants.mapNotNull { participant ->
                profileRepository.getUser(participant.userId).getOrNull()?.let { user ->
                    participant.userId to user.avatar
                }
            }.toMap()
            _uiState.update { it.copy(participantAvatars = avatars) }
        }
    }

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.DraftChanged -> _uiState.update { it.copy(draft = event.draft) }
            ChatEvent.SendClicked -> sendMessage()
            ChatEvent.LoadFriendsForInvite -> loadFriendsForInvite()
            is ChatEvent.InviteFriend -> inviteFriend(event.userId)
            is ChatEvent.RemoveMember -> removeMember(event.userId)
            is ChatEvent.ChangeRole -> changeRole(event.userId, event.role)
            ChatEvent.LeaveGroup -> leaveGroup()
            ChatEvent.SendErrorShown -> _uiState.update { it.copy(sendError = null) }
            ChatEvent.MemberActionErrorShown -> _uiState.update { it.copy(memberActionError = null) }
        }
    }

    private fun sendMessage() {
        val text = _uiState.value.draft.trim()
        if (text.isEmpty()) return

        _uiState.update { it.copy(draft = "", sendError = null) }

        viewModelScope.launch {
            repository.sendMessage(conversationId, text)
                .onFailure { e -> _uiState.update { it.copy(sendError = e.message) } }
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

    private fun removeMember(userId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(memberActionError = null) }
            conversationRepository.removeMember(conversationId, userId)
                .onSuccess { loadConversationDetail() }
                .onFailure { e -> _uiState.update { it.copy(memberActionError = e.message) } }
        }
    }

    private fun changeRole(userId: Long, role: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(memberActionError = null) }
            conversationRepository.changeRole(conversationId, userId, role)
                .onSuccess { loadConversationDetail() }
                .onFailure { e -> _uiState.update { it.copy(memberActionError = e.message) } }
        }
    }

    private fun leaveGroup() {
        viewModelScope.launch {
            _uiState.update { it.copy(memberActionError = null) }
            conversationRepository.leaveGroup(conversationId)
                .onSuccess { _uiState.update { it.copy(leftGroup = true) } }
                .onFailure { e -> _uiState.update { it.copy(memberActionError = e.message) } }
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
    val participantAvatars: Map<Long, String> = emptyMap(),
    val myUserId: Long? = null,
    val myRole: String? = null,
    val availableFriendsToInvite: List<Friend> = emptyList(),
    val inviteError: String? = null,
    val sendError: String? = null,
    val memberActionError: String? = null,
    val leftGroup: Boolean = false
)

sealed interface ChatEvent {
    data class DraftChanged(val draft: String) : ChatEvent
    data object SendClicked : ChatEvent
    data object LoadFriendsForInvite : ChatEvent
    data class InviteFriend(val userId: Long) : ChatEvent
    data class RemoveMember(val userId: Long) : ChatEvent
    data class ChangeRole(val userId: Long, val role: String) : ChatEvent
    data object LeaveGroup : ChatEvent
    data object SendErrorShown : ChatEvent
    data object MemberActionErrorShown : ChatEvent
}
