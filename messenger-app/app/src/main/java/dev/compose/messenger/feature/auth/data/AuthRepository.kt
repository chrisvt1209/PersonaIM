package dev.compose.messenger.feature.auth.data

import dev.compose.messenger.core.database.MessengerDatabase
import dev.compose.messenger.core.datastore.PreferencesManager
import dev.compose.messenger.core.network.api.AuthApi
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import java.io.IOException

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<AuthResponse>
    suspend fun register(request: RegisterRequest): Result<AuthResponse>
    suspend fun logout()
}

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val preferencesManager: PreferencesManager,
    private val database: MessengerDatabase
) : AuthRepository {
    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = api.login(request)
            preferencesManager.saveAuthToken(response.token)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(e.toSafeMessage()))
        }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = api.register(request)
            preferencesManager.saveAuthToken(response.token)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(e.toSafeMessage()))
        }
    }

    override suspend fun logout() {
        preferencesManager.clear()
        database.clearAllTables()
    }

    // Never surface e.message straight to the UI: for ResponseException it embeds
    // the raw request URL and response body, for other types it's a bare
    // exception class name/stacktrace fragment. Both leak backend internals.
    private suspend fun Exception.toSafeMessage(): String = when (this) {
        is ResponseException -> runCatching { response.body<ErrorResponse>().error }
            .getOrDefault("Something went wrong. Please try again.")
        is IOException -> "Can't reach server. Check your connection."
        else -> "Something went wrong. Please try again."
    }
}
