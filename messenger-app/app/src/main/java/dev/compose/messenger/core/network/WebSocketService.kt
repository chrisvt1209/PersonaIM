package dev.compose.messenger.core.network

import dev.compose.messenger.core.datastore.PreferencesManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class WebSocketService(
    private val client: HttpClient,
    private val preferencesManager: PreferencesManager
) {
    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            connect()
        }
    }

    private suspend fun connect() {
        val token = preferencesManager.authToken.first() ?: return
        
        try {
            val session = client.webSocketSession {
                url("ws://10.0.2.2:8080/chat")
                // Token is already in defaultRequest header for HttpClient
            }
            
            session.incoming
                .consumeAsFlow()
                .filterIsInstance<Frame.Text>()
                .collect { frame ->
                    _messages.emit(frame.readText())
                }
        } catch (e: Exception) {
            e.printStackTrace()
            // TODO: Implement retry logic
        }
    }
}
