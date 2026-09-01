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
            set(it.type, ConversationType.SINGLE)
            set(it.createdAt, it.createdAt.defaultValue())
        } as Long

        insertParticipant(conversationId, user1, ParticipantStatus.ACCEPTED, ParticipantRole.MEMBER)
        insertParticipant(conversationId, user2, ParticipantStatus.ACCEPTED, ParticipantRole.MEMBER)

        return findById(conversationId)!!
    }

    fun createGroup(
        creatorId: Long,
        title: String,
        memberUserIds: List<Long>
    ): Conversation {
        val conversationId = database.insertAndGenerateKey(Conversations) {
            set(it.title, title)
            set(it.type, ConversationType.GROUP)
            set(it.createdAt, it.createdAt.defaultValue())
        } as Long

        insertParticipant(conversationId, creatorId, ParticipantStatus.ACCEPTED, ParticipantRole.MAINTAINER)
        memberUserIds.forEach {
            insertParticipant(conversationId, it, ParticipantStatus.PENDING, ParticipantRole.MEMBER)
        }

        return findById(conversationId)!!
    }

    fun addParticipant(conversationId: Long, userId: Long, status: String) {
        insertParticipant(conversationId, userId, status, ParticipantRole.MEMBER)
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

    fun updateParticipantRole(conversationId: Long, userId: Long, role: String): Boolean {
        val updated = database.update(ConversationParticipants) {
            set(it.role, role)
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

    private fun insertParticipant(conversationId: Long, userId: Long, status: String, role: String) {
        database.insert(ConversationParticipants) {
            set(it.conversationId, conversationId)
            set(it.userId, userId)
            set(it.status, status)
            set(it.role, role)
        }
    }

    fun findById(
        conversationId: Long
    ): Conversation? {
        val conversationRow = database
            .from(Conversations)
            .select(Conversations.id, Conversations.title, Conversations.type)
            .where { Conversations.id eq conversationId }
            .map { it[Conversations.title] to it[Conversations.type] }
            .firstOrNull()

        val participants = database
            .from(ConversationParticipants)
            .innerJoin(Users, on = ConversationParticipants.userId eq Users.id)
            .select(Users.id, Users.username, ConversationParticipants.status, ConversationParticipants.role)
            .where { ConversationParticipants.conversationId eq conversationId }
            .map {
                Participant(
                    userId = it[Users.id]!!,
                    username = it[Users.username]!!,
                    status = it[ConversationParticipants.status]!!,
                    role = it[ConversationParticipants.role]!!
                )
            }

        if (participants.isEmpty() || conversationRow == null) {
            return null
        }

        return Conversation(
            id = conversationId,
            title = conversationRow.first,
            type = conversationRow.second ?: ConversationType.SINGLE,
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
