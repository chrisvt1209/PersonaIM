package dev.compose.messenger.feature.chat.data

import dev.compose.messenger.core.common.model.Message
import dev.compose.messenger.core.database.dao.MessageDao
import dev.compose.messenger.core.datastore.PreferencesManager
import dev.compose.messenger.core.network.WebSocketService
import dev.compose.messenger.core.network.api.MessageApi
import dev.compose.messenger.core.network.api.MessageDto
import dev.compose.messenger.core.network.api.SendMessageRequest
import dev.compose.messenger.core.network.toUserMessage
import dev.compose.messenger.feature.chat.data.mapper.toDomain
import dev.compose.messenger.feature.chat.data.mapper.toEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

interface ChatRepository {
    fun getMessages(conversationId: String): Flow<List<Message>>
    suspend fun sendMessage(conversationId: String, text: String): Result<Unit>
    suspend fun syncMessages(conversationId: String): Result<Unit>
}

class ChatRepositoryImpl(
    private val api: MessageApi,
    private val dao: MessageDao,
    private val webSocketService: WebSocketService,
    private val preferencesManager: PreferencesManager
) : ChatRepository {
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            webSocketService.messages.collect { jsonString ->
                try {
                    val messageDto = Json.decodeFromString<MessageDto>(jsonString)
                    dao.insertMessages(listOf(messageDto.toEntity(currentUserId())))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun currentUserId(): Long? = preferencesManager.currentUserId.first()

    override fun getMessages(conversationId: String): Flow<List<Message>> {
        val id = conversationId.toLongOrNull() ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return dao.getMessagesForConversation(id)
            .map { entities -> entities.map { it.toDomain() } }
            .onEach { if (it.isEmpty()) syncMessages(conversationId) }
    }

    override suspend fun syncMessages(conversationId: String): Result<Unit> {
        val id = conversationId.toLongOrNull() ?: return Result.failure(Exception("Invalid ID"))
        return try {
            val userId = currentUserId()
            val dtos = api.getMessages(id)
            dao.insertMessages(dtos.map { it.toEntity(userId) })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.toUserMessage()))
        }
    }

    override suspend fun sendMessage(conversationId: String, text: String): Result<Unit> {
        val id = conversationId.toLongOrNull() ?: return Result.failure(Exception("Invalid ID"))
        return try {
            val dto = api.sendMessage(id, SendMessageRequest(text))
            dao.insertMessages(listOf(dto.toEntity(currentUserId())))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.toUserMessage()))
        }
    }
}
