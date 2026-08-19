package common.websockets

import features.conversations.ConversationService
import features.messages.MessageService
import features.messages.SendMessageRequest
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json

class MessageWebSocket(
    private val messageService: MessageService,
    private val conversationService: ConversationService,
    private val webSocketManager: WebSocketManager
) {
    suspend fun handle(
        conversationId: Long,
        userId: Long,
        session: DefaultWebSocketServerSession
    ) {
      if(
          !conversationService.isParticipant(
              conversationId,
              userId
            )
          ) {
          session.close()
          return
      }

        webSocketManager.connect(
            conversationId,
            session
        )

        try {
            for (frame in session.incoming) {
                if (frame !is Frame.Text) {
                    continue
                }

                val request =
                    Json.decodeFromString<SendMessageRequest>(
                        frame.readText()
                    )

                val message =
                    messageService.send(
                        conversationId = conversationId,
                        senderId = userId,
                        text = request.text
                    )

                val json =
                    Json.encodeToString(message)

                webSocketManager.broadcast(
                    conversationId,
                    json
                )
            }
        } finally {
            webSocketManager.disconnect(
                conversationId,
                session = session
            )
        }
    }
}