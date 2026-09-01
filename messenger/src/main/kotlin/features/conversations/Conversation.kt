package features.conversations

import kotlinx.serialization.Serializable

object ParticipantStatus {
    const val ACCEPTED = "ACCEPTED"
    const val PENDING = "PENDING"
}

object ConversationType {
    const val SINGLE = "SINGLE"
    const val GROUP = "GROUP"
}

object ParticipantRole {
    const val MEMBER = "MEMBER"
    const val INVITER = "INVITER"
    const val MAINTAINER = "MAINTAINER"

    val ALL = setOf(MEMBER, INVITER, MAINTAINER)
}

@Serializable
data class Participant(
    val userId: Long,
    val username: String,
    val status: String,
    val role: String
)

@Serializable
data class Conversation(
    val id: Long,
    val title: String?,
    val type: String,
    val participants: List<Participant>
)
