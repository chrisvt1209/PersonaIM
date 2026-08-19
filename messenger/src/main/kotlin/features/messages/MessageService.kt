package features.messages

import features.conversations.ConversationService

class MessageService(
    private val repository: MessageRepository,
    private val conversationService: ConversationService,
) {
    fun send(
        conversationId: Long,
        senderId: Long,
        text: String
    ): Message {
        require(text.isNotBlank()) {
            "Message cannot be empty"
        }

        require(
            conversationService.isParticipant(
                conversationId,
                senderId
            )
        ) {
            "You are not a participant of this conversation"
        }

        return repository.create(
            conversationId = conversationId,
            senderId = senderId,
            text = text
        )
    }

    fun getMessages(
        conversationId: Long,
        userId: Long
    ): List<Message> {
        require(
            conversationService.isParticipant(
                conversationId,
                userId
            )
        ) {
            "You are not a participant of this conversation"
        }

        return repository.findForConversation(conversationId)
    }
}