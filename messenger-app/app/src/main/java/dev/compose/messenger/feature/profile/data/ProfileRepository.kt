package dev.compose.messenger.feature.profile.data

import dev.compose.messenger.core.database.dao.UserDao
import dev.compose.messenger.core.network.api.UserApi
import dev.compose.messenger.feature.profile.data.mapper.toDomain
import dev.compose.messenger.feature.profile.data.mapper.toEntity
import dev.compose.messenger.feature.profile.domain.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

interface ProfileRepository {
    fun getCurrentUser(): Flow<User>
    suspend fun updateProfile(username: String, bio: String): Result<Unit>
    suspend fun syncProfile(): Result<Unit>
}

class ProfileRepositoryImpl(
    private val api: UserApi,
    private val dao: UserDao
) : ProfileRepository {
    override fun getCurrentUser(): Flow<User> {
        return dao.getUserById(1) // Assuming current user has ID 1 for now
            .onEach { if (it == null) syncProfile() }
            .filterNotNull()
            .map { it.toDomain() }
    }

    override suspend fun syncProfile(): Result<Unit> {
        return try {
            val dto = api.getCurrentUser()
            dao.insertUser(dto.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(username: String, bio: String): Result<Unit> {
        // TODO: Implement update profile API call
        return Result.success(Unit)
    }
}
