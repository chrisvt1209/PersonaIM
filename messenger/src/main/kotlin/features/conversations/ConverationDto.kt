package features.conversations

import kotlinx.serialization.Serializable

@Serializable
data class CreateConversationRequest(
    val userId: Long
)

@Serializable
data class CreateGroupRequest(
    val title: String,
    val memberUserIds: List<Long>
)

@Serializable
data class InviteRequest(
    val userId: Long
)
