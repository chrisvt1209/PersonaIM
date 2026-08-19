package features.messages

import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequest(
    val text: String
)