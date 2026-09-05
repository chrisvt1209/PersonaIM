package dev.compose.messenger.feature.conversations.data.mapper

import dev.compose.messenger.core.database.entity.ConversationEntity
import dev.compose.messenger.feature.conversations.data.ConversationDto
import dev.compose.messenger.feature.conversations.data.ParticipantDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversationMapperTest {

    private val currentUserId = 1L
    private val other = ParticipantDto(userId = 2L, username = "Ann", status = "ONLINE", role = "MEMBER")
    private val self = ParticipantDto(userId = currentUserId, username = "Ren", status = "ONLINE", role = "MEMBER")

    @Test
    fun `toEntity uses explicit title when set`() {
        val dto = ConversationDto(id = 1, title = "Phantom Thieves", type = "GROUP", participants = listOf(self, other))

        val entity = dto.toEntity(currentUserId)

        assertEquals("Phantom Thieves", entity.title)
        assertTrue(entity.isGroup)
    }

    @Test
    fun `toEntity falls back to other participant username when title null`() {
        val dto = ConversationDto(id = 1, title = null, type = "SINGLE", participants = listOf(self, other))

        val entity = dto.toEntity(currentUserId)

        assertEquals("Ann", entity.title)
        assertFalse(entity.isGroup)
    }

    @Test
    fun `toEntity falls back to Conversation when no other participant`() {
        val dto = ConversationDto(id = 1, title = null, participants = listOf(self))

        val entity = dto.toEntity(currentUserId)

        assertEquals("Conversation", entity.title)
    }

    @Test
    fun `toEntity excludes current user from participantIds`() {
        val dto = ConversationDto(id = 1, participants = listOf(self, other))

        val entity = dto.toEntity(currentUserId)

        assertEquals("2", entity.participantIds)
    }

    @Test
    fun `toDetail maps all participants including self`() {
        val dto = ConversationDto(id = 1, type = "GROUP", participants = listOf(self, other))

        val detail = dto.toDetail(currentUserId)

        assertEquals("1", detail.id)
        assertEquals(2, detail.participants.size)
    }

    @Test
    fun `toInvite defaults title to Group and counts members`() {
        val dto = ConversationDto(id = 5, title = null, participants = listOf(self, other))

        val invite = dto.toInvite()

        assertEquals("5", invite.id)
        assertEquals("Group", invite.title)
        assertEquals(2, invite.memberCount)
    }

    @Test
    fun `entity toDomain splits participantIds back into longs`() {
        val entity = ConversationEntity(
            id = 1,
            title = "Ann",
            isGroup = false,
            lastMessage = null,
            lastMessageTimestamp = null,
            unreadCount = 3,
            participantIds = "2,3"
        )

        val domain = entity.toDomain()

        assertEquals(listOf(2L, 3L), domain.participantIds)
        assertEquals("", domain.lastMessage)
        assertEquals(3, domain.unreadCount)
    }

    @Test
    fun `entity toDomain with empty participantIds yields empty list not crash`() {
        val entity = ConversationEntity(
            id = 1,
            title = "Solo",
            isGroup = false,
            lastMessage = "hi",
            lastMessageTimestamp = null,
            unreadCount = 0,
            participantIds = ""
        )

        val domain = entity.toDomain()

        assertTrue(domain.participantIds.isEmpty())
        assertEquals("hi", domain.lastMessage)
    }
}
