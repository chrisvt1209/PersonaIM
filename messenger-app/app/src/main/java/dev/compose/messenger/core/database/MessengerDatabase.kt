package dev.compose.messenger.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.compose.messenger.core.database.dao.ConversationDao
import dev.compose.messenger.core.database.dao.MessageDao
import dev.compose.messenger.core.database.dao.UserDao
import dev.compose.messenger.core.database.entity.ConversationEntity
import dev.compose.messenger.core.database.entity.MessageEntity
import dev.compose.messenger.core.database.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ConversationEntity::class,
        MessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MessengerDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}
