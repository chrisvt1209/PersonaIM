package dev.compose.messenger.feature.friends.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.compose.messenger.feature.conversations.data.ConversationRepository
import dev.compose.messenger.feature.friends.data.FriendRepository
import dev.compose.messenger.feature.friends.domain.Friend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FriendViewModel(
    private val repository: FriendRepository,
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendUiState())
    val uiState: StateFlow<FriendUiState> = _uiState.asStateFlow()

    init {
        loadFriends()
    }

    private fun loadFriends() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getFriends().collect { friends ->
                _uiState.update { it.copy(friends = friends, isLoading = false) }
            }
        }
    }

    fun onEvent(event: FriendEvent) {
        when (event) {
            is FriendEvent.AddFriend -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                    repository.addFriend(event.email)
                        .onSuccess {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                        .onFailure { e ->
                            _uiState.update { it.copy(isLoading = false, error = e.message) }
                        }
                }
            }
            FriendEvent.Refresh -> loadFriends()
            is FriendEvent.StartChat -> {
                viewModelScope.launch {
                    conversationRepository.createConversation(event.userId)
                }
            }
        }
    }
}

data class FriendUiState(
    val friends: List<Friend> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface FriendEvent {
    data class AddFriend(val email: String) : FriendEvent
    data object Refresh : FriendEvent
    data class StartChat(val userId: Long) : FriendEvent
}
