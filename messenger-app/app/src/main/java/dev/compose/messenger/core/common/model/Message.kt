package dev.compose.messenger.core.common.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import dev.compose.messenger.R

data class Message(
    val id: Long,
    val sender: Sender,
    val text: String,
    val timestamp: String,
)

enum class Sender(
    @DrawableRes val image: Int,
    val color: Color,
    val displayName: String,
) {
    Ann(
        image = R.drawable.ann,
        color = Color(0xFFFE93C9),
        displayName = "Ann",
    ),
    Ryuji(
        image = R.drawable.ryuji,
        color = Color(0xFFF0EA40),
        displayName = "Ryuji",
    ),
    Yusuke(
        image = R.drawable.yusuke,
        color = Color(0xFF1BC8F9),
        displayName = "Yusuke",
    ),

    // Ren is the player character, and has no avatar in chat.
    Ren(
        image = -1,
        color = Color.Unspecified,
        displayName = "You",
    );

    companion object {
        fun fromId(id: Long): Sender {
            return entries.getOrNull(id.toInt()) ?: Ren // Mock mapping
        }
    }
}
