package common.websockets

import common.AppException
import features.conversations.ConversationService
import features.messages.MessageService
import features.messages.SendMessageRequest
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(MessageWebSocket::class.java)

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
          session.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Not a participant of this conversation"))
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

                try {
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
                } catch (e: SerializationException) {
                    logger.warn("Malformed message frame on conversation $conversationId", e)
                    session.send(Frame.Text("""{"error":"Malformed message"}"""))
                } catch (e: AppException) {
                    session.send(Frame.Text("""{"error":"${e.message}"}"""))
                }
            }
        } catch (e: Exception) {
            logger.error("Unexpected error on conversation $conversationId websocket", e)
        } finally {
            webSocketManager.disconnect(
                conversationId,
                session = session
            )
        }
    }
}
