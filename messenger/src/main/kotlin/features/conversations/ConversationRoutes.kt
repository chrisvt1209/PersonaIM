package features.conversations

import common.BadRequestException
import common.NotFoundException
import common.UnauthorizedException
import io.ktor.http.*
import io.ktor.server.application.ApplicationCall
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
                val userId = call.userId()
                val conversations = service.getForUser(userId)
                call.respond(conversations)
            }

            get("/invites") {
                val userId = call.userId()
                val invites = service.getInvitesForUser(userId)
                call.respond(invites)
            }

            post {
                val userId = call.userId()
                val request = call.receive<CreateConversationRequest>()

                val conversation = service.create(userId, request.userId)
                call.respond(HttpStatusCode.Created, conversation)
            }

            post("/groups") {
                val userId = call.userId()
                val request = call.receive<CreateGroupRequest>()

                val conversation = service.createGroup(
                    userId,
                    request.title,
                    request.memberUserIds
                )
                call.respond(HttpStatusCode.Created, conversation)
            }

            get("/{id}") {
                val id = call.conversationId()

                val conversation = service.get(id)
                    ?: throw NotFoundException("Conversation not found")

                call.respond(conversation)
            }

            post("/{id}/invite") {
                val userId = call.userId()
                val id = call.conversationId()
                val request = call.receive<InviteRequest>()

                val conversation = service.invite(id, userId, request.userId)
                call.respond(conversation)
            }

            delete("/{id}/members/{userId}") {
                val callerId = call.userId()
                val id = call.conversationId()
                val targetUserId = call.targetUserId()

                val conversation = service.removeMember(id, callerId, targetUserId)
                call.respond(conversation)
            }

            put("/{id}/members/{userId}/role") {
                val callerId = call.userId()
                val id = call.conversationId()
                val targetUserId = call.targetUserId()
                val request = call.receive<ChangeRoleRequest>()

                val conversation = service.changeRole(id, callerId, targetUserId, request.role)
                call.respond(conversation)
            }

            post("/{id}/leave") {
                val userId = call.userId()
                val id = call.conversationId()

                service.leave(id, userId)
                call.respond(HttpStatusCode.OK)
            }

            post("/{id}/accept") {
                val userId = call.userId()
                val id = call.conversationId()

                service.acceptInvite(id, userId)
                call.respond(HttpStatusCode.OK)
            }

            post("/{id}/decline") {
                val userId = call.userId()
                val id = call.conversationId()

                service.declineInvite(id, userId)
                call.respond(HttpStatusCode.OK)
            }

            delete("/{id}") {
                val userId = call.userId()
                val id = call.conversationId()

                service.delete(id, userId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun ApplicationCall.userId(): Long =
    principal<JWTPrincipal>()?.payload?.subject?.toLongOrNull()
        ?: throw UnauthorizedException()

private fun ApplicationCall.conversationId(): Long =
    parameters["id"]?.toLongOrNull()
        ?: throw BadRequestException("Invalid conversation id")

private fun ApplicationCall.targetUserId(): Long =
    parameters["userId"]?.toLongOrNull()
        ?: throw BadRequestException("Invalid user id")
