package features.conversations

import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.support.postgresql.defaultValue

class ConversationRepository(
    private val database: Database
) {
    fun create(
        user1: Long,
        user2: Long
    ): Conversation {
        val conversationId = database.insertAndGenerateKey(Conversations) {
            set(it.createdAt, it.createdAt.defaultValue())
        } as Long

        database.insert(ConversationParticipants) {
            set(it.conversationId, conversationId)
            set(it.userId, user1)
        }

        database.insert(ConversationParticipants) {
            set(it.conversationId, conversationId)
            set(it.userId, user2)
        }

        return Conversation(
            id = conversationId,
            participantIds = listOf(user1, user2)
        )
    }

    fun findById(
        conversationId: Long
    ): Conversation? {
        val participants =
            database
                .from(ConversationParticipants)
                .select()
                .where { ConversationParticipants.conversationId eq conversationId }
                .map {
                    it[ConversationParticipants.userId]!!
                }

        if (participants.isEmpty()) {
            return null
        }

        return Conversation(
            id = conversationId,
            participantIds = participants
        )
    }

    fun userIsParticipant(
        conversationId: Long,
        userId: Long
    ): Boolean {
        return database
            .from(ConversationParticipants)
            .select()
            .where {
                (ConversationParticipants.conversationId eq conversationId) and
                        (ConversationParticipants.userId eq userId)
            }
            .map { 1 }
            .isNotEmpty()
    }

    fun findForUser(userId: Long): List<Conversation> {
        val conversationIds = database
            .from(ConversationParticipants)
            .select(ConversationParticipants.conversationId)
            .where { ConversationParticipants.userId eq userId }
            .map { it[ConversationParticipants.conversationId]!! }

        return conversationIds.mapNotNull { findById(it) }
    }
}
