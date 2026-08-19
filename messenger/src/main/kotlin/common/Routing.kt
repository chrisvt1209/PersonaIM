package common

import common.websockets.WebSocketManager
import common.websockets.messageWebSocketRoutes
import features.auth.AuthService
import features.auth.authRoutes
import features.conversations.ConversationRepository
import features.conversations.ConversationService
import features.conversations.conversationRoutes
import features.messages.MessageRepository
import features.messages.MessageService
import features.messages.messageRoutes
import features.users.UserRepository
import features.users.userRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    val database =
        DatabaseFactory.database

    val userRepository =
        UserRepository(database)

    val conversationRepository =
        ConversationRepository(database)

    val conversationService =
        ConversationService(conversationRepository)

    val messageRepository =
        MessageRepository(database)

    val messageService =
        MessageService(
            messageRepository,
            conversationService
        )

    val webSocketManager =
        WebSocketManager()

    val jwtSecret =
        System.getenv("JWT_SECRET")
            ?: "6c80f22b9f52ea31378eeeaf3bd558cd672693adef4dfb37d4eb91660ed3ae46"

    val authService =
        AuthService(
            userRepository,
            jwtSecret
        )

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