package dev.sirhcvt

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.*
import support.TestDatabase
import support.testModule

class ServerTest {

    @BeforeTest
    fun reset() = TestDatabase.reset()

    @Test
    fun `test root endpoint`() = testApplication {
        application { testModule(TestDatabase.database) }
        // verify server root returns 200
        assertEquals(HttpStatusCode.OK, client.get("/").status)
    }

}
