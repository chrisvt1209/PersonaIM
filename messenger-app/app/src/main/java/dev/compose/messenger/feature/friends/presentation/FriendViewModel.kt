package dev.compose.messenger.feature.friends.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.compose.messenger.feature.conversations.data.ConversationRepository
import dev.compose.messenger.feature.friends.data.FriendRepository
import dev.compose.messenger.feature.friends.domain.Friend
import dev.compose.messenger.feature.profile.data.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FriendViewModel(
    private val repository: FriendRepository,
    private val conversationRepository: ConversationRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendUiState())
    val uiState: StateFlow<FriendUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<String?>(null)
    val navigationEvent: StateFlow<String?> = _navigationEvent.asStateFlow()

    init {
        loadFriends()
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }

    private fun loadFriends() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getFriends().collect { friends ->
                _uiState.update { it.copy(friends = friends, isLoading = false) }
                loadFriendAvatars(friends)
            }
        }
    }

    private fun loadFriendAvatars(friends: List<Friend>) {
        viewModelScope.launch {
            val knownIds = _uiState.value.friendAvatars.keys
            val missingIds = friends.map { it.id }.distinct().filter { it !in knownIds }
            if (missingIds.isEmpty()) return@launch

            val fetched = missingIds.mapNotNull { id ->
                profileRepository.getUser(id).getOrNull()?.let { id to it.avatar }
            }.toMap()

            if (fetched.isNotEmpty()) {
                _uiState.update { it.copy(friendAvatars = it.friendAvatars + fetched) }
            }
        }
    }

    fun onEvent(event: FriendEvent) {
        when (event) {
            is FriendEvent.AddFriend -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                    repository.addFriend(event.uid)
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
                    _uiState.update { it.copy(isLoading = true) }
                    conversationRepository.createConversation(event.userId)
                        .onSuccess { conversation ->
                            _uiState.update { it.copy(isLoading = false) }
                            _navigationEvent.value = "chat/${conversation.id}"
                        }
                        .onFailure { e ->
                            _uiState.update { it.copy(isLoading = false, error = e.message) }
                        }
                }
            }
            is FriendEvent.CreateGroup -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                    conversationRepository.createGroup(event.title, event.memberUserIds)
                        .onSuccess { conversation ->
                            _uiState.update { it.copy(isLoading = false) }
                            _navigationEvent.value = "chat/${conversation.id}"
                        }
                        .onFailure { e ->
                            _uiState.update { it.copy(isLoading = false, error = e.message) }
                        }
                }
            }
            FriendEvent.ErrorShown -> _uiState.update { it.copy(error = null) }
        }
    }
}

data class FriendUiState(
    val friends: List<Friend> = emptyList(),
    val friendAvatars: Map<Long, String> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface FriendEvent {
    data class AddFriend(val uid: String) : FriendEvent
    data object Refresh : FriendEvent
    data class StartChat(val userId: Long) : FriendEvent
    data class CreateGroup(val title: String, val memberUserIds: List<Long>) : FriendEvent
    data object ErrorShown : FriendEvent
}
