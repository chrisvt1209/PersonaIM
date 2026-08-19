package features.messages

import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.text
import org.ktorm.schema.timestamp

object Messages : Table<Nothing>("messages") {
    val id = long("id").primaryKey()
    val conversationId = long("conversation_id")
    val senderId = long("sender_id")
    val text = text("content")
    val sentAt = timestamp("sent_at")
}