package dev.sirhcvt.features.messages

import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequest(
    val conversationId: Long,
    val text: String
)

@Serializable
data class MessageResponse(
    val id: Long,
    val senderId: Long,
    val conversationId: Long,
    val text: String,
    val sentAt: Long
)