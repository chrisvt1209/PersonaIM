package dev.compose.messenger.core.network

import dev.compose.messenger.core.datastore.PreferencesManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

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
            level = LogLevel.BODY
        }

        install(WebSockets)

        defaultRequest {
            url {
                protocol = URLProtocol.HTTP
                host = "10.0.2.2" // Localhost from Android emulator
                port = 8080
            }
            contentType(ContentType.Application.Json)
            
            // Sync read token for the request
            val token = runBlocking { preferencesManager.authToken.first() }
            if (token != null) {
                header("Authorization", "Bearer $token")
            }
        }
    }
}
