package net.serverpeon.discord.event.role

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.Role

/**
 * Fired when a new role is created within a guild.
 *
 * In many cases this will quickly be followed by a [RoleUpdateEvent] to define the properties of the role.
 */
interface RoleCreateEvent : Event {
    val role: Role

    val guild: Guild
}