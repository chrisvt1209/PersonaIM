package dev.compose.messenger.core.database.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.compose.messenger.core.database.MessengerDatabase
import dev.compose.messenger.core.database.entity.MessageEntity
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
class MessageDaoTest {

    private lateinit var database: MessengerDatabase
    private lateinit var dao: MessageDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, MessengerDatabase::class.java).build()
        dao = database.messageDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDatabase() {
        database.close()
    }

    private fun message(id: Long, conversationId: Long, timestamp: String) = MessageEntity(
        id = id,
        conversationId = conversationId,
        senderId = 1,
        senderName = "",
        text = "message $id",
        timestamp = timestamp,
        isFromMe = false
    )

    @Test
    fun getMessagesForConversation_returnsOnlyThatConversationOrderedByTimestampAscending() = runTest {
        dao.insertMessages(
            listOf(
                message(id = 1, conversationId = 10, timestamp = "2026-01-01T00:02:00"),
                message(id = 2, conversationId = 10, timestamp = "2026-01-01T00:01:00"),
                message(id = 3, conversationId = 20, timestamp = "2026-01-01T00:00:00")
            )
        )

        val messages = dao.getMessagesForConversation(10).first()

        assertEquals(listOf(2L, 1L), messages.map { it.id })
    }

    @Test
    fun clearMessagesForConversation_removesOnlyThatConversationsMessages() = runTest {
        dao.insertMessages(
            listOf(
                message(id = 1, conversationId = 10, timestamp = "2026-01-01T00:00:00"),
                message(id = 2, conversationId = 20, timestamp = "2026-01-01T00:00:00")
            )
        )

        dao.clearMessagesForConversation(10)

        assertTrue(dao.getMessagesForConversation(10).first().isEmpty())
        assertEquals(1, dao.getMessagesForConversation(20).first().size)
    }
}
