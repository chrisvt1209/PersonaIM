package dev.compose.messenger.feature.conversations.data.mapper

import androidx.compose.ui.graphics.Color
import dev.compose.messenger.core.database.entity.ConversationEntity
import dev.compose.messenger.feature.conversations.data.ConversationDto
import dev.compose.messenger.feature.conversations.domain.Conversation
import dev.compose.messenger.feature.conversations.domain.ConversationDetail
import dev.compose.messenger.feature.conversations.domain.GroupInvite
import dev.compose.messenger.feature.conversations.domain.Participant

fun ConversationDto.toEntity(currentUserId: Long?) = ConversationEntity(
    id = id,
    title = resolveTitle(currentUserId),
    isGroup = title != null,
    lastMessage = lastMessage,
    lastMessageTimestamp = lastMessageTimestamp,
    unreadCount = unreadCount,
    participantIds = participants
        .filter { it.userId != currentUserId }
        .joinToString(",") { it.userId.toString() }
)

fun ConversationDto.toDetail(currentUserId: Long?) = ConversationDetail(
    id = id.toString(),
    title = resolveTitle(currentUserId),
    isGroup = title != null,
    participants = participants.map { Participant(it.userId, it.username, it.status) }
)

fun ConversationDto.toInvite() = GroupInvite(
    id = id.toString(),
    title = title ?: "Group",
    memberCount = participants.size
)

private fun ConversationDto.resolveTitle(currentUserId: Long?): String {
    return title
        ?: participants.firstOrNull { it.userId != currentUserId }?.username
        ?: "Conversation"
}

fun ConversationEntity.toDomain() = Conversation(
    id = id.toString(),
    title = title,
    subtitle = "active now",
    participantNames = "", // We might need to fetch participants separately
    lastMessage = lastMessage ?: "",
    unreadCount = unreadCount,
    accentColor = Color(0xFFC41001),
    participantIds = participantIds.split(",").mapNotNull { it.toLongOrNull() }
)
