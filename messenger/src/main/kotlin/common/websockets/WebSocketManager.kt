package common.websockets

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class WebSocketManager {
    private val sessions = mutableMapOf<Long, MutableSet<DefaultWebSocketServerSession>>()
    private val mutex = Mutex()

    suspend fun connect(
        conversationId: Long,
        session: DefaultWebSocketServerSession
    ) {
        mutex.withLock {
            sessions
                .getOrPut(conversationId) {
                    mutableSetOf()
                }
                .add(session)
        }
    }

    suspend fun disconnect(
        conversationId: Long,
        session: DefaultWebSocketServerSession
    ) {
        mutex.withLock {
            sessions[conversationId]?.remove(session)

            if (sessions[conversationId]?.isEmpty() == true) {
                sessions.remove(conversationId)
            }
        }
    }

    suspend fun broadcast(
        conversationId: Long,
        message: String
    ) {
        val currentSessions =
            mutex.withLock {
                sessions[conversationId]?.toList()
                    ?: emptyList()
            }

        currentSessions.forEach { session ->
            try {
                session.send(message)
            } catch (_: Exception) {
                disconnect(
                    conversationId,
                    session
                )
            }
        }
    }
}