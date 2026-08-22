package dev.compose.messenger.feature.conversations.data

import kotlinx.serialization.Serializable

@Serializable
data class ConversationDto(
    val id: Long,
    val participantIds: List<Long>,
    val title: String? = null,
    val lastMessage: String? = null,
    val lastMessageTimestamp: String? = null,
    val unreadCount: Int = 0
)

@Serializable
data class CreateConversationRequest(
    val userId: Long
)
