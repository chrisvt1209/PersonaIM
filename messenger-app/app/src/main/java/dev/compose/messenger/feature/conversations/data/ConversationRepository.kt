package dev.compose.messenger.feature.conversations.data

import dev.compose.messenger.core.database.dao.ConversationDao
import dev.compose.messenger.core.database.dao.MessageDao
import dev.compose.messenger.core.datastore.PreferencesManager
import dev.compose.messenger.core.network.api.ConversationApi
import dev.compose.messenger.core.network.toUserMessage
import dev.compose.messenger.feature.conversations.data.mapper.toDetail
import dev.compose.messenger.feature.conversations.data.mapper.toDomain
import dev.compose.messenger.feature.conversations.data.mapper.toEntity
import dev.compose.messenger.feature.conversations.data.mapper.toInvite
import dev.compose.messenger.feature.conversations.domain.Conversation
import dev.compose.messenger.feature.conversations.domain.ConversationDetail
import dev.compose.messenger.feature.conversations.domain.GroupInvite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

interface ConversationRepository {
    fun getConversations(): Flow<List<Conversation>>
    suspend fun createConversation(userId: Long): Result<Conversation>
    suspend fun createGroup(title: String, memberUserIds: List<Long>): Result<Conversation>
    suspend fun syncConversations(): Result<Unit>
    suspend fun deleteConversation(id: String): Result<Unit>
    suspend fun getConversationDetail(id: String): Result<ConversationDetail>
    suspend fun getInvites(): Result<List<GroupInvite>>
    suspend fun acceptInvite(id: String): Result<Unit>
    suspend fun declineInvite(id: String): Result<Unit>
    suspend fun inviteToGroup(conversationId: String, userId: Long): Result<Unit>
}

class ConversationRepositoryImpl(
    private val api: ConversationApi,
    private val dao: ConversationDao,
    private val messageDao: MessageDao,
    private val preferencesManager: PreferencesManager
) : ConversationRepository {

    private suspend fun currentUserId(): Long? = preferencesManager.currentUserId.first()

    override fun getConversations(): Flow<List<Conversation>> {
        return dao.getAllConversations()
            .map { entities -> entities.map { it.toDomain() } }
            .onEach { if (it.isEmpty()) syncConversations() }
    }

    override suspend fun syncConversations(): Result<Unit> {
        return try {
            val userId = currentUserId() ?: return Result.success(Unit)
            val dtos = api.getConversations()
            dao.insertConversations(dtos.map { it.toEntity(userId) })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.toUserMessage()))
        }
    }

    override suspend fun createConversation(userId: Long): Result<Conversation> {
        return try {
            val dto = api.createConversation(CreateConversationRequest(userId))
            val entity = dto.toEntity(currentUserId())
            dao.insertConversations(listOf(entity))
            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception(e.toUserMessage()))
        }
    }

    override suspend fun createGroup(title: String, memberUserIds: List<Long>): Result<Conversation> {
        return try {
            val dto = api.createGroup(CreateGroupRequest(title, memberUserIds))
            val entity = dto.toEntity(currentUserId())
            dao.insertConversations(listOf(entity))
            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception(e.toUserMessage()))
        }
    }

    override suspend fun deleteConversation(id: String): Result<Unit> {
        val conversationId = id.toLongOrNull() ?: return Result.failure(Exception("Invalid ID"))
        return try {
            api.deleteConversation(conversationId)
            messageDao.clearMessagesForConversation(conversationId)
            dao.deleteConversation(conversationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.toUserMessage()))
        }
    }

    override suspend fun getConversationDetail(id: String): Result<ConversationDetail> {
        val conversationId = id.toLongOrNull() ?: return Result.failure(Exception("Invalid ID"))
        return try {
            val userId = currentUserId() ?: return Result.failure(Exception("Not signed in"))
            val dto = api.getConversation(conversationId)
            Result.success(dto.toDetail(userId))
        } catch (e: Exception) {
            Result.failure(Exception(e.toUserMessage()))
        }
    }

    override suspend fun getInvites(): Result<List<GroupInvite>> {
        return try {
            Result.success(api.getInvites().map { it.toInvite() })
        } catch (e: Exception) {
            Result.failure(Exception(e.toUserMessage()))
        }
    }

    override suspend fun acceptInvite(id: String): Result<Unit> {
        val conversationId = id.toLongOrNull() ?: return Result.failure(Exception("Invalid ID"))
        return try {
            api.acceptInvite(conversationId)
            syncConversations()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.toUserMessage()))
        }
    }

    override suspend fun declineInvite(id: String): Result<Unit> {
        val conversationId = id.toLongOrNull() ?: return Result.failure(Exception("Invalid ID"))
        return try {
            api.declineInvite(conversationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.toUserMessage()))
        }
    }

    override suspend fun inviteToGroup(conversationId: String, userId: Long): Result<Unit> {
        val id = conversationId.toLongOrNull() ?: return Result.failure(Exception("Invalid ID"))
        return try {
            api.invite(id, InviteRequest(userId))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.toUserMessage()))
        }
    }
}
