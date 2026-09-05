package dev.compose.messenger.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import dev.compose.messenger.core.designsystem.theme.OptimaNova
import dev.compose.messenger.core.designsystem.theme.PersonaRed
import dev.compose.messenger.core.designsystem.util.personaPanelBackground

/**
 * A persona-themed replacement for [androidx.compose.material3.AlertDialog]: angular red/black
 * panel, [OptimaNova] title, and a confirm/dismiss [PersonaButton] row.
 */
@Composable
fun PersonaDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    confirmEnabled: Boolean = true,
    dismissText: String = "Cancel",
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .personaPanelBackground(
                    accentColor = PersonaRed,
                    fillColor = Color.Black.copy(alpha = 0.92f),
                )
                .padding(horizontal = 28.dp, vertical = 26.dp),
        ) {
            Text(
                text = title.uppercase(),
                color = Color.White,
                fontFamily = OptimaNova,
                fontSize = 20.sp,
            )

            Spacer(modifier = Modifier.height(18.dp))

            content()

            Spacer(modifier = Modifier.height(22.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PersonaButton(
                    text = dismissText,
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f),
                )
                PersonaButton(
                    text = confirmText,
                    onClick = onConfirm,
                    enabled = confirmEnabled,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
