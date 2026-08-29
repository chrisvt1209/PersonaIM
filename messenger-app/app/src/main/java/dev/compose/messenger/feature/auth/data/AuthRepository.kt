package dev.compose.messenger.feature.auth.data

import dev.compose.messenger.core.database.MessengerDatabase
import dev.compose.messenger.core.datastore.PreferencesManager
import dev.compose.messenger.core.network.api.AuthApi
import dev.compose.messenger.core.network.toUserMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
            withContext(Dispatchers.IO) { database.clearAllTables() }
            preferencesManager.saveAuthToken(response.token)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(e.toUserMessage()))
        }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = api.register(request)
            withContext(Dispatchers.IO) { database.clearAllTables() }
            preferencesManager.saveAuthToken(response.token)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(e.toUserMessage()))
        }
    }

    override suspend fun logout() {
        preferencesManager.clear()
        withContext(Dispatchers.IO) { database.clearAllTables() }
    }
}
