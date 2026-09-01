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
        val conversation = requireGroup(conversationId)

        if (!GroupPermissions.canInvite(conversation, inviterId)) {
            throw ForbiddenException("You don't have permission to invite people to this group")
        }
        if (conversation.participants.any { it.userId == inviteeId }) {
            throw ConflictException("That person is already in this group")
        }

        repository.addParticipant(conversationId, inviteeId, ParticipantStatus.PENDING)
        return repository.findById(conversationId)!!
    }

    fun removeMember(
        conversationId: Long,
        removerId: Long,
        targetUserId: Long
    ): Conversation {
        val conversation = requireGroup(conversationId)

        if (conversation.participants.none { it.userId == targetUserId }) {
            throw NotFoundException("That person is not in this group")
        }
        if (!GroupPermissions.canRemove(conversation, removerId, targetUserId)) {
            throw ForbiddenException("You don't have permission to remove that person")
        }

        repository.removeParticipant(conversationId, targetUserId)
        return repository.findById(conversationId)!!
    }

    fun changeRole(
        conversationId: Long,
        changerId: Long,
        targetUserId: Long,
        newRole: String
    ): Conversation {
        if (newRole !in ParticipantRole.ALL) {
            throw BadRequestException("Invalid role")
        }

        val conversation = requireGroup(conversationId)

        if (conversation.participants.none { it.userId == targetUserId }) {
            throw NotFoundException("That person is not in this group")
        }
        if (!GroupPermissions.canChangeRole(conversation, changerId, targetUserId)) {
            throw ForbiddenException("You don't have permission to change that person's role")
        }

        repository.updateParticipantRole(conversationId, targetUserId, newRole)
        return repository.findById(conversationId)!!
    }

    fun leave(conversationId: Long, userId: Long) {
        val conversation = requireGroup(conversationId)

        if (conversation.participants.none { it.userId == userId }) {
            throw NotFoundException("You are not part of this group")
        }

        repository.removeParticipant(conversationId, userId)
    }

    private fun requireGroup(conversationId: Long): Conversation {
        val conversation = repository.findById(conversationId)
            ?: throw NotFoundException("Conversation not found")
        if (conversation.type != ConversationType.GROUP) {
            throw BadRequestException("This action is only available in group conversations")
        }
        return conversation
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
        val conversation = repository.findById(conversationId)
            ?: throw NotFoundException("Conversation not found")

        if (conversation.type == ConversationType.GROUP) {
            if (!GroupPermissions.canDeleteGroup(conversation, userId)) {
                throw ForbiddenException("Only maintainers can delete this group")
            }
        } else if (!isParticipant(conversationId, userId)) {
            throw NotFoundException("Conversation not found")
        }

        repository.delete(conversationId)
    }
}
