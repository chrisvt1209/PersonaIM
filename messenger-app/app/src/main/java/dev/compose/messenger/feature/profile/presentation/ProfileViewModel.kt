package dev.compose.messenger.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.compose.messenger.feature.profile.data.ProfileRepository
import dev.compose.messenger.feature.profile.domain.User
import dev.compose.messenger.feature.auth.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        sync()
        viewModelScope.launch {
            repository.getCurrentUser().collect { user ->
                if (user != null) {
                    _uiState.update { it.copy(user = user, isLoading = false) }
                }
            }
        }
    }

    fun sync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.syncProfile().onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
