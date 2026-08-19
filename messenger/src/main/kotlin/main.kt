import common.DatabaseFactory
import common.configureRouting
import common.configureSecurity
import common.configureSerialization
import common.websockets.configureWebsockets
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.initialize()
    configureSerialization()
    configureSecurity()
    configureWebsockets()
    configureRouting()
}
