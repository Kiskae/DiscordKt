package net.serverpeon.discord.event.channel

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.User

/**
 * Fired when a channel is deleted.
 *
 * Use the more specific [Public] for the deletion of channels in servers and [Private] for the deletion of Direct
 * Messaging channels.
 */
interface ChannelDeleteEvent : Event {
    val deletedChannel: Channel

    interface Public : ChannelDeleteEvent {
        override val deletedChannel: Channel.Public

        val guild: Guild
            get() = deletedChannel.guild
    }

    interface Private : ChannelDeleteEvent {
        override val deletedChannel: Channel.Private

        val recipient: User
            get() = deletedChannel.recipient
    }
}