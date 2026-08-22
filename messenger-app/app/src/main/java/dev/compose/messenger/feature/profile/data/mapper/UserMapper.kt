package dev.compose.messenger.feature.profile.data.mapper

import dev.compose.messenger.core.database.entity.UserEntity
import dev.compose.messenger.core.network.api.UserDto
import dev.compose.messenger.feature.profile.domain.User

fun UserDto.toEntity() = UserEntity(
    id = id,
    username = username,
    email = email,
    bio = bio,
    avatarUrl = avatarUrl
)

fun UserEntity.toDomain() = User(
    id = id,
    username = username,
    email = email,
    bio = bio,
    avatarUrl = avatarUrl
)
