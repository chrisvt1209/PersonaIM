package features.conversations

import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.timestamp

object Conversations : Table<Nothing>("conversations") {
    val id = long("id").primaryKey()
    val createdAt = timestamp("created_at")
}

object ConversationParticipants : Table<Nothing>("conversation_participants") {
    val conversationId = long("conversation_id")
    val userId = long("user_id")
}