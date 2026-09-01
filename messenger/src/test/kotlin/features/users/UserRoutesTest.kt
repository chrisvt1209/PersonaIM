package features.users

import common.ErrorResponse
import dev.sirhcvt.features.auth.LoginRequest
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import support.TestDatabase
import support.jsonClient
import support.registerUser
import support.testModule

class UserRoutesTest {

    @BeforeTest
    fun reset() = TestDatabase.reset()

    @Test
    fun `get me requires authentication`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()

        val response = client.get("/users/me")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `get me returns the caller's profile`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()
        val joker = client.registerUser("Joker", "joker@persona.dev")

        val response = client.get("/users/me") { bearerAuth(joker.token) }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(joker.username, response.body<User>().username)
    }

    @Test
    fun `get another user by id`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()
        val joker = client.registerUser("Joker", "joker@persona.dev")
        val skull = client.registerUser("Skull", "skull@persona.dev")

        val response = client.get("/users/${skull.id}") { bearerAuth(joker.token) }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Skull", response.body<User>().username)
    }

    @Test
    fun `update profile rejects an invalid avatar`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()
        val joker = client.registerUser("Joker", "joker@persona.dev")

        val response = client.put("/users/me") {
            bearerAuth(joker.token)
            contentType(ContentType.Application.Json)
            setBody(UpdateProfileRequest("Joker", "joker@persona.dev", "not-a-real-avatar"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `update profile rejects an email already used by someone else`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()
        val joker = client.registerUser("Joker", "joker@persona.dev")
        client.registerUser("Skull", "skull@persona.dev")

        val response = client.put("/users/me") {
            bearerAuth(joker.token)
            contentType(ContentType.Application.Json)
            setBody(UpdateProfileRequest("Joker", "skull@persona.dev", "ann"))
        }

        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `update profile succeeds with valid data`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()
        val joker = client.registerUser("Joker", "joker@persona.dev")

        val response = client.put("/users/me") {
            bearerAuth(joker.token)
            contentType(ContentType.Application.Json)
            setBody(UpdateProfileRequest("Joker Renamed", "joker@persona.dev", "ann"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Joker Renamed", response.body<User>().username)
    }

    @Test
    fun `change password rejects a wrong current password`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()
        val joker = client.registerUser("Joker", "joker@persona.dev", "password123")

        val response = client.put("/users/me/password") {
            bearerAuth(joker.token)
            contentType(ContentType.Application.Json)
            setBody(ChangePasswordRequest("wrong-current-password", "newpassword456"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Current password is incorrect", response.body<ErrorResponse>().error)
    }

    @Test
    fun `change password succeeds and old password stops working`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()
        val joker = client.registerUser("Joker", "joker@persona.dev", "password123")

        val response = client.put("/users/me/password") {
            bearerAuth(joker.token)
            contentType(ContentType.Application.Json)
            setBody(ChangePasswordRequest("password123", "newpassword456"))
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val loginWithOldPassword = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("joker@persona.dev", "password123"))
        }
        assertEquals(HttpStatusCode.Unauthorized, loginWithOldPassword.status)

        val loginWithNewPassword = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("joker@persona.dev", "newpassword456"))
        }
        assertEquals(HttpStatusCode.OK, loginWithNewPassword.status)
    }
}
