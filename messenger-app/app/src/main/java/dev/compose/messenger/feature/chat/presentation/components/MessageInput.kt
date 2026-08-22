package dev.compose.messenger.feature.chat.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.compose.messenger.core.designsystem.component.PersonaButton
import dev.compose.messenger.core.designsystem.component.PersonaTextField

@Composable
fun MessageInput(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    sendEnabled: Boolean
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .systemBarsPadding()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp),
    ) {
        PersonaTextField(
            value = draft,
            onValueChange = onDraftChange,
            placeholder = "Send a reply...",
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            modifier = Modifier.weight(1f)
        )

        PersonaButton(
            text = "SEND",
            enabled = sendEnabled,
            onClick = {
                onSend()
                keyboardController?.hide()
            }
        )
    }
}
