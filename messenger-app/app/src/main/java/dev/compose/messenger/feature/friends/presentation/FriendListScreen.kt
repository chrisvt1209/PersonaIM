package dev.compose.messenger.feature.friends.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import dev.compose.messenger.core.designsystem.component.SeasonMenu
import dev.compose.messenger.R
import dev.compose.messenger.core.common.model.Season
import dev.compose.messenger.core.designsystem.theme.OptimaNova
import dev.compose.messenger.core.designsystem.util.personaPanelBackground
import dev.compose.messenger.feature.friends.domain.Friend
import org.koin.androidx.compose.koinViewModel

@Composable
fun FriendListRoute(
    onNavigate: (String) -> Unit,
    onBackClick: () -> Unit,
    season: Season,
    onSeasonChange: (Season) -> Unit,
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

    FriendListScreen(
        uiState = uiState,
        onFriendClick = { userId ->
            viewModel.onEvent(FriendEvent.StartChat(userId))
        },
        onBackClick = onBackClick,
        onAddClick = { showAddDialog = true },
        season = season,
        onSeasonChange = onSeasonChange
    )

    if (showAddDialog) {
        var uidText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Friend") },
            text = {
                Column {
                    Text("Enter friend's UID:")
                    OutlinedTextField(
                        value = uidText,
                        onValueChange = { uidText = it },
                        label = { Text("UID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (uidText.isNotBlank()) {
                        viewModel.onEvent(FriendEvent.AddFriend(uidText))
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
    onAddClick: () -> Unit,
    season: Season,
    onSeasonChange: (Season) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = Color.White,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Friend")
            }
        },
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
                    season = season,
                    onSeasonChange = onSeasonChange
                )

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
    season: Season,
    onSeasonChange: (Season) -> Unit
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
                text = headerStatus(season),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            SeasonMenu(
                hostElement = {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Change Season",
                        tint = Color.White,
                        modifier = Modifier.padding(top = 12.dp).size(28.dp)
                    )
                },
                onSeasonChange = onSeasonChange
            )

            IconButton(onClick = onAddFriendClick, modifier = Modifier.padding(top = 8.dp)) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Add Friend",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

private fun headerStatus(season: Season): String {
    val seasonLabel = when (season) {
        Season.NONE -> "clean feed"
        Season.SPRING -> "spring flair"
        Season.WINTER -> "winter flair"
    }
    return "recent contacts | $seasonLabel"
}

@Composable
fun FriendItem(
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
