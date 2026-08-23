package dev.compose.messenger.feature.chat.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
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

import dev.compose.messenger.core.common.model.Season
import dev.compose.messenger.core.designsystem.component.SeasonMenu

@Composable
fun ChatTopBar(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    season: Season,
    onSeasonChange: (Season) -> Unit
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 8.dp, top = 2.dp, end = 12.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.logo_im),
            contentDescription = "Home",
            modifier = Modifier
                .height(100.dp)
                .offset(x = 4.dp, y = (-4).dp)
                .clickable { onBackClick() }
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .weight(1f)
                .padding(top = 12.dp, start = 4.dp),
        ) {
            Text(
                text = title,
                color = Color.White,
                fontFamily = OptimaNova,
                fontSize = 26.sp,
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
            )
        }

        SeasonMenu(
            hostElement = {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Change Season",
                    tint = Color.White,
                    modifier = Modifier.padding(top = 12.dp).size(28.dp)
                )
            },
            onSeasonChange = onSeasonChange
        )
    }
}
