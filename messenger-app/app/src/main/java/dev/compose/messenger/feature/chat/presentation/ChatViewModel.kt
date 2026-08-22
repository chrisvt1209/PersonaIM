package dev.compose.messenger.feature.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.compose.messenger.core.common.model.Message
import dev.compose.messenger.core.common.model.Sender
import dev.compose.messenger.feature.chat.data.ChatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository,
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
    }

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.DraftChanged -> _uiState.update { it.copy(draft = event.draft) }
            ChatEvent.SendClicked -> sendMessage()
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
}

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val draft: String = "",
    val typingSender: Sender? = null,
    val isLoading: Boolean = false
)

sealed interface ChatEvent {
    data class DraftChanged(val draft: String) : ChatEvent
    data object SendClicked : ChatEvent
}
