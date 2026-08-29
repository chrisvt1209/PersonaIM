package features.messages

import common.BadRequestException
import common.UnauthorizedException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.messageRoutes(
    messageService: MessageService
) {
    authenticate("auth-jwt") {
        route("/conversations/{conversationId}/messages") {
            get {
                val userId = call.userId()
                val conversationId = call.conversationId()

                val messages = messageService.getMessages(conversationId, userId)
                call.respond(messages)
            }

            post {
                val userId = call.userId()
                val conversationId = call.conversationId()
                val request = call.receive<SendMessageRequest>()

                val message = messageService.send(conversationId, userId, request.text)
                call.respond(HttpStatusCode.Created, message)
            }
        }
    }
}

private fun ApplicationCall.userId(): Long =
    principal<JWTPrincipal>()?.payload?.subject?.toLongOrNull()
        ?: throw UnauthorizedException()

private fun ApplicationCall.conversationId(): Long =
    parameters["conversationId"]?.toLongOrNull()
        ?: throw BadRequestException("Invalid conversation id")
