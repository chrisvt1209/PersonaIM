package dev.compose.messenger.feature.chat.data.mapper

import dev.compose.messenger.core.common.model.Message
import dev.compose.messenger.core.common.model.Sender
import dev.compose.messenger.core.database.entity.MessageEntity
import dev.compose.messenger.core.network.api.MessageDto

fun MessageDto.toEntity() = MessageEntity(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    senderName = "", // Fetch from somewhere if needed
    text = text,
    timestamp = sentAt,
    isFromMe = false // Determine from current user ID
)

fun MessageEntity.toDomain() = Message(
    id = id,
    sender = Sender.fromId(senderId),
    text = text,
    timestamp = timestamp
)
