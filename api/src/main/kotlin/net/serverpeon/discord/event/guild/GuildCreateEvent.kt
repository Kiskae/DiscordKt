package net.serverpeon.discord.event.guild

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.ClientModel

/**
 * Fired when the client joins a new server. This can happen either by accepting an invite and by creating a guild using
 * [ClientModel.createGuild]
 */
interface GuildCreateEvent : Event {
    val guild: Guild
}