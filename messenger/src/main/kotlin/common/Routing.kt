package common

import common.websockets.WebSocketManager
import common.websockets.messageWebSocketRoutes
import features.auth.AuthService
import features.auth.authRoutes
import features.conversations.ConversationService
import features.conversations.conversationRoutes
import features.messages.MessageService
import features.messages.messageRoutes
import features.users.UserRepository
import features.users.userRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get

fun Application.configureRouting() {

    val userRepository = get<UserRepository>()
    val conversationService = get<ConversationService>()
    val messageService = get<MessageService>()
    val webSocketManager = get<WebSocketManager>()
    val authService = get<AuthService>()

    routing {
        get("/") {
            call.respondText(
                "Chat API is running"
            )
        }

        get("/health") {
            call.respond("OK")
        }

        authRoutes(authService)

        userRoutes(userRepository)

        conversationRoutes(
            conversationService
        )

        messageRoutes(
            messageService
        )

        messageWebSocketRoutes(
            messageService,
            conversationService,
            webSocketManager
        )
    }
}
