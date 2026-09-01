package support

import dev.sirhcvt.features.auth.AuthResponse
import dev.sirhcvt.features.auth.RegisterRequest
import features.users.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

data class TestUser(
    val id: Long,
    val uid: String,
    val username: String,
    val token: String
)

/** Registers a brand new user and returns everything tests typically need about them. */
suspend fun HttpClient.registerUser(
    username: String,
    email: String,
    password: String = "password123"
): TestUser {
    val auth = post("/auth/register") {
        contentType(ContentType.Application.Json)
        setBody(RegisterRequest(username, email, password))
    }.body<AuthResponse>()

    val me = get("/users/me") { bearerAuth(auth.token) }.body<User>()

    return TestUser(id = me.id, uid = me.uid, username = me.username, token = auth.token)
}
