package dev.compose.messenger.core.network.api

import dev.compose.messenger.feature.conversations.data.ConversationDto
import dev.compose.messenger.feature.conversations.data.CreateConversationRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class ConversationApi(private val client: HttpClient) {
    suspend fun getConversations(): List<ConversationDto> {
        return client.get("conversations").body()
    }

    suspend fun createConversation(request: CreateConversationRequest): ConversationDto {
        return client.post("conversations") {
            setBody(request)
        }.body()
    }

    suspend fun deleteConversation(id: Long) {
        client.delete("conversations/$id")
    }
}
