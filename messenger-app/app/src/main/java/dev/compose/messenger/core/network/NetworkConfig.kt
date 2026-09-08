package dev.compose.messenger.core.network

/** Shared backend host, used by both the Ktor HTTP client and anything building raw URLs (e.g. avatar images). */
object NetworkConfig {
    const val HOST = "10.0.2.2" // Localhost from Android emulator
    const val PORT = 8080
    const val BASE_URL = "http://$HOST:$PORT"
}
