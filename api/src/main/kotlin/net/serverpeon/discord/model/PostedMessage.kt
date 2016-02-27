package net.serverpeon.discord.model

import net.serverpeon.discord.interaction.Deletable
import net.serverpeon.discord.interaction.Editable
import net.serverpeon.discord.message.Message
import java.time.ZonedDateTime

/**
 * Represents a message that has been published to a [Channel.Text].
 */
interface PostedMessage : DiscordId.Identifiable<PostedMessage>, Editable<PostedMessage, PostedMessage.Edit>, Deletable {
    /**
     * If this message was edited at some point this will indicate the last time it was edited.
     */
    val lastEdited: ZonedDateTime?

    /**
     * The time at which this message was posted to the channel.
     */
    val postedAt: ZonedDateTime

    /**
     * Whether this message enabled Text-To-Speech
     */
    val textToSpeech: Boolean

    /**
     * The user which created this message.
     */
    val author: User

    /**
     * The message was posted to this channel.
     */
    val channel: Channel.Text

    /**
     * The raw string as sent by discord, contains all the unescaped data.
     */
    val rawContent: String

    /**
     * The message parsed from [rawContent], also contains a list of people explicitly mentioned in this message.
     */
    val content: Message

    interface Edit : Editable.Transaction<Edit, PostedMessage> {
        var content: Message
    }
}