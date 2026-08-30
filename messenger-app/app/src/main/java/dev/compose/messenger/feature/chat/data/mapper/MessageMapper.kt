package dev.compose.messenger.feature.chat.data.mapper

import dev.compose.messenger.core.common.model.Message
import dev.compose.messenger.core.common.model.Sender
import dev.compose.messenger.core.database.entity.MessageEntity
import dev.compose.messenger.core.network.api.MessageDto

fun MessageDto.toEntity(currentUserId: Long?) = MessageEntity(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    senderName = "", // Fetch from somewhere if needed
    text = text,
    timestamp = sentAt,
    isFromMe = currentUserId != null && senderId == currentUserId
)

fun MessageEntity.toDomain() = Message(
    id = id,
    sender = Sender.fromId(senderId),
    senderId = senderId,
    text = text,
    timestamp = timestamp,
    isFromMe = isFromMe
)
