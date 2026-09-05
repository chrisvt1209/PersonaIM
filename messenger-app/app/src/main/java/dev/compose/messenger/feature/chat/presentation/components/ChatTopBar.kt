package dev.compose.messenger.feature.chat.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.compose.messenger.core.designsystem.component.PersonaTopBar

@Composable
fun ChatTopBar(
    title: String,
    onBackClick: () -> Unit,
    showGroupButton: Boolean = false,
    onGroupClick: () -> Unit = {}
) {
    PersonaTopBar(
        title = title,
        onLogoClick = onBackClick,
    ) {
        if (showGroupButton) {
            IconButton(onClick = onGroupClick) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Group Members",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
