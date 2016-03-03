package net.serverpeon.discord.event.role

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.Role

/**
 *
 */
interface RoleUpdateEvent : Event {
    val role: Role

    val guild: Guild
}