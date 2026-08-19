package features.users

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(
    repository: UserRepository
) {
    authenticate("auth-jwt") {
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