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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.compose.messenger.R
import dev.compose.messenger.core.common.model.Avatar
import dev.compose.messenger.core.designsystem.component.PersonaAvatar
import dev.compose.messenger.core.designsystem.component.PersonaTopBar
import dev.compose.messenger.core.designsystem.component.randomAvatarColor
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.conversations) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            participantAvatars = uiState.participantAvatars,
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
    PersonaTopBar(
        title = "MESSAGES",
        onLogoClick = {},
    ) {
        if (inviteCount > 0) {
            Box {
                IconButton(onClick = onInvitesClick) {
                    Icon(
                        imageVector = Icons.Default.MarkEmailUnread,
                        contentDescription = "Group Invites",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 2.dp)
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
                modifier = Modifier.size(28.dp)
            )
        }

        Image(
            painter = painterResource(Avatar.fromKey(userAvatar).drawableRes),
            contentDescription = "Profile",
            modifier = Modifier
                .height(44.dp)
                .clickable { onProfileClick() }
        )
    }
}

@Composable
private fun ConversationItem(
    conversation: Conversation,
    participantAvatars: Map<Long, String>,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        label = "conversation-scale",
    )

    val containerModifier = Modifier
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
        .padding(horizontal = 18.dp, vertical = 12.dp)

    if (conversation.isGroup) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = containerModifier,
        ) {
            ConversationTitleRow(conversation, onDeleteClick)

            Row(verticalAlignment = Alignment.CenterVertically) {
                ParticipantAvatars(
                    participantIds = conversation.participantIds,
                    avatars = participantAvatars,
                )

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = conversation.lastMessage,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 1,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = containerModifier,
        ) {
            ParticipantAvatars(
                participantIds = conversation.participantIds,
                avatars = participantAvatars,
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f),
            ) {
                ConversationTitleRow(conversation, onDeleteClick)

                Text(
                    text = conversation.lastMessage,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 1,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun ConversationTitleRow(
    conversation: Conversation,
    onDeleteClick: () -> Unit,
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
            fontSize = 18.sp,
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

            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .personaBadgeBackground(PersonaRed)
                .clickable(onClick = onDeleteClick)
                .padding(6.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete conversation",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private val AvatarChipSize = 44.dp
private val AvatarChipAspect = 110f / 90f
private val AvatarChipWidth = AvatarChipSize * AvatarChipAspect
private val AvatarChipOverlap = 34.dp
private const val MaxVisibleAvatars = 3

@Composable
private fun ParticipantAvatars(
    participantIds: List<Long>,
    avatars: Map<Long, String>,
    modifier: Modifier = Modifier,
) {
    if (participantIds.isEmpty()) return

    val visibleIds = participantIds.take(MaxVisibleAvatars)
    val hasOverflow = participantIds.size > MaxVisibleAvatars
    val clusterWidth = AvatarChipWidth + AvatarChipOverlap * (visibleIds.size - 1)

    Box(
        modifier = modifier
            .width(clusterWidth)
            .height(AvatarChipSize)
    ) {
        visibleIds.forEachIndexed { index, userId ->
            val isFadedOverflowChip = hasOverflow && index == visibleIds.lastIndex
            val avatarKey = avatars[userId].orEmpty()

            PersonaAvatar(
                drawableRes = Avatar.fromKey(avatarKey).drawableRes,
                backgroundColor = randomAvatarColor(avatarKey),
                size = AvatarChipSize,
                modifier = Modifier
                    .offset(x = AvatarChipOverlap * index)
                    .then(
                        if (isFadedOverflowChip) {
                            Modifier.drawWithContent {
                                drawContent()
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        0f to Color.Transparent,
                                        1f to Color.Black,
                                    ),
                                    blendMode = BlendMode.DstOut,
                                )
                            }
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}
