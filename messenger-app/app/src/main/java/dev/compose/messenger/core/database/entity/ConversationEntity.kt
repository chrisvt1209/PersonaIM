package dev.compose.messenger.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: Long,
    val title: String?,
    val lastMessage: String?,
    val lastMessageTimestamp: String?,
    val unreadCount: Int
)
