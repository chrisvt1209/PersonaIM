package dev.compose.messenger.feature.friends.data.mapper

import dev.compose.messenger.core.database.entity.FriendEntity
import dev.compose.messenger.core.network.api.FriendDto
import org.junit.Assert.assertEquals
import org.junit.Test

class FriendMapperTest {

    @Test
    fun `dto toEntity carries all fields across`() {
        val dto = FriendDto(id = 1, username = "Ann", email = "ann@shujin.jp")

        val entity = dto.toEntity()

        assertEquals(1L, entity.id)
        assertEquals("Ann", entity.username)
        assertEquals("ann@shujin.jp", entity.email)
    }

    @Test
    fun `entity toDomain carries all fields across`() {
        val entity = FriendEntity(id = 1, username = "Ann", email = "ann@shujin.jp")

        val domain = entity.toDomain()

        assertEquals(1L, domain.id)
        assertEquals("Ann", domain.username)
        assertEquals("ann@shujin.jp", domain.email)
    }
}
