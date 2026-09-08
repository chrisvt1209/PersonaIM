package dev.compose.messenger.feature.profile.presentation

import dev.compose.messenger.feature.auth.data.AuthRepository
import dev.compose.messenger.feature.auth.data.AuthResponse
import dev.compose.messenger.feature.auth.data.LoginRequest
import dev.compose.messenger.feature.auth.data.RegisterRequest
import dev.compose.messenger.feature.profile.data.ProfileRepository
import dev.compose.messenger.feature.profile.domain.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

private class FakeProfileRepository(
    private val currentUser: Flow<User?> = MutableStateFlow(null),
    private val uploadAvatarResult: Result<Unit> = Result.success(Unit)
) : ProfileRepository {
    override fun getCurrentUser(): Flow<User?> = currentUser
    override suspend fun updateProfile(username: String, email: String, avatar: String): Result<Unit> = Result.success(Unit)
    override suspend fun uploadAvatar(imageBytes: ByteArray): Result<Unit> = uploadAvatarResult
    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = Result.success(Unit)
    override suspend fun syncProfile(): Result<Unit> = Result.success(Unit)
    override suspend fun getUser(id: Long): Result<User> = Result.success(fakeUser())
}

private class FakeAuthRepository : AuthRepository {
    override suspend fun login(request: LoginRequest): Result<AuthResponse> = throw NotImplementedError()
    override suspend fun register(request: RegisterRequest): Result<AuthResponse> = throw NotImplementedError()
    override suspend fun logout() {}
}

private fun fakeUser() = User(id = 1, username = "Joker", email = "joker@persona.dev", uid = "uid-1", avatar = "ann")

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `upload avatar success clears loading and error`() = runTest {
        val viewModel = ProfileViewModel(
            repository = FakeProfileRepository(),
            authRepository = FakeAuthRepository()
        )

        viewModel.onEvent(ProfileEvent.UploadAvatar(byteArrayOf(1, 2, 3)))

        val state = viewModel.uiState.value
        assertFalse(state.isUploadingAvatar)
        assertNull(state.avatarUploadError)
    }

    @Test
    fun `upload avatar failure surfaces error message`() = runTest {
        val viewModel = ProfileViewModel(
            repository = FakeProfileRepository(uploadAvatarResult = Result.failure(Exception("Avatar image too large"))),
            authRepository = FakeAuthRepository()
        )

        viewModel.onEvent(ProfileEvent.UploadAvatar(byteArrayOf(1, 2, 3)))

        val state = viewModel.uiState.value
        assertFalse(state.isUploadingAvatar)
        assertEquals("Avatar image too large", state.avatarUploadError)
    }
}
