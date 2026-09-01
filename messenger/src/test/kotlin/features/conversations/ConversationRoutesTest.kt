package features.conversations

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
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
import kotlin.test.assertTrue
import support.TestDatabase
import support.TestUser
import support.jsonClient
import support.registerUser
import support.testModule

private suspend fun HttpClient.createGroup(creator: TestUser, title: String, memberIds: List<Long>): Conversation =
    post("/conversations/groups") {
        bearerAuth(creator.token)
        contentType(ContentType.Application.Json)
        setBody(CreateGroupRequest(title, memberIds))
    }.body()

class ConversationRoutesTest {

    @BeforeTest
    fun reset() = TestDatabase.reset()

    @Test
    fun `creating a 1-to-1 conversation returns a SINGLE type with both participants`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()
        val joker = client.registerUser("Joker", "joker@persona.dev")
        val skull = client.registerUser("Skull", "skull@persona.dev")

        val response = client.post("/conversations") {
            bearerAuth(joker.token)
            contentType(ContentType.Application.Json)
            setBody(CreateConversationRequest(skull.id))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val conversation = response.body<Conversation>()
        assertEquals(ConversationType.SINGLE, conversation.type)
        assertEquals(setOf(joker.id, skull.id), conversation.participants.map { it.userId }.toSet())
    }

    @Test
    fun `creating a group makes the creator a maintainer and invites members as pending`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()
        val joker = client.registerUser("Joker", "joker@persona.dev")
        val skull = client.registerUser("Skull", "skull@persona.dev")

        val group = client.createGroup(joker, "Phantom Thieves", listOf(skull.id))

        assertEquals(ConversationType.GROUP, group.type)
        val creator = group.participants.first { it.userId == joker.id }
        assertEquals(ParticipantRole.MAINTAINER, creator.role)
        assertEquals(ParticipantStatus.ACCEPTED, creator.status)
        val invitee = group.participants.first { it.userId == skull.id }
        assertEquals(ParticipantRole.MEMBER, invitee.role)
        assertEquals(ParticipantStatus.PENDING, invitee.status)
    }

    @Test
    fun `a plain member cannot invite, but an inviter and a maintainer can`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()
        val joker = client.registerUser("Joker", "joker@persona.dev")
        val skull = client.registerUser("Skull", "skull@persona.dev")
        val fox = client.registerUser("Fox", "fox@persona.dev")
        val panther = client.registerUser("Panther", "panther@persona.dev")
        val outsider = client.registerUser("Outsider", "outsider@persona.dev")

        val group = client.createGroup(joker, "Phantom Thieves", listOf(skull.id, fox.id, panther.id))
        client.post("/conversations/${group.id}/accept") { bearerAuth(skull.token) }
        client.post("/conversations/${group.id}/accept") { bearerAuth(fox.token) }
        client.post("/conversations/${group.id}/accept") { bearerAuth(panther.token) }

        // Skull is a plain MEMBER by default and should be forbidden from inviting.
        val forbidden = client.post("/conversations/${group.id}/invite") {
            bearerAuth(skull.token)
            contentType(ContentType.Application.Json)
            setBody(InviteRequest(outsider.id))
        }
        assertEquals(HttpStatusCode.Forbidden, forbidden.status)

        // Promote Fox to INVITER; they should now be able to invite.
        client.put("/conversations/${group.id}/members/${fox.id}/role") {
            bearerAuth(joker.token)
            contentType(ContentType.Application.Json)
            setBody(ChangeRoleRequest(ParticipantRole.INVITER))
        }
        val invitedByInviter = client.post("/conversations/${group.id}/invite") {
            bearerAuth(fox.token)
            contentType(ContentType.Application.Json)
            setBody(InviteRequest(outsider.id))
        }
        assertEquals(HttpStatusCode.OK, invitedByInviter.status)
        val afterInvite = invitedByInviter.body<Conversation>()
        assertTrue(afterInvite.participants.any { it.userId == outsider.id })
    }

    @Test
    fun `maintainer can remove a plain member but not another maintainer`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()
        val joker = client.registerUser("Joker", "joker@persona.dev")
        val skull = client.registerUser("Skull", "skull@persona.dev")
        val fox = client.registerUser("Fox", "fox@persona.dev")

        val group = client.createGroup(joker, "Phantom Thieves", listOf(skull.id, fox.id))
        client.post("/conversations/${group.id}/accept") { bearerAuth(skull.token) }
        client.post("/conversations/${group.id}/accept") { bearerAuth(fox.token) }

        client.put("/conversations/${group.id}/members/${fox.id}/role") {
            bearerAuth(joker.token)
            contentType(ContentType.Application.Json)
            setBody(ChangeRoleRequest(ParticipantRole.MAINTAINER))
        }

        val removeMaintainer = client.delete("/conversations/${group.id}/members/${fox.id}") {
            bearerAuth(joker.token)
        }
        assertEquals(HttpStatusCode.Forbidden, removeMaintainer.status)

        val removeMember = client.delete("/conversations/${group.id}/members/${skull.id}") {
            bearerAuth(joker.token)
        }
        assertEquals(HttpStatusCode.OK, removeMember.status)
        assertTrue(removeMember.body<Conversation>().participants.none { it.userId == skull.id })
    }

    @Test
    fun `leaving a group removes you regardless of role`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()
        val joker = client.registerUser("Joker", "joker@persona.dev")
        val skull = client.registerUser("Skull", "skull@persona.dev")

        val group = client.createGroup(joker, "Phantom Thieves", listOf(skull.id))
        client.post("/conversations/${group.id}/accept") { bearerAuth(skull.token) }

        val leaveResponse = client.post("/conversations/${group.id}/leave") { bearerAuth(skull.token) }
        assertEquals(HttpStatusCode.OK, leaveResponse.status)

        val afterLeave = client.get("/conversations/${group.id}") { bearerAuth(joker.token) }.body<Conversation>()
        assertTrue(afterLeave.participants.none { it.userId == skull.id })
    }

    @Test
    fun `only a maintainer can delete a group, but either side can delete a 1-to-1`() = testApplication {
        application { testModule(TestDatabase.database) }
        val client = jsonClient()
        val joker = client.registerUser("Joker", "joker@persona.dev")
        val skull = client.registerUser("Skull", "skull@persona.dev")

        val group = client.createGroup(joker, "Phantom Thieves", listOf(skull.id))
        client.post("/conversations/${group.id}/accept") { bearerAuth(skull.token) }

        val memberDelete = client.delete("/conversations/${group.id}") { bearerAuth(skull.token) }
        assertEquals(HttpStatusCode.Forbidden, memberDelete.status)

        val maintainerDelete = client.delete("/conversations/${group.id}") { bearerAuth(joker.token) }
        assertEquals(HttpStatusCode.NoContent, maintainerDelete.status)

        val direct = client.post("/conversations") {
            bearerAuth(joker.token)
            contentType(ContentType.Application.Json)
            setBody(CreateConversationRequest(skull.id))
        }.body<Conversation>()

        val singleDelete = client.delete("/conversations/${direct.id}") { bearerAuth(skull.token) }
        assertEquals(HttpStatusCode.NoContent, singleDelete.status)
    }
}
