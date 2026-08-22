package dev.compose.messenger.feature.auth.data

import dev.compose.messenger.core.database.MessengerDatabase
import dev.compose.messenger.core.datastore.PreferencesManager
import dev.compose.messenger.core.network.api.AuthApi
import kotlinx.coroutines.delay

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
            Result.failure(e)
        }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = api.register(request)
            preferencesManager.saveAuthToken(response.token)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        preferencesManager.clear()
        database.clearAllTables()
    }
}
