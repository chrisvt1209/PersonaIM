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
import androidx.compose.ui.unit.sp
import dev.compose.messenger.core.designsystem.component.BackgroundParticles
import dev.compose.messenger.R
import dev.compose.messenger.core.designsystem.theme.PersonaRed
import dev.compose.messenger.feature.chat.presentation.components.ChatTopBar
import dev.compose.messenger.feature.chat.presentation.components.GroupMembersDialog
import dev.compose.messenger.feature.chat.presentation.components.MessageInput
import dev.compose.messenger.feature.chat.presentation.components.MessageList
import dev.compose.messenger.feature.chat.presentation.rememberTranscriptState
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ChatRoute(
    conversationId: String,
    onBackClick: () -> Unit,
    onLeftGroup: () -> Unit,
    viewModel: ChatViewModel = koinViewModel { parametersOf(conversationId) }
) {
    val uiState by viewModel.uiState.collectAsState()

    var showMembersDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.leftGroup) {
        if (uiState.leftGroup) {
            showMembersDialog = false
            onLeftGroup()
        }
    }

    ChatScreen(
        conversationId = conversationId,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onGroupClick = {
            viewModel.onEvent(ChatEvent.LoadFriendsForInvite)
            showMembersDialog = true
        }
    )

    if (showMembersDialog) {
        val alreadyInGroup = uiState.participants.map { it.userId }.toSet()
        val invitable = uiState.availableFriendsToInvite.filter { it.id !in alreadyInGroup }

        GroupMembersDialog(
            participants = uiState.participants,
            myUserId = uiState.myUserId,
            myRole = uiState.myRole,
            invitableFriends = invitable,
            memberActionError = uiState.memberActionError,
            inviteError = uiState.inviteError,
            onRemove = { userId -> viewModel.onEvent(ChatEvent.RemoveMember(userId)) },
            onChangeRole = { userId, role -> viewModel.onEvent(ChatEvent.ChangeRole(userId, role)) },
            onInvite = { userId -> viewModel.onEvent(ChatEvent.InviteFriend(userId)) },
            onLeave = { viewModel.onEvent(ChatEvent.LeaveGroup) },
            onDismiss = { showMembersDialog = false }
        )
    }
}

@Composable
fun ChatScreen(
    conversationId: String,
    uiState: ChatUiState,
    onEvent: (ChatEvent) -> Unit,
    onBackClick: () -> Unit,
    onGroupClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            ChatTopBar(
                title = uiState.conversationTitle,
                onBackClick = onBackClick,
                showGroupButton = uiState.isGroup,
                onGroupClick = onGroupClick
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
                    participantAvatars = uiState.participantAvatars,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (uiState.sendError != null) {
                val error = uiState.sendError
                LaunchedEffect(error) {
                    delay(4000)
                    onEvent(ChatEvent.SendErrorShown)
                }
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEvent(ChatEvent.SendErrorShown) }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
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
