package features.messages

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.messageRoutes(
    messageService: MessageService
) {
    authenticate("auth-jwt") {
        route("/conversations/{conversationId}/messages") {
            get {
                val userId =
                    call.principal<UserIdPrincipal>()
                        ?.name
                        ?.toLong()
                        ?: return@get call.respond(
                            HttpStatusCode.Unauthorized
                        )

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
        }
    }
}