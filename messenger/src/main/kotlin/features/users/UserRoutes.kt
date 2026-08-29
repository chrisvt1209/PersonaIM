package features.users

import common.BadRequestException
import common.NotFoundException
import common.UnauthorizedException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

import io.ktor.server.auth.jwt.*

fun Route.userRoutes(
    repository: UserRepository,
    service: UserService
) {
    authenticate("auth-jwt") {
        get("/users/me") {
            val userId = call.userId()

            val user = repository.findById(userId)
                ?: throw NotFoundException("User not found")

            call.respond(user)
        }

        put("/users/me") {
            val userId = call.userId()
            val request = call.receive<UpdateProfileRequest>()

            val user = service.updateProfile(
                userId,
                request.username,
                request.email,
                request.avatar
            )
            call.respond(user)
        }

        put("/users/me/password") {
            val userId = call.userId()
            val request = call.receive<ChangePasswordRequest>()

            service.changePassword(userId, request.currentPassword, request.newPassword)
            call.respond(HttpStatusCode.OK)
        }

        get("/users/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: throw BadRequestException("Invalid user id")

            val user = repository.findById(id)
                ?: throw NotFoundException("User not found")

            call.respond(user)
        }
    }
}

private fun ApplicationCall.userId(): Long =
    principal<JWTPrincipal>()?.payload?.subject?.toLongOrNull()
        ?: throw UnauthorizedException()
