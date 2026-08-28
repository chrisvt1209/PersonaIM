package dev.compose.messenger.core.common.model

import androidx.annotation.DrawableRes
import dev.compose.messenger.R

enum class Avatar(val key: String, @DrawableRes val drawableRes: Int) {
    Ann("ann", R.drawable.ann),
    Ryuji("ryuji", R.drawable.ryuji),
    Yusuke("yusuke", R.drawable.yusuke);

    companion object {
        val Default = Ann

        fun fromKey(key: String): Avatar = entries.find { it.key == key } ?: Default
    }
}
