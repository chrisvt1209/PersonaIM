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
    val participantIds: List<Long> = emptyList(),
    val isGroup: Boolean = false
)

object ParticipantRole {
    const val MEMBER = "MEMBER"
    const val INVITER = "INVITER"
    const val MAINTAINER = "MAINTAINER"
}

data class Participant(
    val userId: Long,
    val username: String,
    val status: String,
    val role: String
)

data class ConversationDetail(
    val id: String,
    val title: String,
    val isGroup: Boolean,
    val participants: List<Participant>
)

data class GroupInvite(
    val id: String,
    val title: String,
    val memberCount: Int
)
