package features.auth

import common.ErrorResponse
import dev.sirhcvt.features.auth.AuthResponse
import dev.sirhcvt.features.auth.LoginRequest
import dev.sirhcvt.features.auth.RegisterRequest
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import support.TestDatabase
import support.jsonClient
import support.testModule

class AuthRoutesTest {

    @BeforeTest
    fun reset() = TestDatabase.reset()

    @Test
    fun `register returns a token`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("Joker", "joker@persona.dev", "password123"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(response.body<AuthResponse>().token.isNotBlank())
    }

    @Test
    fun `register with an already-used email is rejected`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("Joker", "joker@persona.dev", "password123"))
        }

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("Someone Else", "joker@persona.dev", "otherpassword"))
        }

        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `login with correct credentials returns a token`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("Joker", "joker@persona.dev", "password123"))
        }

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("joker@persona.dev", "password123"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.body<AuthResponse>().token.isNotBlank())
    }

    @Test
    fun `login with wrong password is unauthorized`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()

        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("Joker", "joker@persona.dev", "password123"))
        }

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("joker@persona.dev", "wrong-password"))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `login with unknown email is unauthorized`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("nobody@persona.dev", "password123"))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("Invalid credentials", response.body<ErrorResponse>().error)
    }
}
