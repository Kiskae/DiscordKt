package net.serverpeon.discord.event.message

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.PostedMessage
import net.serverpeon.discord.model.User

/**
 * Fired when a message is edited by the user that posted it.
 *
 * If the message contains embeddable links then a [MessageEmbedEvent] will follow this event.
 */
interface MessageEditEvent : Event {
    val message: PostedMessage

    val author: User
        get() = message.author

    val channel: Channel.Text
        get() = message.channel
}