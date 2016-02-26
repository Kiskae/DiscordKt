package net.serverpeon.discord.model

import net.serverpeon.discord.message.Message
import java.time.ZonedDateTime

interface PostedMessage : DiscordId.Identifiable<PostedMessage> {
    val lastEdited: ZonedDateTime?

    val postedAt: ZonedDateTime

    val textToSpeech: Boolean

    val rawContent: String

    val content: Message
}