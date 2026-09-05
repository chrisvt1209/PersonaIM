package dev.compose.messenger.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.compose.messenger.R
import dev.compose.messenger.core.designsystem.theme.OptimaNova

/**
 * Shared top bar used on every screen except the auth screen: small logo, screen title,
 * and any trailing icon actions, all vertically centered against each other.
 */
@Composable
fun PersonaTopBar(
    title: String,
    onLogoClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 8.dp, top = 4.dp, bottom = 4.dp, end = 12.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.logo_im),
            contentDescription = "Home",
            modifier = Modifier
                .height(56.dp)
                .clickable { onLogoClick() }
        )

        Text(
            text = title,
            color = Color.White,
            fontFamily = OptimaNova,
            fontSize = 20.sp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        )

        actions()
    }
}
