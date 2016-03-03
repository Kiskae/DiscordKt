package net.serverpeon.discord.event.channel

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Channel

/**
 *
 */
interface ChannelDeleteEvent : Event {
    val deletedChannel: Channel

    interface Public : ChannelDeleteEvent {
        override val deletedChannel: Channel.Public
    }

    interface Private : ChannelDeleteEvent {
        override val deletedChannel: Channel.Private
    }
}