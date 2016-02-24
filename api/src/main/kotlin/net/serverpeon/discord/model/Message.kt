package net.serverpeon.discord.model

import java.time.ZonedDateTime

interface Message : DiscordId.Identifiable<Message> {
    val lastEdited: ZonedDateTime?

    val postedAt: ZonedDateTime

    val textToSpeech: Boolean
}