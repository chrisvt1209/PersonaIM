package dev.compose.messenger.feature.friends.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.compose.messenger.R
import dev.compose.messenger.core.common.model.Season
import dev.compose.messenger.core.designsystem.component.BackgroundParticles
import dev.compose.messenger.core.designsystem.theme.OptimaNova
import dev.compose.messenger.core.designsystem.theme.PersonaRed
import dev.compose.messenger.core.designsystem.util.personaPanelBackground
import dev.compose.messenger.feature.friends.domain.Friend
import org.koin.androidx.compose.koinViewModel

@Composable
fun FriendListRoute(
    onFriendClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    viewModel: FriendViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    FriendListScreen(
        uiState = uiState,
        onFriendClick = { userId ->
            viewModel.onEvent(FriendEvent.StartChat(userId))
            onFriendClick(userId)
        },
        onBackClick = onBackClick,
        onAddClick = { showAddDialog = true }
    )

    if (showAddDialog) {
        var emailText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Friend") },
            text = {
                Column {
                    Text("Enter friend's email address:")
                    OutlinedTextField(
                        value = emailText,
                        onValueChange = { emailText = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (emailText.isNotBlank()) {
                        viewModel.onEvent(FriendEvent.AddFriend(emailText))
                        showAddDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                Button(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FriendListScreen(
    uiState: FriendUiState,
    onFriendClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit
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
            modifier = Modifier.fillMaxSize()
        ) {
            FriendHeader(onBackClick = onBackClick, onAddClick = onAddClick)

            if (uiState.friends.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No friends yet. Add some!", color = Color.White, fontFamily = OptimaNova)
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
                            onClick = { onFriendClick(friend.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendHeader(
    onBackClick: () -> Unit,
    onAddClick: () -> Unit
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
            contentDescription = "Back",
            modifier = Modifier
                .height(100.dp)
                .offset(x = 4.dp, y = (-4).dp)
                .clickable { onBackClick() }
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .weight(1f)
                .padding(top = 12.dp, start = 4.dp),
        ) {
            Text(
                text = "FRIENDS",
                color = Color.White,
                fontFamily = OptimaNova,
                fontSize = 26.sp,
            )
            Text(
                text = "tap logo to go back",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
            )
        }

        IconButton(onClick = onAddClick, modifier = Modifier.padding(top = 8.dp)) {
            Icon(
                painter = painterResource(R.drawable.logo_im), // Using logo as placeholder icon
                contentDescription = "Add Friend",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun FriendItem(
    friend: Friend,
    onClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .personaPanelBackground(
                accentColor = Color.White,
                fillColor = Color.Black.copy(alpha = 0.76f),
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Text(
            text = friend.username,
            color = Color.White,
            fontFamily = OptimaNova,
            fontSize = 20.sp,
        )
        Text(
            text = friend.email,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
        )
    }
}
