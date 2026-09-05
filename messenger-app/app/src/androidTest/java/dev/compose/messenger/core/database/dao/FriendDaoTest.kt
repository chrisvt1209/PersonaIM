package dev.compose.messenger.core.database.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.compose.messenger.core.database.MessengerDatabase
import dev.compose.messenger.core.database.entity.FriendEntity
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
class FriendDaoTest {

    private lateinit var database: MessengerDatabase
    private lateinit var dao: FriendDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, MessengerDatabase::class.java).build()
        dao = database.friendDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun getAllFriends_returnsRowsOrderedByUsername() = runTest {
        dao.insertFriends(
            listOf(
                FriendEntity(id = 1, username = "Ryuji", email = "ryuji@shujin.jp"),
                FriendEntity(id = 2, username = "Ann", email = "ann@shujin.jp")
            )
        )

        val friends = dao.getAllFriends().first()

        assertEquals(listOf("Ann", "Ryuji"), friends.map { it.username })
    }

    @Test
    fun insertFriend_replacesExistingRowWithSameId() = runTest {
        dao.insertFriend(FriendEntity(id = 1, username = "Ann", email = "old@shujin.jp"))
        dao.insertFriend(FriendEntity(id = 1, username = "Ann", email = "new@shujin.jp"))

        val friends = dao.getAllFriends().first()

        assertEquals(1, friends.size)
        assertEquals("new@shujin.jp", friends.first().email)
    }

    @Test
    fun clearAll_removesEveryRow() = runTest {
        dao.insertFriends(
            listOf(
                FriendEntity(id = 1, username = "Ann", email = "ann@shujin.jp"),
                FriendEntity(id = 2, username = "Ryuji", email = "ryuji@shujin.jp")
            )
        )

        dao.clearAll()

        assertTrue(dao.getAllFriends().first().isEmpty())
    }
}
