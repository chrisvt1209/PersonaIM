package dev.compose.messenger.feature.conversations.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.compose.messenger.feature.conversations.data.ConversationRepository
import dev.compose.messenger.feature.conversations.domain.Conversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConversationViewModel(
    private val repository: ConversationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getConversations().collect { conversations ->
                _uiState.update { it.copy(conversations = conversations, isLoading = false) }
            }
        }
    }

    fun onEvent(event: ConversationEvent) {
        when (event) {
            ConversationEvent.Refresh -> loadConversations()
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
        }
    }
}

data class ConversationUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)

sealed interface ConversationEvent {
    data object Refresh : ConversationEvent
    data class Search(val query: String) : ConversationEvent
    data class CreateConversation(val userId: Long) : ConversationEvent
    data class DeleteConversation(val conversationId: String) : ConversationEvent
}
