package dev.compose.messenger.feature.chat.presentation.components

import androidx.compose.runtime.Composable
import dev.compose.messenger.core.common.model.rememberAvatarPainter
import dev.compose.messenger.core.designsystem.component.PersonaAvatar
import dev.compose.messenger.core.designsystem.component.randomAvatarColor
import dev.compose.messenger.feature.chat.presentation.ChatEntry
import dev.compose.messenger.feature.chat.presentation.TranscriptSizes

@Composable
fun Avatar(entry: ChatEntry, avatarKey: String?) {
    PersonaAvatar(
        painter = rememberAvatarPainter(entry.message.senderId, avatarKey.orEmpty()),
        backgroundColor = randomAvatarColor(avatarKey.orEmpty()),
        size = TranscriptSizes.AvatarSize.height,
        backgroundScale = entry.avatarBackgroundScale.value,
        foregroundScale = entry.avatarForegroundScale.value,
    )
}
