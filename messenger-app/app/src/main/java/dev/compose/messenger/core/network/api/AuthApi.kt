package dev.compose.messenger.core.network.api

import dev.compose.messenger.feature.auth.data.AuthResponse
import dev.compose.messenger.feature.auth.data.LoginRequest
import dev.compose.messenger.feature.auth.data.RegisterRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthApi(private val client: HttpClient) {
    suspend fun login(request: LoginRequest): AuthResponse {
        return client.post("auth/login") {
            setBody(request)
        }.body()
    }

    suspend fun register(request: RegisterRequest): AuthResponse {
        return client.post("auth/register") {
            setBody(request)
        }.body()
    }
}
