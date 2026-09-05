package dev.compose.messenger.feature.friends.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.compose.messenger.R
import dev.compose.messenger.core.common.model.Avatar
import dev.compose.messenger.core.designsystem.component.PersonaAvatar
import dev.compose.messenger.core.designsystem.component.PersonaDialog
import dev.compose.messenger.core.designsystem.component.PersonaTextField
import dev.compose.messenger.core.designsystem.component.PersonaTopBar
import dev.compose.messenger.core.designsystem.component.randomAvatarColor
import dev.compose.messenger.core.designsystem.theme.OptimaNova
import dev.compose.messenger.core.designsystem.theme.PersonaRed
import dev.compose.messenger.core.designsystem.util.personaPanelBackground
import dev.compose.messenger.feature.friends.domain.Friend
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun FriendListRoute(
    onNavigate: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: FriendViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navEvent by viewModel.navigationEvent.collectAsState()
    
    LaunchedEffect(navEvent) {
        navEvent?.let { route ->
            onNavigate(route)
            viewModel.onNavigationHandled()
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var showNewGroupDialog by remember { mutableStateOf(false) }

    FriendListScreen(
        uiState = uiState,
        onFriendClick = { userId ->
            viewModel.onEvent(FriendEvent.StartChat(userId))
        },
        onBackClick = onBackClick,
        onAddClick = { showAddDialog = true },
        onNewGroupClick = { showNewGroupDialog = true },
        onErrorShown = { viewModel.onEvent(FriendEvent.ErrorShown) }
    )

    if (showAddDialog) {
        var uidText by remember { mutableStateOf("") }
        PersonaDialog(
            title = "Add Friend",
            onDismissRequest = { showAddDialog = false },
            confirmText = "Add",
            confirmEnabled = uidText.isNotBlank(),
            onConfirm = {
                if (uidText.isNotBlank()) {
                    viewModel.onEvent(FriendEvent.AddFriend(uidText))
                    showAddDialog = false
                }
            },
        ) {
            Text(
                text = "Enter friend's UID:",
                color = Color.White,
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            PersonaTextField(
                value = uidText,
                onValueChange = { uidText = it },
                placeholder = "UID",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showNewGroupDialog) {
        var groupName by remember { mutableStateOf("") }
        val selectedFriendIds = remember { mutableStateListOf<Long>() }

        PersonaDialog(
            title = "New Group",
            onDismissRequest = { showNewGroupDialog = false },
            confirmText = "Create",
            confirmEnabled = groupName.isNotBlank() && selectedFriendIds.isNotEmpty(),
            onConfirm = {
                viewModel.onEvent(
                    FriendEvent.CreateGroup(groupName, selectedFriendIds.toList())
                )
                showNewGroupDialog = false
            },
        ) {
            PersonaTextField(
                value = groupName,
                onValueChange = { groupName = it },
                placeholder = "Group name",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Select friends to invite:",
                color = Color.White,
                fontSize = 14.sp,
            )
            Column(modifier = Modifier.fillMaxWidth()) {
                uiState.friends.forEach { friend ->
                    val checked = selectedFriendIds.contains(friend.id)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (checked) {
                                    selectedFriendIds.remove(friend.id)
                                } else {
                                    selectedFriendIds.add(friend.id)
                                }
                            }
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    selectedFriendIds.add(friend.id)
                                } else {
                                    selectedFriendIds.remove(friend.id)
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = PersonaRed,
                                uncheckedColor = Color.White,
                                checkmarkColor = Color.White,
                            )
                        )
                        Text(
                            text = friend.username,
                            color = Color.White,
                            fontFamily = OptimaNova,
                            fontSize = 15.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FriendListScreen(
    uiState: FriendUiState,
    onFriendClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onNewGroupClick: () -> Unit,
    onErrorShown: () -> Unit = {}
) {
    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                FriendHeader(
                    onBackClick = onBackClick,
                    onAddFriendClick = onAddClick,
                    onNewGroupClick = onNewGroupClick
                )

                if (uiState.error != null) {
                    val error = uiState.error
                    LaunchedEffect(error) {
                        delay(4000)
                        onErrorShown()
                    }
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onErrorShown() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                if (uiState.friends.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White)
                        } else {
                            Text(
                                "No friends yet. Add some!",
                                color = Color.White,
                                fontFamily = OptimaNova
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
                        items(uiState.friends) { friend ->
                            FriendItem(
                                friend = friend,
                                avatarKey = uiState.friendAvatars[friend.id],
                                onClick = { onFriendClick(friend.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendHeader(
    onBackClick: () -> Unit,
    onAddFriendClick: () -> Unit,
    onNewGroupClick: () -> Unit
) {
    PersonaTopBar(
        title = "FRIENDS",
        onLogoClick = onBackClick,
    ) {
        IconButton(onClick = onAddFriendClick) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = "Add Friend",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        IconButton(onClick = onNewGroupClick) {
            Icon(
                imageVector = Icons.Default.GroupAdd,
                contentDescription = "New Group",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}


@Composable
fun FriendItem(
    friend: Friend,
    avatarKey: String?,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .personaPanelBackground(
                accentColor = Color.White,
                fillColor = Color.Black.copy(alpha = 0.76f),
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        PersonaAvatar(
            drawableRes = Avatar.fromKey(avatarKey.orEmpty()).drawableRes,
            backgroundColor = randomAvatarColor(avatarKey.orEmpty()),
            size = 64.dp,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = friend.username,
            color = Color.White,
            fontFamily = OptimaNova,
            fontSize = 20.sp,
        )
    }
}
