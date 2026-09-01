package dev.compose.messenger.core.network.api

import dev.compose.messenger.feature.conversations.data.ChangeRoleRequest
import dev.compose.messenger.feature.conversations.data.ConversationDto
import dev.compose.messenger.feature.conversations.data.CreateConversationRequest
import dev.compose.messenger.feature.conversations.data.CreateGroupRequest
import dev.compose.messenger.feature.conversations.data.InviteRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class ConversationApi(private val client: HttpClient) {
    suspend fun getConversations(): List<ConversationDto> {
        return client.get("conversations").body()
    }

    suspend fun getConversation(id: Long): ConversationDto {
        return client.get("conversations/$id").body()
    }

    suspend fun createConversation(request: CreateConversationRequest): ConversationDto {
        return client.post("conversations") {
            setBody(request)
        }.body()
    }

    suspend fun createGroup(request: CreateGroupRequest): ConversationDto {
        return client.post("conversations/groups") {
            setBody(request)
        }.body()
    }

    suspend fun deleteConversation(id: Long) {
        client.delete("conversations/$id")
    }

    suspend fun getInvites(): List<ConversationDto> {
        return client.get("conversations/invites").body()
    }

    suspend fun acceptInvite(id: Long) {
        client.post("conversations/$id/accept")
    }

    suspend fun declineInvite(id: Long) {
        client.post("conversations/$id/decline")
    }

    suspend fun invite(id: Long, request: InviteRequest) {
        client.post("conversations/$id/invite") {
            setBody(request)
        }
    }

    suspend fun removeMember(conversationId: Long, userId: Long): ConversationDto {
        return client.delete("conversations/$conversationId/members/$userId").body()
    }

    suspend fun changeRole(conversationId: Long, userId: Long, request: ChangeRoleRequest): ConversationDto {
        return client.put("conversations/$conversationId/members/$userId/role") {
            setBody(request)
        }.body()
    }

    suspend fun leaveGroup(conversationId: Long) {
        client.post("conversations/$conversationId/leave")
    }
}
