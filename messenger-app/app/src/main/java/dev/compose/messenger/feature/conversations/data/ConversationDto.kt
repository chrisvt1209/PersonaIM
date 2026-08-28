package dev.compose.messenger.feature.conversations.data

import kotlinx.serialization.Serializable

@Serializable
data class ParticipantDto(
    val userId: Long,
    val username: String,
    val status: String
)

@Serializable
data class ConversationDto(
    val id: Long,
    val title: String? = null,
    val participants: List<ParticipantDto> = emptyList(),
    val lastMessage: String? = null,
    val lastMessageTimestamp: String? = null,
    val unreadCount: Int = 0
)

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
