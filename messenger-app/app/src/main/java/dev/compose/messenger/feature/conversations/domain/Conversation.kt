package dev.compose.messenger.feature.conversations.domain

import androidx.compose.ui.graphics.Color

data class Conversation(
    val id: String,
    val title: String,
    val subtitle: String,
    val participantNames: String,
    val lastMessage: String,
    val unreadCount: Int,
    val accentColor: Color,
    val avatarUrls: List<Int> = emptyList() // Using resource IDs for now
)
