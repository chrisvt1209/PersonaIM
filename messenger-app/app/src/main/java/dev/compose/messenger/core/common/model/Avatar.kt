package dev.compose.messenger.core.common.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import coil3.compose.rememberAsyncImagePainter
import dev.compose.messenger.R
import dev.compose.messenger.core.network.LocalAvatarImageLoader
import dev.compose.messenger.core.network.NetworkConfig

enum class Avatar(val key: String, @DrawableRes val drawableRes: Int) {
    Ann("ann", R.drawable.ann),
    Ryuji("ryuji", R.drawable.ryuji),
    Yusuke("yusuke", R.drawable.yusuke);

    companion object {
        val Default = Ann

        fun fromKey(key: String): Avatar = entries.find { it.key == key } ?: Default
    }
}

/** Sentinel [Avatar.key] used for a user-uploaded, stylized photo instead of a bundled preset. */
const val CUSTOM_AVATAR_KEY = "custom"

fun avatarImageUrl(userId: Long): String = "${NetworkConfig.BASE_URL}/users/$userId/avatar-image"

/** Resolves a preset drawable or the user's uploaded avatar image, depending on [avatarKey]. */
@Composable
fun rememberAvatarPainter(userId: Long, avatarKey: String): Painter {
    return if (avatarKey == CUSTOM_AVATAR_KEY) {
        rememberAsyncImagePainter(
            model = avatarImageUrl(userId),
            imageLoader = LocalAvatarImageLoader.current,
        )
    } else {
        painterResource(Avatar.fromKey(avatarKey).drawableRes)
    }
}
