package dev.compose.messenger.core.network.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

@Serializable
data class AddFriendRequest(
    val friendUid: String
)

@Serializable
data class FriendDto(
    val id: Long,
    val username: String,
    val email: String
)

class FriendApi(private val client: HttpClient) {
    suspend fun getFriends(): List<FriendDto> {
        return client.get("friends").body()
    }

    suspend fun addFriend(request: AddFriendRequest): FriendDto {
        return client.post("friends") {
            setBody(request)
        }.body()
    }
}
