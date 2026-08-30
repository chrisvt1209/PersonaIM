package dev.compose.messenger.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val isGroup: Boolean,
    val lastMessage: String?,
    val lastMessageTimestamp: String?,
    val unreadCount: Int,
    val participantIds: String
)
