package features.conversations

/**
 * Pure permission policy for group actions. Takes an already-fetched [Conversation] so it has
 * no database dependency, which keeps it trivially unit-testable.
 */
object GroupPermissions {
    fun canInvite(conversation: Conversation, userId: Long): Boolean {
        if (!conversation.isGroup()) return false
        val role = conversation.activeRoleOf(userId) ?: return false
        return role == ParticipantRole.INVITER || role == ParticipantRole.MAINTAINER
    }

    fun canRemove(conversation: Conversation, removerId: Long, targetUserId: Long): Boolean {
        if (!conversation.isGroup()) return false
        if (removerId == targetUserId) return false
        if (conversation.activeRoleOf(removerId) != ParticipantRole.MAINTAINER) return false
        val targetRole = conversation.roleOf(targetUserId) ?: return false
        return targetRole != ParticipantRole.MAINTAINER
    }

    fun canChangeRole(conversation: Conversation, changerId: Long, targetUserId: Long): Boolean {
        if (!conversation.isGroup()) return false
        if (conversation.activeRoleOf(changerId) != ParticipantRole.MAINTAINER) return false
        val targetRole = conversation.roleOf(targetUserId) ?: return false
        return targetRole != ParticipantRole.MAINTAINER
    }

    fun canDeleteGroup(conversation: Conversation, userId: Long): Boolean {
        if (!conversation.isGroup()) return false
        return conversation.activeRoleOf(userId) == ParticipantRole.MAINTAINER
    }
}

private fun Conversation.isGroup(): Boolean = type == ConversationType.GROUP

private fun Conversation.activeRoleOf(userId: Long): String? =
    participants.firstOrNull { it.userId == userId && it.status == ParticipantStatus.ACCEPTED }?.role

private fun Conversation.roleOf(userId: Long): String? =
    participants.firstOrNull { it.userId == userId }?.role
