package dev.compose.messenger.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey val id: Long,
    val username: String,
    val email: String
)
