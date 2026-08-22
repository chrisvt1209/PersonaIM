package dev.compose.messenger.feature.chat.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.compose.messenger.core.designsystem.component.BackgroundParticles
import dev.compose.messenger.R
import dev.compose.messenger.core.common.model.Season
import dev.compose.messenger.core.designsystem.theme.PersonaRed
import dev.compose.messenger.feature.chat.presentation.components.ChatTopBar
import dev.compose.messenger.feature.chat.presentation.components.MessageInput
import dev.compose.messenger.feature.chat.presentation.components.MessageList
import dev.compose.messenger.feature.chat.presentation.rememberTranscriptState
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ChatRoute(
    conversationId: String,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = koinViewModel { parametersOf(conversationId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // We need to fetch the conversation details for the header
    // For now we'll pass the conversationId and assume the ViewModel or a separate state handles it
    
    ChatScreen(
        conversationId = conversationId,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick
    )
}

@Composable
fun ChatScreen(
    conversationId: String,
    uiState: ChatUiState,
    onEvent: (ChatEvent) -> Unit,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = PersonaRed)
    ) {
        BackgroundParticles(Season.SPRING)

        Image(
            painter = painterResource(R.drawable.bg_splatter_background),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .statusBarsPadding()
                .offset(y = (-16).dp)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            ChatTopBar(
                title = "Chat", // TODO: Get from conversation state
                subtitle = "active now",
                onBackClick = onBackClick
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                val transcriptState = rememberTranscriptState(
                    conversationKey = conversationId,
                    messages = uiState.messages
                )

                MessageList(
                    entries = transcriptState.entries,
                    showTypingIndicator = uiState.typingSender != null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            MessageInput(
                draft = uiState.draft,
                onDraftChange = { onEvent(ChatEvent.DraftChanged(it)) },
                onSend = { onEvent(ChatEvent.SendClicked) },
                sendEnabled = uiState.draft.isNotBlank()
            )
        }
    }
}
