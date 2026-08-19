package features.conversations

import kotlinx.serialization.Serializable

@Serializable
data class Conversation(
    val id: Long,
    val participantIds: List<Long>
)