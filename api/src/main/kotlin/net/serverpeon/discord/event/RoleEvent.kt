package net.serverpeon.discord.event

import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.Role

interface RoleEvent {
    val guild: Guild
    val roleId: DiscordId<Role>

    interface New : RoleEvent {
        val role: Role
    }

    interface Changed : RoleEvent {
        val role: Role
    }

    interface Deleted : RoleEvent
}