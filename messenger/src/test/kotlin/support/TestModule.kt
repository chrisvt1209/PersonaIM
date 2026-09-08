package support

import common.configureDependencyInjection
import common.configureRouting
import common.configureSecurity
import common.configureSerialization
import common.configureStatusPages
import common.websockets.configureWebsockets
import io.ktor.server.application.Application
import org.ktorm.database.Database
import java.nio.file.Files

/**
 * Mirrors [Application.module] in main.kt but wires the given (test) [Database] instead of
 * DatabaseFactory, and a fresh temp directory for avatar storage per call - user ids are reused
 * across tests (TestDatabase.reset() restarts the identity sequence), so reusing one directory
 * would let one test's avatar file leak into another test's freshly-recycled user id.
 */
fun Application.testModule(database: Database) {
    configureDependencyInjection(database, Files.createTempDirectory("persona-messenger-test-avatars"))
    configureSerialization()
    configureStatusPages()
    configureSecurity()
    configureWebsockets()
    configureRouting()
}
