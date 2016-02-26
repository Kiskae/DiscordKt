package net.serverpeon.discord.event

import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.Role

interface RoleEvent {
    val guild: Guild
    val role: Role

    interface New : RoleEvent

    interface Changed : RoleEvent

    interface Deleted : RoleEvent
}