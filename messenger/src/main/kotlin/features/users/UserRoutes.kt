package features.users

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

import io.ktor.server.auth.jwt.*

fun Route.userRoutes(
    repository: UserRepository
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