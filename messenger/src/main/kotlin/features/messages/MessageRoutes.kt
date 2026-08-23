package features.messages

import io.ktor.http.*
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
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val conversationId =
                    call.parameters["conversationId"]
                        ?.toLongOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest
                        )

                try {
                    val messages =
                        messageService.getMessages(
                            conversationId,
                            userId
                        )

                    call.respond(messages)
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to e.message)
                    )
                }
            }

            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject?.toLongOrNull()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val conversationId =
                    call.parameters["conversationId"]
                        ?.toLongOrNull()
                        ?: return@post call.respond(
                            HttpStatusCode.BadRequest
                        )

                val request = call.receive<SendMessageRequest>()

                try {
                    val message = messageService.send(
                        conversationId,
                        userId,
                        request.text
                    )
                    call.respond(HttpStatusCode.Created, message)
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to e.message)
                    )
                }
            }
        }
    }
}
