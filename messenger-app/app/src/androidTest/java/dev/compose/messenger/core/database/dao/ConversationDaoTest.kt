package dev.compose.messenger.core.database.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.compose.messenger.core.database.MessengerDatabase
import dev.compose.messenger.core.database.entity.ConversationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ConversationDaoTest {

    private lateinit var database: MessengerDatabase
    private lateinit var dao: ConversationDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, MessengerDatabase::class.java).build()
        dao = database.conversationDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDatabase() {
        database.close()
    }

    private fun conversation(id: Long, timestamp: String?) = ConversationEntity(
        id = id,
        title = "Conversation $id",
        isGroup = false,
        lastMessage = "hi",
        lastMessageTimestamp = timestamp,
        unreadCount = 0,
        participantIds = ""
    )

    @Test
    fun getAllConversations_returnsRowsOrderedByLastMessageTimestampDescending() = runTest {
        dao.insertConversations(
            listOf(
                conversation(id = 1, timestamp = "2026-01-01T00:00:00"),
                conversation(id = 2, timestamp = "2026-03-01T00:00:00")
            )
        )

        val conversations = dao.getAllConversations().first()

        assertEquals(listOf(2L, 1L), conversations.map { it.id })
    }

    @Test
    fun deleteConversation_removesOnlyThatRow() = runTest {
        dao.insertConversations(
            listOf(
                conversation(id = 1, timestamp = "2026-01-01T00:00:00"),
                conversation(id = 2, timestamp = "2026-01-02T00:00:00")
            )
        )

        dao.deleteConversation(1)

        val remaining = dao.getAllConversations().first()
        assertEquals(listOf(2L), remaining.map { it.id })
    }

    @Test
    fun clearAll_removesEveryRow() = runTest {
        dao.insertConversations(listOf(conversation(id = 1, timestamp = "2026-01-01T00:00:00")))

        dao.clearAll()

        assertTrue(dao.getAllConversations().first().isEmpty())
    }
}
