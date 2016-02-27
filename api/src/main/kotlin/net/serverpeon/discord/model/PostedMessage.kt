package net.serverpeon.discord.model

import net.serverpeon.discord.interaction.Deletable
import net.serverpeon.discord.interaction.Editable
import net.serverpeon.discord.message.Message
import java.time.ZonedDateTime

interface PostedMessage : DiscordId.Identifiable<PostedMessage>, Editable<PostedMessage, PostedMessage.Edit>, Deletable {
    val lastEdited: ZonedDateTime?

    val postedAt: ZonedDateTime

    val textToSpeech: Boolean

    val poster: User

    val rawContent: String

    val content: Message

    interface Edit : Editable.Transaction<Edit, PostedMessage> {
        var content: Message
    }
}