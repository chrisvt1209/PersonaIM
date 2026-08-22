package dev.compose.messenger.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: Long,
    val conversationId: Long,
    val senderId: Long,
    val senderName: String, // Simplified for now, or use a proper relation
    val text: String,
    val timestamp: String,
    val isFromMe: Boolean
)
