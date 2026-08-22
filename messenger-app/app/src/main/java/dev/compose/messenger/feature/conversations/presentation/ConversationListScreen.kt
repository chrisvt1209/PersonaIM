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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.compose.messenger.core.designsystem.component.BackgroundParticles
import dev.compose.messenger.R
import dev.compose.messenger.core.common.model.Season
import dev.compose.messenger.core.designsystem.theme.OptimaNova
import dev.compose.messenger.core.designsystem.theme.PersonaRed
import dev.compose.messenger.core.common.model.conversationSeeds
import dev.compose.messenger.core.designsystem.util.personaBadgeBackground
import dev.compose.messenger.core.designsystem.util.personaPanelBackground
import dev.compose.messenger.feature.conversations.domain.Conversation
import org.koin.androidx.compose.koinViewModel

@Composable
fun ConversationListRoute(
    onConversationClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onAddClick: () -> Unit,
    viewModel: ConversationViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ConversationListScreen(
        uiState = uiState,
        onConversationClick = onConversationClick,
        onProfileClick = onProfileClick,
        onAddClick = onAddClick
    )
}

@Composable
fun ConversationListScreen(
    uiState: ConversationUiState,
    onConversationClick: (String) -> Unit,
    onProfileClick: () -> Unit,
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
            PersonaListHeader(
                onProfileClick = onProfileClick,
                onAddClick = onAddClick
            )

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

@Composable
private fun PersonaListHeader(
    onProfileClick: () -> Unit,
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
            contentDescription = "Friends",
            modifier = Modifier
                .height(100.dp)
                .offset(x = 4.dp, y = (-4).dp)
                .clickable { onAddClick() }
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
                text = "tap logo for friends | clean feed",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
            )
        }

        Image(
            painter = painterResource(R.drawable.ann),
            contentDescription = "Profile",
            modifier = Modifier
                .padding(top = 8.dp)
                .height(80.dp)
                .clickable { onProfileClick() }
        )
    }
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
