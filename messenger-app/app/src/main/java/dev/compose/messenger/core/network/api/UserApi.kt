package dev.compose.messenger.core.network.api

import dev.compose.messenger.feature.profile.domain.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Long,
    val username: String,
    val email: String,
    val bio: String? = null,
    val avatarUrl: String? = null
)

class UserApi(private val client: HttpClient) {
    suspend fun getCurrentUser(): UserDto {
        return client.get("users/me").body()
    }
}
