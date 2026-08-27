package features.friends

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.friendRoutes(
    friendService: FriendService
) {
    authenticate("auth-jwt") {
        route("/friends") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject?.toLongOrNull()
                
                if (userId != null) {
                    val friends = friendService.getFriends(userId)
                    call.respond(friends)
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }

            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject?.toLongOrNull()
                
                if (userId != null) {
                    try {
                        val request = call.receive<AddFriendRequest>()
                        val response = friendService.addFriend(userId, request.friendUid)
                        call.respond(HttpStatusCode.Created, response)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }
        }
    }
}
