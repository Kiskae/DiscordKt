package net.serverpeon.discord.event.guild

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Guild

/**
 * Fired when a property of a server is changed.
 */
interface GuildUpdateEvent : Event {
    val guild: Guild
}