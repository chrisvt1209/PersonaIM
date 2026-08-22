package dev.compose.messenger.core.common.model

import androidx.compose.ui.graphics.Color
import dev.compose.messenger.core.designsystem.theme.PersonaRed
import dev.compose.messenger.core.designsystem.theme.AnnColor
import dev.compose.messenger.core.designsystem.theme.YusukeColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

enum class Season {
    NONE, SPRING, WINTER
}

data class MessageTemplate(
    val sender: Sender,
    val text: String,
)

data class ConversationSeed(
    val id: String,
    val title: String,
    val subtitle: String,
    val participants: ImmutableList<Sender>,
    val accentColor: Color,
    val backgroundSeason: Season,
    val openingMessages: ImmutableList<MessageTemplate>,
    val autoReplies: ImmutableList<ImmutableList<MessageTemplate>>,
)

fun conversationSeeds(): ImmutableList<ConversationSeed> = persistentListOf(
    ConversationSeed(
        id = "phantom-thieves",
        title = "Phantom Thieves",
        subtitle = "Mementos planning thread",
        participants = persistentListOf(Sender.Ann, Sender.Ryuji, Sender.Yusuke),
        accentColor = PersonaRed,
        backgroundSeason = Season.SPRING,
        openingMessages = persistentListOf(
            MessageTemplate(
                sender = Sender.Ann,
                text = "We have to find them tomorrow for sure. This is the only lead we have right now.",
            ),
            MessageTemplate(
                sender = Sender.Yusuke,
                text = "If we tail him carefully, he may lead us straight back to his boss.",
            ),
            MessageTemplate(
                sender = Sender.Ryuji,
                text = "Then we hit Central Street after school and keep our eyes open.",
            ),
            MessageTemplate(
                sender = Sender.Ann,
                text = "And no one is rushing in alone this time, okay?",
            ),
        ),
        autoReplies = persistentListOf(
            persistentListOf(
                MessageTemplate(
                    sender = Sender.Ryuji,
                    text = "Fine, fine. I'll hang back until we know who's calling the shots.",
                ),
                MessageTemplate(
                    sender = Sender.Yusuke,
                    text = "A measured approach would be preferable. Even the most dramatic sting needs patience.",
                ),
            ),
            persistentListOf(
                MessageTemplate(
                    sender = Sender.Ann,
                    text = "I'll cover the station side. If anything feels off, message us right away.",
                ),
            ),
            persistentListOf(
                MessageTemplate(
                    sender = Sender.Yusuke,
                    text = "Should fortune favor us, we may secure enough intel for a clean change of heart.",
                ),
                MessageTemplate(
                    sender = Sender.Ryuji,
                    text = "And if fortune doesn't, we're still not backing down.",
                ),
            ),
        ),
    ),
    ConversationSeed(
        id = "after-school",
        title = "After School",
        subtitle = "Cafe and study plans",
        participants = persistentListOf(Sender.Ann, Sender.Ryuji),
        accentColor = AnnColor,
        backgroundSeason = Season.NONE,
        openingMessages = persistentListOf(
            MessageTemplate(
                sender = Sender.Ann,
                text = "I can be at Leblanc in twenty. Are we actually studying this time?",
            ),
            MessageTemplate(
                sender = Sender.Ryuji,
                text = "That depends. Does buying snacks count as studying?",
            ),
        ),
        autoReplies = persistentListOf(
            persistentListOf(
                MessageTemplate(
                    sender = Sender.Ann,
                    text = "Only if you're memorizing the menu again.",
                ),
            ),
            persistentListOf(
                MessageTemplate(
                    sender = Sender.Ryuji,
                    text = "Hey, strategy matters. A guy needs fuel before exams.",
                ),
            ),
            persistentListOf(
                MessageTemplate(
                    sender = Sender.Ann,
                    text = "Bring your notes and I'll allow one curry break.",
                ),
                MessageTemplate(
                    sender = Sender.Ryuji,
                    text = "Now that's a deal.",
                ),
            ),
        ),
    ),
    ConversationSeed(
        id = "gallery-intel",
        title = "Gallery Intel",
        subtitle = "Yusuke's private thread",
        participants = persistentListOf(Sender.Yusuke),
        accentColor = YusukeColor,
        backgroundSeason = Season.WINTER,
        openingMessages = persistentListOf(
            MessageTemplate(
                sender = Sender.Yusuke,
                text = "The owner finally moved the invoices. They are on the second floor beside the archive room.",
            ),
            MessageTemplate(
                sender = Sender.Yusuke,
                text = "Should you need a distraction, I can provide one with tasteful flair.",
            ),
        ),
        autoReplies = persistentListOf(
            persistentListOf(
                MessageTemplate(
                    sender = Sender.Yusuke,
                    text = "I shall remain nearby. Message me if the route changes and I will adapt.",
                ),
            ),
            persistentListOf(
                MessageTemplate(
                    sender = Sender.Yusuke,
                    text = "There is beauty in a perfect infiltration plan, even if it lacks a canvas.",
                ),
            ),
            persistentListOf(
                MessageTemplate(
                    sender = Sender.Yusuke,
                    text = "One more thing: the side entrance alarm resets after midnight. Timing will be critical.",
                ),
            ),
        ),
    ),
)
