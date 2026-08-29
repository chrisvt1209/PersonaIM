package common.websockets

import features.conversations.ConversationService
import features.messages.MessageService
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

fun Route.messageWebSocketRoutes(
    messageService: MessageService,
    conversationService: ConversationService,
    webSocketManager: WebSocketManager
) {
    authenticate("auth-jwt") {
        webSocket(
            "/conversations/{conversationId}/ws"
        ) {
            val userId =
                call.principal<JWTPrincipal>()
                    ?.payload?.subject?.toLongOrNull()

            if (userId == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Unauthorized"))
                return@webSocket
            }

            val conversationId =
                call.parameters["conversationId"]
                    ?.toLongOrNull()

            if (conversationId == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid conversation id"))
                return@webSocket
            }

            MessageWebSocket(
                messageService,
                conversationService,
                webSocketManager
            ).handle(
                conversationId = conversationId,
                userId = userId,
                session = this
            )
        }
    }
}
