package dev.compose.messenger.feature.conversations.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.compose.messenger.R
import dev.compose.messenger.core.common.model.Avatar
import dev.compose.messenger.core.designsystem.theme.OptimaNova
import dev.compose.messenger.core.designsystem.theme.PersonaRed
import dev.compose.messenger.core.designsystem.util.personaBadgeBackground
import dev.compose.messenger.core.designsystem.util.personaPanelBackground
import dev.compose.messenger.feature.conversations.domain.Conversation
import dev.compose.messenger.feature.conversations.domain.GroupInvite
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun ConversationListRoute(
    onConversationClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onAddClick: () -> Unit,
    viewModel: ConversationViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var conversationPendingDelete by remember { mutableStateOf<Conversation?>(null) }
    var showInvitesDialog by remember { mutableStateOf(false) }

    ConversationListScreen(
        uiState = uiState,
        onConversationClick = onConversationClick,
        onProfileClick = onProfileClick,
        onAddClick = onAddClick,
        onDeleteClick = { conversation -> conversationPendingDelete = conversation },
        onInvitesClick = { showInvitesDialog = true },
        onErrorShown = { viewModel.onEvent(ConversationEvent.ErrorShown) }
    )

    conversationPendingDelete?.let { conversation ->
        AlertDialog(
            onDismissRequest = { conversationPendingDelete = null },
            title = { Text("Delete Conversation") },
            text = { Text("Delete conversation with ${conversation.title}? This cannot be undone.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.onEvent(ConversationEvent.DeleteConversation(conversation.id))
                    conversationPendingDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { conversationPendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showInvitesDialog) {
        InvitesDialog(
            invites = uiState.invites,
            onAccept = { viewModel.onEvent(ConversationEvent.AcceptInvite(it)) },
            onDecline = { viewModel.onEvent(ConversationEvent.DeclineInvite(it)) },
            onDismiss = { showInvitesDialog = false }
        )
    }
}

@Composable
private fun InvitesDialog(
    invites: List<GroupInvite>,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Group Invites") },
        text = {
            if (invites.isEmpty()) {
                Text("No pending invites.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    invites.forEach { invite ->
                        Column {
                            Text(invite.title, fontFamily = OptimaNova, fontSize = 16.sp)
                            Text(
                                "${invite.memberCount} members",
                                fontSize = 12.sp,
                                color = Color.Black.copy(alpha = 0.6f)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { onAccept(invite.id) }) {
                                    Text("Accept")
                                }
                                Button(onClick = { onDecline(invite.id) }) {
                                    Text("Decline")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ConversationListScreen(
    uiState: ConversationUiState,
    onConversationClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onAddClick: () -> Unit,
    onDeleteClick: (Conversation) -> Unit,
    onInvitesClick: () -> Unit,
    onErrorShown: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            PersonaListHeader(
                userAvatar = uiState.userAvatar,
                inviteCount = uiState.invites.size,
                onProfileClick = onProfileClick,
                onAddClick = onAddClick,
                onInvitesClick = onInvitesClick
            )

            if (uiState.error != null) {
                ErrorBanner(message = uiState.error, onDismissed = onErrorShown)
            }

            if (uiState.conversations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text(
                            "No messages yet.\nTap logo to find friends!",
                            color = Color.White,
                            fontFamily = OptimaNova,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.conversations) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            onClick = { onConversationClick(conversation.id) },
                            onDeleteClick = { onDeleteClick(conversation) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onDismissed: () -> Unit
) {
    LaunchedEffect(message) {
        delay(4000)
        onDismissed()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PersonaRed.copy(alpha = 0.9f))
            .clickable { onDismissed() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(text = message, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun PersonaListHeader(
    userAvatar: String,
    inviteCount: Int,
    onProfileClick: () -> Unit,
    onAddClick: () -> Unit,
    onInvitesClick: () -> Unit
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
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .weight(1f)
                .padding(top = 12.dp, start = 4.dp),
        ) {
            Text(
                text = "MESSAGES",
                color = Color.White,
                fontFamily = OptimaNova,
                fontSize = 26.sp,
            )
        }

        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (inviteCount > 0) {
                Box {
                    IconButton(onClick = onInvitesClick) {
                        Icon(
                            imageVector = Icons.Default.MarkEmailUnread,
                            contentDescription = "Group Invites",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 6.dp)
                            .size(16.dp)
                            .background(PersonaRed, shape = androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Text(
                            text = inviteCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            IconButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Friends",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Image(
                painter = painterResource(Avatar.fromKey(userAvatar).drawableRes),
                contentDescription = "Profile",
                modifier = Modifier
                    .height(60.dp)
                    .clickable { onProfileClick() }
            )
        }
    }
}

@Composable
private fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        label = "conversation-scale",
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            )
            .personaPanelBackground(
                accentColor = conversation.accentColor,
                fillColor = Color.Black.copy(alpha = 0.76f),
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = conversation.title,
                color = Color.White,
                fontFamily = OptimaNova,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f)
            )

            if (conversation.unreadCount > 0) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .personaBadgeBackground(conversation.accentColor)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "${conversation.unreadCount} new",
                        color = Color.Black,
                        fontFamily = OptimaNova,
                        fontSize = 13.sp,
                    )
                }
            }

            IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete conversation",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Text(
            text = conversation.participantNames,
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = conversation.lastMessage,
            color = Color.White.copy(alpha = 0.82f),
            maxLines = 2,
            fontSize = 13.sp,
            modifier = Modifier.height(34.dp),
        )
    }
}
