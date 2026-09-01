package features.messages

import features.conversations.Conversation
import features.conversations.CreateConversationRequest
import io.ktor.client.HttpClient
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
import support.TestDatabase
import support.TestUser
import support.jsonClient
import support.registerUser
import support.testModule

private suspend fun HttpClient.createConversation(caller: TestUser, otherUserId: Long): Conversation =
    post("/conversations") {
        bearerAuth(caller.token)
        contentType(ContentType.Application.Json)
        setBody(CreateConversationRequest(otherUserId))
    }.body()

class MessageRoutesTest {

    @BeforeTest
    fun reset() = TestDatabase.reset()

    @Test
    fun `participant can send and read messages`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()
        val joker = client.registerUser("Joker", "joker@persona.dev")
        val skull = client.registerUser("Skull", "skull@persona.dev")
        val conversation = client.createConversation(joker, skull.id)

        val sendResponse = client.post("/conversations/${conversation.id}/messages") {
            bearerAuth(joker.token)
            contentType(ContentType.Application.Json)
            setBody(SendMessageRequest("For the people, for the truth!"))
        }
        assertEquals(HttpStatusCode.Created, sendResponse.status)

        val messages = client.get("/conversations/${conversation.id}/messages") {
            bearerAuth(skull.token)
        }.body<List<Message>>()

        assertEquals(1, messages.size)
        assertEquals("For the people, for the truth!", messages.first().text)
    }

    @Test
    fun `sending a blank message is rejected`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()
        val joker = client.registerUser("Joker", "joker@persona.dev")
        val skull = client.registerUser("Skull", "skull@persona.dev")
        val conversation = client.createConversation(joker, skull.id)

        val response = client.post("/conversations/${conversation.id}/messages") {
            bearerAuth(joker.token)
            contentType(ContentType.Application.Json)
            setBody(SendMessageRequest("   "))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `non-participant cannot send or read messages`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()
        val joker = client.registerUser("Joker", "joker@persona.dev")
        val skull = client.registerUser("Skull", "skull@persona.dev")
        val outsider = client.registerUser("Outsider", "outsider@persona.dev")
        val conversation = client.createConversation(joker, skull.id)

        val sendResponse = client.post("/conversations/${conversation.id}/messages") {
            bearerAuth(outsider.token)
            contentType(ContentType.Application.Json)
            setBody(SendMessageRequest("I shouldn't be able to say this"))
        }
        assertEquals(HttpStatusCode.Forbidden, sendResponse.status)

        val readResponse = client.get("/conversations/${conversation.id}/messages") {
            bearerAuth(outsider.token)
        }
        assertEquals(HttpStatusCode.Forbidden, readResponse.status)
    }
}
