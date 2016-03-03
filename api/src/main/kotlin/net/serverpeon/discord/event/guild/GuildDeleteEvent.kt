package net.serverpeon.discord.event.guild

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Guild

/**
 *
 */
interface GuildDeleteEvent : Event {
    val deletedGuild: Guild
}