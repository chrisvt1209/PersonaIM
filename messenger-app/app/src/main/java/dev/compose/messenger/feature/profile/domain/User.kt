package dev.compose.messenger.feature.profile.domain

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val uid: String,
    val avatar: String,
    val bio: String? = null
)
