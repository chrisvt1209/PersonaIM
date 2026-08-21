package dev.compose.messenger

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val appState = rememberChatAppState()
            val conversation = appState.selectedConversation
            val transcriptState = rememberTranscriptState(
                conversationKey = conversation.id,
                messages = conversation.messagesSnapshot(),
            )

            RootContainer {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = PersonaRed)
                ) {
                    BackgroundParticles(appState.season)

                    Image(
                        painter = painterResource(R.drawable.bg_splatter_background),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .statusBarsPadding()
                            .offset(y = (-16).dp)
                    )

                    Column(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        PersonaHeader(
                            conversation = conversation,
                            season = appState.season,
                            onSeasonChange = appState::changeSeason,
                        )

                        ConversationStrip(
                            conversations = appState.conversations,
                            selectedConversationId = appState.selectedConversationId,
                            onConversationSelected = appState::selectConversation,
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        ) {
                            Transcript(
                                entries = transcriptState.entries,
                                showTypingIndicator = conversation.typingSender != null,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        ComposerBar(
                            draft = appState.draft,
                            onDraftChange = appState::updateDraft,
                            onSend = appState::sendMessage,
                            sendEnabled = appState.draft.isNotBlank(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonaHeader(
    conversation: ConversationState,
    season: Season,
    onSeasonChange: (Season) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 8.dp, top = 2.dp, end = 12.dp),
    ) {
        SeasonMenu(
            hostElement = {
                Image(
                    painter = painterResource(R.drawable.logo_im),
                    contentDescription = "Persona IM logo",
                    modifier = Modifier.height(100.dp)
                )
            },
            onSeasonChange = onSeasonChange,
            modifier = Modifier.offset(x = 4.dp, y = (-4).dp),
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .weight(1f)
                .padding(top = 12.dp, start = 4.dp),
        ) {
            Text(
                text = conversation.title,
                color = Color.White,
                fontFamily = OptimaNova,
                fontSize = 26.sp,
            )
            Text(
                text = headerStatus(conversation, season),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
            )
        }

        Portraits(
            senders = conversation.participants,
            modifier = Modifier
                .padding(top = 8.dp)
                .widthIn(min = 96.dp, max = 210.dp),
        )
    }
}

@Composable
private fun ConversationStrip(
    conversations: List<ConversationState>,
    selectedConversationId: String,
    onConversationSelected: (String) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        conversations.forEach { conversation ->
            ConversationCard(
                conversation = conversation,
                selected = conversation.id == selectedConversationId,
                onClick = { onConversationSelected(conversation.id) },
            )
        }
    }
}

@Composable
private fun ConversationCard(
    conversation: ConversationState,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        label = "conversation-scale",
    )
    val textColor = if (selected) Color.Black else Color.White
    val subtitleColor = if (selected) Color.Black.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.82f)

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .width(220.dp)
            .scale(scale)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            )
            .personaPanelBackground(
                accentColor = conversation.accentColor,
                fillColor = if (selected) Color.White else Color.Black.copy(alpha = 0.76f),
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Text(
            text = conversation.title,
            color = textColor,
            fontFamily = OptimaNova,
            fontSize = 20.sp,
        )
        Text(
            text = conversation.participantNames,
            color = subtitleColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = conversation.lastMessagePreview,
            color = subtitleColor,
            maxLines = 2,
            fontSize = 13.sp,
            modifier = Modifier.height(34.dp),
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
}

@Composable
private fun ComposerBar(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    sendEnabled: Boolean,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .systemBarsPadding()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp),
    ) {
        BasicTextField(
            value = draft,
            onValueChange = onDraftChange,
            textStyle = TextStyle(
                color = Color.Black,
                fontFamily = OptimaNova,
                fontSize = 18.sp,
                lineHeight = 22.sp,
            ),
            cursorBrush = SolidColor(Color.Black),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    onSend()
                    keyboardController?.hide()
                },
            ),
            maxLines = 4,
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .composerBackground()
                        .padding(start = 24.dp, top = 18.dp, end = 24.dp, bottom = 18.dp),
                ) {
                    if (draft.isBlank()) {
                        Text(
                            text = "Send a reply to the group...",
                            color = Color.Black.copy(alpha = 0.5f),
                            fontSize = 16.sp,
                        )
                    }
                    innerTextField()
                }
            },
        )

        SendButton(
            enabled = sendEnabled,
            onClick = {
                onSend()
                keyboardController?.hide()
            },
        )
    }
}

@Composable
private fun SendButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.92f else 1f,
        label = "send-scale",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.45f)
            .scale(scale)
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            )
            .sendButtonBackground()
            .padding(horizontal = 22.dp, vertical = 18.dp),
    ) {
        Text(
            text = "SEND",
            color = Color.White,
            fontFamily = OptimaNova,
            fontSize = 18.sp,
        )
    }
}

