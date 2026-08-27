package dev.compose.messenger.feature.profile.data

import dev.compose.messenger.core.database.dao.UserDao
import dev.compose.messenger.core.datastore.PreferencesManager
import dev.compose.messenger.core.network.api.ChangePasswordRequest
import dev.compose.messenger.core.network.api.UpdateProfileRequest
import dev.compose.messenger.core.network.api.UserApi
import dev.compose.messenger.feature.auth.data.ErrorResponse
import dev.compose.messenger.feature.profile.data.mapper.toDomain
import dev.compose.messenger.feature.profile.data.mapper.toEntity
import dev.compose.messenger.feature.profile.domain.User
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.IOException

interface ProfileRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun updateProfile(username: String, email: String, avatar: String): Result<Unit>
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
    suspend fun syncProfile(): Result<Unit>
}

class ProfileRepositoryImpl(
    private val api: UserApi,
    private val dao: UserDao,
    private val preferencesManager: PreferencesManager
) : ProfileRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getCurrentUser(): Flow<User?> {
        return preferencesManager.currentUserId.flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                dao.getUserById(id).map { it?.toDomain() }
            }
        }
    }

    override suspend fun syncProfile(): Result<Unit> {
        return try {
            val dto = api.getCurrentUser()
            preferencesManager.saveCurrentUserId(dto.id)
            dao.insertUser(dto.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(username: String, email: String, avatar: String): Result<Unit> {
        return try {
            val dto = api.updateProfile(UpdateProfileRequest(username, email, avatar))
            dao.insertUser(dto.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.toSafeMessage()))
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            api.changePassword(ChangePasswordRequest(currentPassword, newPassword))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.toSafeMessage()))
        }
    }

    private suspend fun Exception.toSafeMessage(): String = when (this) {
        is ResponseException -> runCatching { response.body<ErrorResponse>().error }
            .getOrDefault("Something went wrong. Please try again.")
        is IOException -> "Can't reach server. Check your connection."
        else -> "Something went wrong. Please try again."
    }
}
