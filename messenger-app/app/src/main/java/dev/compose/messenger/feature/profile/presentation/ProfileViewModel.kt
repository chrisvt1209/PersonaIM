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

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.UpdateProfile -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isSavingProfile = true, profileSaveError = null) }
                    repository.updateProfile(event.username, event.email, event.avatar)
                        .onSuccess {
                            _uiState.update { it.copy(isSavingProfile = false) }
                        }
                        .onFailure { e ->
                            _uiState.update { it.copy(isSavingProfile = false, profileSaveError = e.message) }
                        }
                }
            }
            is ProfileEvent.ChangePassword -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isChangingPassword = true, passwordChangeError = null) }
                    repository.changePassword(event.currentPassword, event.newPassword)
                        .onSuccess {
                            _uiState.update {
                                it.copy(isChangingPassword = false, passwordChangeSuccess = true)
                            }
                        }
                        .onFailure { e ->
                            _uiState.update {
                                it.copy(isChangingPassword = false, passwordChangeError = e.message)
                            }
                        }
                }
            }
            ProfileEvent.PasswordChangeHandled -> {
                _uiState.update { it.copy(passwordChangeSuccess = false) }
            }
        }
    }
}

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSavingProfile: Boolean = false,
    val profileSaveError: String? = null,
    val isChangingPassword: Boolean = false,
    val passwordChangeError: String? = null,
    val passwordChangeSuccess: Boolean = false
)

sealed interface ProfileEvent {
    data class UpdateProfile(val username: String, val email: String, val avatar: String) : ProfileEvent
    data class ChangePassword(val currentPassword: String, val newPassword: String) : ProfileEvent
    data object PasswordChangeHandled : ProfileEvent
}
