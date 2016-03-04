package net.serverpeon.discord.event.channel

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.Guild

/**
 * Fired when the settings of a public channel are altered.
 *
 * This can include the name and the topic of the channel.
 */
interface ChannelUpdateEvent : Event {
    val channel: Channel.Public

    val guild: Guild
        get() = channel.guild
}