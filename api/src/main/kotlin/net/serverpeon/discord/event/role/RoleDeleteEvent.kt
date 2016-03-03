package net.serverpeon.discord.event.role

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.Role

/**
 *
 */
interface RoleDeleteEvent : Event {
    val deletedRole: Role

    val guild: Guild
}