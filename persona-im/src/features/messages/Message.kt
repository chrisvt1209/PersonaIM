package dev.sirhcvt.features.messages

data class Message(
    val id: Long,
    val senderId: Long,
    val conversationId: Long,
    val text: String,
    val sentAt: Long
)