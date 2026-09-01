package features.friends

import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
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
import support.registerUser
import support.testModule

class FriendRoutesTest {

    @BeforeTest
    fun reset() = TestDatabase.reset()

    @Test
    fun `add friend by uid succeeds both ways`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()
        val joker = client.registerUser("Joker", "joker@persona.dev")
        val skull = client.registerUser("Skull", "skull@persona.dev")

        val response = client.post("/friends") {
            bearerAuth(joker.token)
            contentType(ContentType.Application.Json)
            setBody(AddFriendRequest(skull.uid))
        }
        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("Skull", response.body<FriendResponse>().username)

        val jokersFriends = client.get("/friends") { bearerAuth(joker.token) }.body<List<FriendResponse>>()
        assertTrue(jokersFriends.any { it.username == "Skull" })

        val skullsFriends = client.get("/friends") { bearerAuth(skull.token) }.body<List<FriendResponse>>()
        assertTrue(skullsFriends.any { it.username == "Joker" })
    }

    @Test
    fun `adding yourself as a friend is rejected`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()
        val joker = client.registerUser("Joker", "joker@persona.dev")

        val response = client.post("/friends") {
            bearerAuth(joker.token)
            contentType(ContentType.Application.Json)
            setBody(AddFriendRequest(joker.uid))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `adding an unknown uid is not found`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()
        val joker = client.registerUser("Joker", "joker@persona.dev")

        val response = client.post("/friends") {
            bearerAuth(joker.token)
            contentType(ContentType.Application.Json)
            setBody(AddFriendRequest("NOTREAL1"))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `adding an existing friend again is a conflict`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()
        val joker = client.registerUser("Joker", "joker@persona.dev")
        val skull = client.registerUser("Skull", "skull@persona.dev")

        client.post("/friends") {
            bearerAuth(joker.token)
            contentType(ContentType.Application.Json)
            setBody(AddFriendRequest(skull.uid))
        }

        val response = client.post("/friends") {
            bearerAuth(joker.token)
            contentType(ContentType.Application.Json)
            setBody(AddFriendRequest(skull.uid))
        }

        assertEquals(HttpStatusCode.Conflict, response.status)
    }
}
