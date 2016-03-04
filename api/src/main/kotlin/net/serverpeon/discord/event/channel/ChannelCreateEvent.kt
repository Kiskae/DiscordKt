package net.serverpeon.discord.event.channel

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.User

/**
 * Fired when a new channel is created.
 *
 * Use the more specific [Public] for the creation of channels in servers and [Private] for the creation of Direct
 * Messaging channels.
 */
interface ChannelCreateEvent : Event {
    val channel: Channel

    interface Public : ChannelCreateEvent {
        override val channel: Channel.Public

        val guild: Guild
            get() = channel.guild
    }

    interface Private : ChannelCreateEvent {
        override val channel: Channel.Private

        val recipient: User
            get() = channel.recipient
    }
}