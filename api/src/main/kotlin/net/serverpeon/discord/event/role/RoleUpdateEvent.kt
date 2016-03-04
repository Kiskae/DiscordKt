package net.serverpeon.discord.event.role

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.Role

/**
 * Fired when [role] is updated, triggered by a user editing the properties of the role.
 */
interface RoleUpdateEvent : Event {
    val role: Role

    val guild: Guild
}