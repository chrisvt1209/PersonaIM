package dev.compose.messenger.feature.chat.data.mapper

import dev.compose.messenger.core.common.model.Sender
import dev.compose.messenger.core.database.entity.MessageEntity
import dev.compose.messenger.core.network.api.MessageDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageMapperTest {

    @Test
    fun `toEntity marks message as mine when sender matches current user`() {
        val dto = MessageDto(id = 1, conversationId = 10, senderId = 42, text = "hi", sentAt = "t")

        val entity = dto.toEntity(currentUserId = 42)

        assertTrue(entity.isFromMe)
    }

    @Test
    fun `toEntity marks message as not mine when sender differs`() {
        val dto = MessageDto(id = 1, conversationId = 10, senderId = 42, text = "hi", sentAt = "t")

        val entity = dto.toEntity(currentUserId = 7)

        assertFalse(entity.isFromMe)
    }

    @Test
    fun `toEntity marks message as not mine when current user unknown`() {
        val dto = MessageDto(id = 1, conversationId = 10, senderId = 42, text = "hi", sentAt = "t")

        val entity = dto.toEntity(currentUserId = null)

        assertFalse(entity.isFromMe)
    }

    @Test
    fun `entity toDomain resolves sender by id and carries isFromMe`() {
        val entity = MessageEntity(
            id = 1,
            conversationId = 10,
            senderId = 0,
            senderName = "",
            text = "yo",
            timestamp = "t",
            isFromMe = false
        )

        val domain = entity.toDomain()

        assertEquals(Sender.Ann, domain.sender)
        assertEquals("yo", domain.text)
        assertFalse(domain.isFromMe)
    }

    @Test
    fun `entity toDomain falls back to Ren for unknown sender id`() {
        val entity = MessageEntity(
            id = 1,
            conversationId = 10,
            senderId = 999,
            senderName = "",
            text = "yo",
            timestamp = "t",
            isFromMe = true
        )

        val domain = entity.toDomain()

        assertEquals(Sender.Ren, domain.sender)
    }
}
