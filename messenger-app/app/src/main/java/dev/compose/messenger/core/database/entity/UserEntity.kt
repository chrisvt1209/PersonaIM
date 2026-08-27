package dev.compose.messenger.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Long,
    val username: String,
    val email: String,
    val uid: String,
    val bio: String?,
    val avatarUrl: String?
)
