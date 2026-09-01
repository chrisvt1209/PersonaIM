package support

import common.configureDependencyInjection
import common.configureRouting
import common.configureSecurity
import common.configureSerialization
import common.configureStatusPages
import common.websockets.configureWebsockets
import io.ktor.server.application.Application
import org.ktorm.database.Database

/** Mirrors [Application.module] in main.kt but wires the given (test) [Database] instead of DatabaseFactory. */
fun Application.testModule(database: Database) {
    configureDependencyInjection(database)
    configureSerialization()
    configureStatusPages()
    configureSecurity()
    configureWebsockets()
    configureRouting()
}
