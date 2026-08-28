package dev.compose.messenger.feature.chat.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    season: Season,
    onSeasonChange: (Season) -> Unit,
    viewModel: ChatViewModel = koinViewModel { parametersOf(conversationId) }
) {
    val uiState by viewModel.uiState.collectAsState()

    var showInviteDialog by remember { mutableStateOf(false) }

    ChatScreen(
        conversationId = conversationId,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onInviteClick = {
            viewModel.onEvent(ChatEvent.LoadFriendsForInvite)
            showInviteDialog = true
        },
        season = season,
        onSeasonChange = onSeasonChange
    )

    if (showInviteDialog) {
        val alreadyInGroup = uiState.participants.map { it.userId }.toSet()
        val invitable = uiState.availableFriendsToInvite.filter { it.id !in alreadyInGroup }

        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = { Text("Invite to Group") },
            text = {
                Column {
                    if (uiState.inviteError != null) {
                        Text(uiState.inviteError!!, color = Color.Red)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (invitable.isEmpty()) {
                        Text("No more friends to invite.")
                    } else {
                        invitable.forEach { friend ->
                            Text(
                                text = friend.username,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onEvent(ChatEvent.InviteFriend(friend.id)) }
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showInviteDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
fun ChatScreen(
    conversationId: String,
    uiState: ChatUiState,
    onEvent: (ChatEvent) -> Unit,
    onBackClick: () -> Unit,
    onInviteClick: () -> Unit,
    season: Season,
    onSeasonChange: (Season) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            ChatTopBar(
                title = uiState.conversationTitle,
                subtitle = "active now",
                onBackClick = onBackClick,
                season = season,
                onSeasonChange = onSeasonChange,
                showInviteButton = uiState.isGroup,
                onInviteClick = onInviteClick
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }

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

            Spacer(modifier = Modifier.height(8.dp))

            MessageInput(
                draft = uiState.draft,
                onDraftChange = { onEvent(ChatEvent.DraftChanged(it)) },
                onSend = { onEvent(ChatEvent.SendClicked) },
                sendEnabled = uiState.draft.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
