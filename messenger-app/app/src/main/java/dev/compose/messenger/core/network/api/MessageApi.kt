package dev.compose.messenger.core.network.api

import dev.compose.messenger.feature.chat.data.ChatRepository // Wait, I need DTOs
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    val id: Long,
    val conversationId: Long,
    val senderId: Long,
    val text: String,
    val sentAt: String
)

@Serializable
data class SendMessageRequest(
    val text: String
)

class MessageApi(private val client: HttpClient) {
    suspend fun getMessages(conversationId: Long): List<MessageDto> {
        return client.get("conversations/$conversationId/messages").body()
    }

    suspend fun sendMessage(conversationId: Long, request: SendMessageRequest): MessageDto {
        return client.post("conversations/$conversationId/messages") {
            setBody(request)
        }.body()
    }
}
