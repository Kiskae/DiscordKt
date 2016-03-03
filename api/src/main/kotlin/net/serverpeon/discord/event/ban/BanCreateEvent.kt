package net.serverpeon.discord.event.ban

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.User

/**
 *
 */
interface BanCreateEvent : Event {
    val guild: Guild
    val user: User
}