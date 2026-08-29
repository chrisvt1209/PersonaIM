package dev.compose.messenger.feature.friends.data

import dev.compose.messenger.core.database.dao.FriendDao
import dev.compose.messenger.core.network.api.AddFriendRequest
import dev.compose.messenger.core.network.api.FriendApi
import dev.compose.messenger.core.network.toUserMessage
import dev.compose.messenger.feature.friends.data.mapper.toDomain
import dev.compose.messenger.feature.friends.data.mapper.toEntity
import dev.compose.messenger.feature.friends.domain.Friend
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

interface FriendRepository {
    fun getFriends(): Flow<List<Friend>>
    suspend fun addFriend(uid: String): Result<Unit>
    suspend fun syncFriends(): Result<Unit>
}

class FriendRepositoryImpl(
    private val api: FriendApi,
    private val dao: FriendDao
) : FriendRepository {
    override fun getFriends(): Flow<List<Friend>> {
        return dao.getAllFriends()
            .map { entities -> entities.map { it.toDomain() } }
            .onEach { if (it.isEmpty()) syncFriends() }
    }

    override suspend fun syncFriends(): Result<Unit> {
        return try {
            val dtos = api.getFriends()
            dao.insertFriends(dtos.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.toUserMessage()))
        }
    }

    override suspend fun addFriend(uid: String): Result<Unit> {
        return try {
            val dto = api.addFriend(AddFriendRequest(uid))
            dao.insertFriend(dto.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.toUserMessage()))
        }
    }
}
