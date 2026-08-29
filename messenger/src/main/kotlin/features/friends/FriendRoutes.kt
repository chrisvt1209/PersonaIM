package features.friends

import common.UnauthorizedException
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
                val userId = call.userId()
                val friends = friendService.getFriends(userId)
                call.respond(friends)
            }

            post {
                val userId = call.userId()
                val request = call.receive<AddFriendRequest>()
                val response = friendService.addFriend(userId, request.friendUid)
                call.respond(HttpStatusCode.Created, response)
            }
        }
    }
}

private fun ApplicationCall.userId(): Long =
    principal<JWTPrincipal>()?.payload?.subject?.toLongOrNull()
        ?: throw UnauthorizedException()
