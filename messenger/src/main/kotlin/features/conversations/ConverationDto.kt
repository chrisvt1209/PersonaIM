package features.conversations

import kotlinx.serialization.Serializable

@Serializable
data class CreateConversationRequest(
    val userId: Long
)
