package dev.compose.messenger.feature.chat.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.compose.messenger.core.designsystem.theme.PersonaRed
import dev.compose.messenger.feature.conversations.domain.Participant
import dev.compose.messenger.feature.conversations.domain.ParticipantRole
import dev.compose.messenger.feature.friends.domain.Friend

private val AssignableRoles = listOf(
    ParticipantRole.MEMBER,
    ParticipantRole.INVITER,
    ParticipantRole.MAINTAINER,
)

private fun String.toDisplayLabel() = lowercase().replaceFirstChar { it.uppercaseChar() }

@Composable
fun GroupMembersDialog(
    participants: List<Participant>,
    myUserId: Long?,
    myRole: String?,
    invitableFriends: List<Friend>,
    memberActionError: String?,
    inviteError: String?,
    onRemove: (userId: Long) -> Unit,
    onChangeRole: (userId: Long, role: String) -> Unit,
    onInvite: (userId: Long) -> Unit,
    onLeave: () -> Unit,
    onDismiss: () -> Unit
) {
    val canInvite = myRole == ParticipantRole.INVITER || myRole == ParticipantRole.MAINTAINER
    val canManage = myRole == ParticipantRole.MAINTAINER

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Group Members") },
        text = {
            Column {
                if (memberActionError != null) {
                    Text(memberActionError, color = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                participants.forEach { participant ->
                    MemberRow(
                        participant = participant,
                        isSelf = participant.userId == myUserId,
                        canManage = canManage,
                        onRemove = { onRemove(participant.userId) },
                        onChangeRole = { role -> onChangeRole(participant.userId, role) }
                    )
                }

                if (canInvite) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Invite friends", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    if (inviteError != null) {
                        Text(inviteError, color = Color.Red)
                    }

                    if (invitableFriends.isEmpty()) {
                        Text("No more friends to invite.")
                    } else {
                        invitableFriends.forEach { friend ->
                            Text(
                                text = friend.username,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onInvite(friend.id) }
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Leave Group",
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLeave() }
                        .padding(vertical = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun MemberRow(
    participant: Participant,
    isSelf: Boolean,
    canManage: Boolean,
    onRemove: () -> Unit,
    onChangeRole: (String) -> Unit
) {
    val canModifyThisMember = canManage && !isSelf && participant.role != ParticipantRole.MAINTAINER

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(participant.username + if (isSelf) " (you)" else "")
                Text(
                    text = participant.role.toDisplayLabel() +
                        if (participant.status == "PENDING") " · pending" else "",
                    color = if (participant.role == ParticipantRole.MAINTAINER) PersonaRed else Color.Gray,
                    fontSize = 12.sp
                )
            }
            if (canModifyThisMember) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove ${participant.username}"
                    )
                }
            }
        }

        if (canModifyThisMember) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AssignableRoles.filter { it != participant.role }.forEach { role ->
                    Text(
                        text = "Make ${role.toDisplayLabel()}",
                        fontSize = 12.sp,
                        color = PersonaRed,
                        modifier = Modifier.clickable { onChangeRole(role) }
                    )
                }
            }
        }
    }
}
