package features.messages

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: Long,
    val conversationId: Long,
    val senderId: Long,
    val text: String,
    val sentAt: String
)