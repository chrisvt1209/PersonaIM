package features.messages

import common.BadRequestException
import common.ForbiddenException
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
        if (text.isBlank()) {
            throw BadRequestException("Message cannot be empty")
        }

        if (!conversationService.isParticipant(conversationId, senderId)) {
            throw ForbiddenException("You are not a participant of this conversation")
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
        if (!conversationService.isParticipant(conversationId, userId)) {
            throw ForbiddenException("You are not a participant of this conversation")
        }

        return repository.findForConversation(conversationId)
    }
}