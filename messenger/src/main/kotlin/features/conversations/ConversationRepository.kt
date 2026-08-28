package features.conversations

import features.users.Users
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

        insertParticipant(conversationId, user1, ParticipantStatus.ACCEPTED)
        insertParticipant(conversationId, user2, ParticipantStatus.ACCEPTED)

        return findById(conversationId)!!
    }

    fun createGroup(
        creatorId: Long,
        title: String,
        memberUserIds: List<Long>
    ): Conversation {
        val conversationId = database.insertAndGenerateKey(Conversations) {
            set(it.title, title)
            set(it.createdAt, it.createdAt.defaultValue())
        } as Long

        insertParticipant(conversationId, creatorId, ParticipantStatus.ACCEPTED)
        memberUserIds.forEach { insertParticipant(conversationId, it, ParticipantStatus.PENDING) }

        return findById(conversationId)!!
    }

    fun addParticipant(conversationId: Long, userId: Long, status: String) {
        insertParticipant(conversationId, userId, status)
    }

    fun updateParticipantStatus(conversationId: Long, userId: Long, status: String): Boolean {
        val updated = database.update(ConversationParticipants) {
            set(it.status, status)
            where {
                (it.conversationId eq conversationId) and (it.userId eq userId)
            }
        }
        return updated > 0
    }

    fun removeParticipant(conversationId: Long, userId: Long): Boolean {
        val deleted = database.delete(ConversationParticipants) {
            (it.conversationId eq conversationId) and (it.userId eq userId)
        }
        return deleted > 0
    }

    private fun insertParticipant(conversationId: Long, userId: Long, status: String) {
        database.insert(ConversationParticipants) {
            set(it.conversationId, conversationId)
            set(it.userId, userId)
            set(it.status, status)
        }
    }

    fun findById(
        conversationId: Long
    ): Conversation? {
        val title = database
            .from(Conversations)
            .select(Conversations.id, Conversations.title)
            .where { Conversations.id eq conversationId }
            .map { it[Conversations.title] }
            .firstOrNull()

        // firstOrNull() above returns null both when there's no matching row and when
        // there IS a row but its title column is legitimately null (1:1 conversation),
        // so re-check existence separately via the participant rows.
        val participants = database
            .from(ConversationParticipants)
            .innerJoin(Users, on = ConversationParticipants.userId eq Users.id)
            .select(Users.id, Users.username, ConversationParticipants.status)
            .where { ConversationParticipants.conversationId eq conversationId }
            .map {
                Participant(
                    userId = it[Users.id]!!,
                    username = it[Users.username]!!,
                    status = it[ConversationParticipants.status]!!
                )
            }

        if (participants.isEmpty()) {
            return null
        }

        return Conversation(
            id = conversationId,
            title = title,
            participants = participants
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
                    (ConversationParticipants.userId eq userId) and
                    (ConversationParticipants.status eq ParticipantStatus.ACCEPTED)
            }
            .map { 1 }
            .isNotEmpty()
    }

    fun findForUser(userId: Long, status: String): List<Conversation> {
        val conversationIds = database
            .from(ConversationParticipants)
            .select(ConversationParticipants.conversationId)
            .where {
                (ConversationParticipants.userId eq userId) and
                    (ConversationParticipants.status eq status)
            }
            .map { it[ConversationParticipants.conversationId]!! }

        return conversationIds.mapNotNull { findById(it) }
    }

    fun findBetween(user1: Long, user2: Long): Conversation? {
        val conversationIds = database
            .from(ConversationParticipants)
            .select(ConversationParticipants.conversationId)
            .where { ConversationParticipants.userId eq user1 }
            .map { it[ConversationParticipants.conversationId]!! }

        for (conversationId in conversationIds) {
            val conversation = findById(conversationId) ?: continue
            if (conversation.title == null &&
                conversation.participants.size == 2 &&
                conversation.participants.any { it.userId == user2 }
            ) {
                return conversation
            }
        }

        return null
    }

    fun delete(conversationId: Long) {
        database.delete(Conversations) { it.id eq conversationId }
    }
}
