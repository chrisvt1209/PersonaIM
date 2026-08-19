package features.messages

import org.ktorm.database.Database
import org.ktorm.dsl.*

class MessageRepository(
    private val database: Database
) {
    fun create(
        conversationId: Long,
        senderId: Long,
        text: String
    ): Message {
        database.insert(Messages) {
            set(it.conversationId, conversationId)
            set(it.senderId, senderId)
            set(it.text, text)
        }

        return database
            .from(Messages)
            .select()
            .where {
                (Messages.conversationId eq conversationId) and
                (Messages.senderId eq senderId) and
                (Messages.text eq text)
            }
            .orderBy(Messages.id.desc())
            .map {
                Message(
                    id = it[Messages.id]!!,
                    conversationId = it[Messages.conversationId]!!,
                    senderId = it[Messages.senderId]!!,
                    text = it[Messages.text]!!,
                    sentAt = it[Messages.sentAt]!!.toString()
                )
            }
            .first()
    }

    fun findForConversation(
        conversationId: Long,
        limit: Int = 50
    ): List<Message> {
        return database
            .from(Messages)
            .select()
            .where {
                Messages.conversationId eq conversationId
            }
            .orderBy(Messages.sentAt.asc())
            .limit(limit)
            .map {
                Message(
                    id = it[Messages.id]!!,
                    conversationId = it[Messages.conversationId]!!,
                    senderId = it[Messages.senderId]!!,
                    text = it[Messages.text]!!,
                    sentAt = it[Messages.sentAt]!!.toString()
                )
            }
    }
}