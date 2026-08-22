package dev.compose.messenger.feature.conversations.data

import dev.compose.messenger.core.database.dao.ConversationDao
import dev.compose.messenger.core.network.api.ConversationApi
import dev.compose.messenger.feature.conversations.data.mapper.toDomain
import dev.compose.messenger.feature.conversations.data.mapper.toEntity
import dev.compose.messenger.feature.conversations.domain.Conversation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

interface ConversationRepository {
    fun getConversations(): Flow<List<Conversation>>
    suspend fun createConversation(userId: Long): Result<Unit>
    suspend fun syncConversations(): Result<Unit>
}

class ConversationRepositoryImpl(
    private val api: ConversationApi,
    private val dao: ConversationDao
) : ConversationRepository {
    override fun getConversations(): Flow<List<Conversation>> {
        return dao.getAllConversations()
            .map { entities -> entities.map { it.toDomain() } }
            .onEach { if (it.isEmpty()) syncConversations() }
    }

    override suspend fun syncConversations(): Result<Unit> {
        return try {
            val dtos = api.getConversations()
            dao.insertConversations(dtos.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createConversation(userId: Long): Result<Unit> {
        return try {
            val dto = api.createConversation(CreateConversationRequest(userId))
            dao.insertConversations(listOf(dto.toEntity()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
