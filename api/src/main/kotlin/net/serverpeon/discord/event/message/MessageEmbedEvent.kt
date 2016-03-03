package net.serverpeon.discord.event.message

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.PostedMessage

/**
 *
 */
interface MessageEmbedEvent : Event {
    val message: PostedMessage

    val embeds: List<Any>

    val channel: Channel.Text
        get() = message.channel
}