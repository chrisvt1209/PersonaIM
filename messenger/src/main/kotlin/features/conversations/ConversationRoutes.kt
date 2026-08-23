package features.conversations

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

import io.ktor.server.auth.jwt.*

fun Route.conversationRoutes(
    service: ConversationService
) {
    authenticate("auth-jwt") {
        route("conversations") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val conversations = service.getForUser(userId)
                call.respond(conversations)
            }

            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject?.toLongOrNull()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val request =
                    call.receive<CreateConversationRequest>()

                try {
                    val conversation =
                        service.create(
                            userId,
                            request.userId
                        )
                    call.respond(
                        HttpStatusCode.Created,
                        conversation
                    )
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to e.message)
                    )
                }
            }

            get("/{id}") {
                val id =
                    call.parameters["id"]
                        ?.toLongOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest
                        )

                val conversation =
                    service.get(id)
                        ?: return@get call.respond(
                            HttpStatusCode.NotFound
                        )
                
                call.respond(conversation)
            }
        }
    }
}