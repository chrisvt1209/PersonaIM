package dev.compose.messenger.feature.conversations.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.compose.messenger.R
import dev.compose.messenger.core.common.model.Season
import dev.compose.messenger.core.designsystem.component.SeasonMenu
import dev.compose.messenger.core.designsystem.theme.OptimaNova
import dev.compose.messenger.core.designsystem.util.personaBadgeBackground
import dev.compose.messenger.core.designsystem.util.personaPanelBackground
import dev.compose.messenger.feature.conversations.domain.Conversation
import org.koin.androidx.compose.koinViewModel

@Composable
fun ConversationListRoute(
    onConversationClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onAddClick: () -> Unit,
    season: Season,
    onSeasonChange: (Season) -> Unit,
    viewModel: ConversationViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ConversationListScreen(
        uiState = uiState,
        onConversationClick = onConversationClick,
        onProfileClick = onProfileClick,
        onAddClick = onAddClick,
        season = season,
        onSeasonChange = onSeasonChange
    )
}

@Composable
fun ConversationListScreen(
    uiState: ConversationUiState,
    onConversationClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onAddClick: () -> Unit,
    season: Season,
    onSeasonChange: (Season) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            PersonaListHeader(
                onProfileClick = onProfileClick,
                onAddClick = onAddClick,
                season = season,
                onSeasonChange = onSeasonChange
            )

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
                            onClick = { onConversationClick(conversation.id) }
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
private fun PersonaListHeader(
    onProfileClick: () -> Unit,
    onAddClick: () -> Unit,
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
            Text(
                text = headerStatus(season),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
            )
        }

        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SeasonMenu(
                hostElement = {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Change Season",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp).padding(4.dp)
                    )
                },
                onSeasonChange = onSeasonChange
            )

            IconButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Friends",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Image(
                painter = painterResource(R.drawable.ann),
                contentDescription = "Profile",
                modifier = Modifier
                    .height(60.dp)
                    .clickable { onProfileClick() }
            )
        }
    }
}

private fun headerStatus(season: Season): String {
    val seasonLabel = when (season) {
        Season.NONE -> "clean feed"
        Season.SPRING -> "spring flair"
        Season.WINTER -> "winter flair"
    }
    return "recent chatter | $seasonLabel"
}

@Composable
private fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
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
