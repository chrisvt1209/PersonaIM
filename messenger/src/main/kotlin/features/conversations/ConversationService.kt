package features.conversations

class ConversationService(
    private val repository: ConversationRepository
) {
    fun create(
        userId: Long,
        otherUserId: Long
    ): Conversation {
        require(userId != otherUserId) {
            "You cannot create a conversation with yourself"
        }

        return repository.create(
            user1 = userId,
            user2 = otherUserId
        )
    }

    fun get(
        conversationId: Long
    ): Conversation? {
        return repository.findById(conversationId)
    }

    fun getForUser(userId: Long): List<Conversation> {
        return repository.findForUser(userId)
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
}