package net.serverpeon.discord.event.channel

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Channel

/**
 *
 */
interface ChannelUpdateEvent : Event {
    val channel: Channel.Public
}