package features.users

import io.ktor.http.*
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
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.subject?.toLongOrNull()

            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            val user = repository.findById(userId)
                ?: return@get call.respond(HttpStatusCode.NotFound)

            call.respond(user)
        }

        put("/users/me") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.subject?.toLongOrNull()
                ?: return@put call.respond(HttpStatusCode.Unauthorized)

            val request = call.receive<UpdateProfileRequest>()

            try {
                val user = service.updateProfile(
                    userId,
                    request.username,
                    request.email,
                    request.avatar
                )
                call.respond(user)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        put("/users/me/password") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.subject?.toLongOrNull()
                ?: return@put call.respond(HttpStatusCode.Unauthorized)

            val request = call.receive<ChangePasswordRequest>()

            try {
                service.changePassword(userId, request.currentPassword, request.newPassword)
                call.respond(HttpStatusCode.OK)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        get("/users/{id}") {
            val id =
                call.parameters["id"]
                    ?.toLongOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest
                    )

            val user =
                repository.findById(id)
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound
                    )

            call.respond(user)
        }
    }
}