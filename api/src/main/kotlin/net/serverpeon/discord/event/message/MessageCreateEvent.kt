package net.serverpeon.discord.event.message

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.PostedMessage
import net.serverpeon.discord.model.User

/**
 * Fired when a new [message] is posted to [channel].
 *
 * If the message contains embeddable links then a [MessageEmbedEvent] will follow this event.
 */
interface MessageCreateEvent : Event {
    val message: PostedMessage

    val author: User
        get() = message.author

    val channel: Channel.Text
        get() = message.channel
}