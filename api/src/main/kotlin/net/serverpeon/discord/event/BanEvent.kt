package net.serverpeon.discord.event

import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.User

interface BanEvent {
    val guild: Guild
    val user: User

    interface New : BanEvent
    interface Removed : BanEvent
}