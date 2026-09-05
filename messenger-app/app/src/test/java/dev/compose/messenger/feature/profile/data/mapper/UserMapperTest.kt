package dev.compose.messenger.feature.profile.data.mapper

import dev.compose.messenger.core.database.entity.UserEntity
import dev.compose.messenger.core.network.api.UserDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserMapperTest {

    @Test
    fun `dto toEntity carries all fields across including null bio`() {
        val dto = UserDto(id = 1, username = "Joker", email = "joker@leblanc.jp", uid = "uid-1", avatar = "cat.png", bio = null)

        val entity = dto.toEntity()

        assertEquals(1L, entity.id)
        assertEquals("Joker", entity.username)
        assertEquals("uid-1", entity.uid)
        assertNull(entity.bio)
    }

    @Test
    fun `entity toDomain carries all fields across`() {
        val entity = UserEntity(id = 1, username = "Joker", email = "joker@leblanc.jp", uid = "uid-1", avatar = "cat.png", bio = "leader")

        val domain = entity.toDomain()

        assertEquals("Joker", domain.username)
        assertEquals("cat.png", domain.avatar)
        assertEquals("leader", domain.bio)
    }
}
