package net.serverpeon.discord.event.channel

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Channel

/**
 *
 */
interface ChannelCreateEvent : Event {
    val channel: Channel

    interface Public : ChannelCreateEvent {
        override val channel: Channel.Public
    }

    interface Private : ChannelCreateEvent {
        override val channel: Channel.Private
    }
}