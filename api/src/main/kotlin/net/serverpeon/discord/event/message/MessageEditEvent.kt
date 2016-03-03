package net.serverpeon.discord.event.message

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.PostedMessage

/**
 *
 */
interface MessageEditEvent : Event {
    val message: PostedMessage

    val channel: Channel.Text
        get() = message.channel
}