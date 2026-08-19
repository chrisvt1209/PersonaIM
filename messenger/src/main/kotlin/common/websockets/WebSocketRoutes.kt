package common.websockets

import features.conversations.ConversationService
import features.messages.MessageService
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

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
                call.principal<UserIdPrincipal>()
                    ?.name
                    ?.toLong()
                    ?: return@webSocket

            val conversationId =
                call.parameters["conversationId"]
                    ?.toLongOrNull()
                    ?: return@webSocket

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