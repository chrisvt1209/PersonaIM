package dev.compose.messenger.core.network

import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import kotlinx.serialization.Serializable
import java.io.IOException

@Serializable
data class ErrorResponse(val error: String)

// Never surface e.message straight to the UI: for ResponseException it embeds
// the raw request URL and response body, for other types it's a bare
// exception class name/stacktrace fragment. Both leak backend internals.
suspend fun Throwable.toUserMessage(): String = when (this) {
    is ResponseException -> runCatching { response.body<ErrorResponse>().error }
        .getOrDefault("Something went wrong. Please try again.")
    is IOException -> "Can't reach server. Check your connection."
    else -> "Something went wrong. Please try again."
}
