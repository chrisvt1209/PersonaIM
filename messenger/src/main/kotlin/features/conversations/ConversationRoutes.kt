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

            get("/invites") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val invites = service.getInvitesForUser(userId)
                call.respond(invites)
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

            post("/groups") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject?.toLongOrNull()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val request = call.receive<CreateGroupRequest>()

                try {
                    val conversation = service.createGroup(
                        userId,
                        request.title,
                        request.memberUserIds
                    )
                    call.respond(HttpStatusCode.Created, conversation)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
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

            post("/{id}/invite") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject?.toLongOrNull()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest)

                val request = call.receive<InviteRequest>()

                try {
                    val conversation = service.invite(id, userId, request.userId)
                    call.respond(conversation)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            post("/{id}/accept") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject?.toLongOrNull()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest)

                try {
                    service.acceptInvite(id, userId)
                    call.respond(HttpStatusCode.OK)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            post("/{id}/decline") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject?.toLongOrNull()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest)

                try {
                    service.declineInvite(id, userId)
                    call.respond(HttpStatusCode.OK)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject?.toLongOrNull()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized)

                val id =
                    call.parameters["id"]
                        ?.toLongOrNull()
                        ?: return@delete call.respond(
                            HttpStatusCode.BadRequest
                        )

                try {
                    service.delete(id, userId)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
