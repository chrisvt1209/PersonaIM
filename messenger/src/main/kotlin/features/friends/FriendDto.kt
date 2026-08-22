package features.friends

import kotlinx.serialization.Serializable

@Serializable
data class AddFriendRequest(
    val friendEmail: String
)

@Serializable
data class FriendResponse(
    val id: Long,
    val username: String,
    val email: String
)
