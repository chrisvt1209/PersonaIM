package features.conversations

import kotlinx.serialization.Serializable

object ParticipantStatus {
    const val ACCEPTED = "ACCEPTED"
    const val PENDING = "PENDING"
}

@Serializable
data class Participant(
    val userId: Long,
    val username: String,
    val status: String
)

@Serializable
data class Conversation(
    val id: Long,
    val title: String?,
    val participants: List<Participant>
)
