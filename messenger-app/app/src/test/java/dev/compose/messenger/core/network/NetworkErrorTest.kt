package dev.compose.messenger.core.network

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class NetworkErrorTest {

    @Test
    fun `IOException maps to connection message`() = runTest {
        val message = IOException("connect timed out").toUserMessage()

        assertEquals("Can't reach server. Check your connection.", message)
    }

    @Test
    fun `unexpected exception maps to generic message`() = runTest {
        val message = IllegalStateException("boom").toUserMessage()

        assertEquals("Something went wrong. Please try again.", message)
    }
}
