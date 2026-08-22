package dev.compose.messenger.feature.conversations.data.mapper

import androidx.compose.ui.graphics.Color
import dev.compose.messenger.core.database.entity.ConversationEntity
import dev.compose.messenger.feature.conversations.data.ConversationDto
import dev.compose.messenger.feature.conversations.domain.Conversation

fun ConversationDto.toEntity() = ConversationEntity(
    id = id,
    title = title,
    lastMessage = lastMessage,
    lastMessageTimestamp = lastMessageTimestamp,
    unreadCount = unreadCount
)

fun ConversationEntity.toDomain() = Conversation(
    id = id.toString(),
    title = title ?: "Group",
    subtitle = "active now",
    participantNames = "", // We might need to fetch participants separately
    lastMessage = lastMessage ?: "",
    unreadCount = unreadCount,
    accentColor = Color(0xFFC41001),
    avatarUrls = emptyList()
)
