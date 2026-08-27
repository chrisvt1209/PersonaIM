package dev.compose.messenger.core.network.api

import dev.compose.messenger.feature.profile.domain.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Long,
    val username: String,
    val email: String,
    val uid: String,
    val avatar: String,
    val bio: String? = null
)

@Serializable
data class UpdateProfileRequest(
    val username: String,
    val email: String,
    val avatar: String
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

class UserApi(private val client: HttpClient) {
    suspend fun getCurrentUser(): UserDto {
        return client.get("users/me").body()
    }

    suspend fun updateProfile(request: UpdateProfileRequest): UserDto {
        return client.put("users/me") {
            setBody(request)
        }.body()
    }

    suspend fun changePassword(request: ChangePasswordRequest) {
        client.put("users/me/password") {
            setBody(request)
        }
    }
}
