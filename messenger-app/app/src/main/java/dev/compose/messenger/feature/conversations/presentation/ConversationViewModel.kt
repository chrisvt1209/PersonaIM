package dev.compose.messenger.feature.conversations.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.compose.messenger.core.common.model.Avatar
import dev.compose.messenger.feature.conversations.data.ConversationRepository
import dev.compose.messenger.feature.conversations.domain.Conversation
import dev.compose.messenger.feature.conversations.domain.GroupInvite
import dev.compose.messenger.feature.profile.data.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConversationViewModel(
    private val repository: ConversationRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
        loadInvites()
        viewModelScope.launch {
            profileRepository.getCurrentUser().collect { user ->
                _uiState.update { it.copy(userAvatar = user?.avatar ?: Avatar.Default.key) }
            }
        }
    }

    private fun loadConversations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getConversations().collect { conversations ->
                _uiState.update { it.copy(conversations = conversations, isLoading = false) }
            }
        }
    }

    private fun loadInvites() {
        viewModelScope.launch {
            repository.getInvites().onSuccess { invites ->
                _uiState.update { it.copy(invites = invites) }
            }
        }
    }

    fun onEvent(event: ConversationEvent) {
        when (event) {
            ConversationEvent.Refresh -> {
                loadConversations()
                loadInvites()
            }
            is ConversationEvent.Search -> {
                // TODO: Implement search
            }
            is ConversationEvent.CreateConversation -> {
                viewModelScope.launch {
                    repository.createConversation(event.userId)
                }
            }
            is ConversationEvent.DeleteConversation -> {
                viewModelScope.launch {
                    repository.deleteConversation(event.conversationId)
                }
            }
            is ConversationEvent.CreateGroup -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isCreatingGroup = true, createGroupError = null) }
                    repository.createGroup(event.title, event.memberUserIds)
                        .onSuccess {
                            _uiState.update { it.copy(isCreatingGroup = false) }
                        }
                        .onFailure { e ->
                            _uiState.update { it.copy(isCreatingGroup = false, createGroupError = e.message) }
                        }
                }
            }
            is ConversationEvent.AcceptInvite -> {
                viewModelScope.launch {
                    repository.acceptInvite(event.conversationId)
                    loadInvites()
                }
            }
            is ConversationEvent.DeclineInvite -> {
                viewModelScope.launch {
                    repository.declineInvite(event.conversationId)
                    loadInvites()
                }
            }
        }
    }
}

data class ConversationUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val userAvatar: String = Avatar.Default.key,
    val invites: List<GroupInvite> = emptyList(),
    val isCreatingGroup: Boolean = false,
    val createGroupError: String? = null
)

sealed interface ConversationEvent {
    data object Refresh : ConversationEvent
    data class Search(val query: String) : ConversationEvent
    data class CreateConversation(val userId: Long) : ConversationEvent
    data class DeleteConversation(val conversationId: String) : ConversationEvent
    data class CreateGroup(val title: String, val memberUserIds: List<Long>) : ConversationEvent
    data class AcceptInvite(val conversationId: String) : ConversationEvent
    data class DeclineInvite(val conversationId: String) : ConversationEvent
}
