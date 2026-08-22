package dev.compose.messenger.feature.friends.data.mapper

import dev.compose.messenger.core.database.entity.FriendEntity
import dev.compose.messenger.core.network.api.FriendDto
import dev.compose.messenger.feature.friends.domain.Friend

fun FriendDto.toEntity() = FriendEntity(
    id = id,
    username = username,
    email = email
)

fun FriendEntity.toDomain() = Friend(
    id = id,
    username = username,
    email = email
)
