package net.serverpeon.discord.event.message

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.PostedMessage

/**
 * Fired when a message contains an embeddable element.
 *
 * Since parsing these elements takes a small amount of time the result is sent through a delayed event.
 */
interface MessageEmbedEvent : Event {
    val message: PostedMessage

    val embeds: List<Any>

    val channel: Channel.Text
        get() = message.channel
}