package dev.compose.messenger.core.network

import dev.compose.messenger.core.datastore.PreferencesManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

val AuthPlugin = createClientPlugin("AuthPlugin", ::AuthPluginConfig) {
    val preferencesManager = pluginConfig.preferencesManager
    
    onRequest { request, _ ->
        val token = runBlocking { preferencesManager.authToken.first() }
        if (token != null) {
            request.header("Authorization", "Bearer $token")
        }
    }
}

class AuthPluginConfig {
    lateinit var preferencesManager: PreferencesManager
}

fun createHttpClient(preferencesManager: PreferencesManager): HttpClient {
    return HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }

        install(Logging) {
            level = LogLevel.INFO
        }

        install(WebSockets)

        install(AuthPlugin) {
            this.preferencesManager = preferencesManager
        }

        defaultRequest {
            url {
                protocol = URLProtocol.HTTP
                host = "10.0.2.2" // Localhost from Android emulator
                port = 8080
            }
            contentType(ContentType.Application.Json)
        }
    }
}