private fun headerStatus(
    conversation: ConversationState,
    season: Season,
): String {
    val seasonLabel = when (season) {
        Season.NONE -> "clean feed"
        Season.SPRING -> "spring flair"
        Season.WINTER -> "winter flair"
    }

    val typingLabel = conversation.typingSender?.let { "${it.displayName} is typing" }
    return typingLabel ?: "${conversation.subtitle} | $seasonLabel"
}

private fun Modifier.personaPanelBackground(
    accentColor: Color,
    fillColor: Color,
): Modifier = drawBehind {
    val outer = GenericShape { size, _ ->
        moveTo(0f, 12.dp.toPx())
        lineTo(size.width - 24.dp.toPx(), 0f)
        lineTo(size.width, size.height - 14.dp.toPx())
        lineTo(22.dp.toPx(), size.height)
        close()
    }

    val inner = GenericShape { size, _ ->
        moveTo(10.dp.toPx(), 18.dp.toPx())
        lineTo(size.width - 28.dp.toPx(), 8.dp.toPx())
        lineTo(size.width - 10.dp.toPx(), size.height - 18.dp.toPx())
        lineTo(28.dp.toPx(), size.height - 8.dp.toPx())
        close()
    }

    val accent = GenericShape { size, _ ->
        moveTo(12.dp.toPx(), 24.dp.toPx())
        lineTo(44.dp.toPx(), 18.dp.toPx())
        lineTo(38.dp.toPx(), size.height - 28.dp.toPx())
        lineTo(6.dp.toPx(), size.height - 20.dp.toPx())
        close()
    }

    drawOutline(asOutline(outer), Color.Black)
    drawOutline(asOutline(inner), fillColor)
    drawOutline(asOutline(accent), accentColor)
}

private fun Modifier.personaBadgeBackground(
    color: Color,
): Modifier = drawBehind {
    val outer = GenericShape { size, _ ->
        moveTo(0f, 4.dp.toPx())
        lineTo(size.width - 10.dp.toPx(), 0f)
        lineTo(size.width, size.height - 4.dp.toPx())
        lineTo(10.dp.toPx(), size.height)
        close()
    }

    val inner = GenericShape { size, _ ->
        moveTo(4.dp.toPx(), 7.dp.toPx())
        lineTo(size.width - 12.dp.toPx(), 4.dp.toPx())
        lineTo(size.width - 4.dp.toPx(), size.height - 6.dp.toPx())
        lineTo(12.dp.toPx(), size.height - 4.dp.toPx())
        close()
    }

    drawOutline(asOutline(outer), Color.Black)
    drawOutline(asOutline(inner), color)
}

private fun Modifier.composerBackground(): Modifier = drawBehind {
    val outer = GenericShape { size, _ ->
        moveTo(0f, 18.dp.toPx())
        lineTo(size.width - 30.dp.toPx(), 0f)
        lineTo(size.width, size.height - 12.dp.toPx())
        lineTo(22.dp.toPx(), size.height)
        close()
    }

    val inner = GenericShape { size, _ ->
        moveTo(12.dp.toPx(), 22.dp.toPx())
        lineTo(size.width - 34.dp.toPx(), 8.dp.toPx())
        lineTo(size.width - 12.dp.toPx(), size.height - 16.dp.toPx())
        lineTo(30.dp.toPx(), size.height - 6.dp.toPx())
        close()
    }

    drawOutline(asOutline(outer), Color.Black)
    drawOutline(asOutline(inner), Color.White)
}

private fun Modifier.sendButtonBackground(): Modifier = drawBehind {
    val outer = GenericShape { size, _ ->
        moveTo(0f, 8.dp.toPx())
        lineTo(size.width - 16.dp.toPx(), 0f)
        lineTo(size.width, size.height - 10.dp.toPx())
        lineTo(18.dp.toPx(), size.height)
        close()
    }

    val inner = GenericShape { size, _ ->
        moveTo(8.dp.toPx(), 12.dp.toPx())
        lineTo(size.width - 18.dp.toPx(), 6.dp.toPx())
        lineTo(size.width - 8.dp.toPx(), size.height - 12.dp.toPx())
        lineTo(22.dp.toPx(), size.height - 4.dp.toPx())
        close()
    }

    drawOutline(asOutline(outer), Color.White)
    drawOutline(asOutline(inner), Color.Black)
}

@Composable
private fun RootContainer(content: @Composable () -> Unit) {
    val view = LocalView.current
    val window = (view.context as Activity).window
    SideEffect {
        window.statusBarColor = Color.Black.copy(alpha = 0.3f).toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()
    }
    content()
}
