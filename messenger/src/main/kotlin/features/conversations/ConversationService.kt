package features.conversations

import common.BadRequestException
import common.ConflictException
import common.ForbiddenException
import common.NotFoundException

class ConversationService(
    private val repository: ConversationRepository
) {
    fun create(
        userId: Long,
        otherUserId: Long
    ): Conversation {
        if (userId == otherUserId) {
            throw BadRequestException("You cannot create a conversation with yourself")
        }

        repository.findBetween(userId, otherUserId)?.let { return it }

        return repository.create(
            user1 = userId,
            user2 = otherUserId
        )
    }

    fun createGroup(
        creatorId: Long,
        title: String,
        memberUserIds: List<Long>
    ): Conversation {
        if (title.isBlank()) {
            throw BadRequestException("Group name cannot be empty")
        }

        val members = memberUserIds.filter { it != creatorId }.distinct()
        if (members.isEmpty()) {
            throw BadRequestException("Pick at least one friend to invite")
        }

        return repository.createGroup(creatorId, title.trim(), members)
    }

    fun invite(
        conversationId: Long,
        inviterId: Long,
        inviteeId: Long
    ): Conversation {
        val conversation = repository.findById(conversationId)
            ?: throw NotFoundException("Conversation not found")

        if (conversation.title == null) {
            throw BadRequestException("Can only invite people to a group")
        }
        if (!isParticipant(conversationId, inviterId)) {
            throw ForbiddenException("You are not part of this group")
        }
        if (conversation.participants.any { it.userId == inviteeId }) {
            throw ConflictException("That person is already in this group")
        }

        repository.addParticipant(conversationId, inviteeId, ParticipantStatus.PENDING)
        return repository.findById(conversationId)!!
    }

    fun acceptInvite(conversationId: Long, userId: Long) {
        if (!repository.updateParticipantStatus(conversationId, userId, ParticipantStatus.ACCEPTED)) {
            throw NotFoundException("Invite not found")
        }
    }

    fun declineInvite(conversationId: Long, userId: Long) {
        if (!repository.removeParticipant(conversationId, userId)) {
            throw NotFoundException("Invite not found")
        }
    }

    fun get(
        conversationId: Long
    ): Conversation? {
        return repository.findById(conversationId)
    }

    fun getForUser(userId: Long): List<Conversation> {
        return repository.findForUser(userId, ParticipantStatus.ACCEPTED)
    }

    fun getInvitesForUser(userId: Long): List<Conversation> {
        return repository.findForUser(userId, ParticipantStatus.PENDING)
    }

    fun isParticipant(
        conversationId: Long,
        userId: Long
    ): Boolean {
        return repository.userIsParticipant(
            conversationId,
            userId
        )
    }

    fun delete(conversationId: Long, userId: Long) {
        if (!isParticipant(conversationId, userId)) {
            throw NotFoundException("Conversation not found")
        }

        repository.delete(conversationId)
    }
}
