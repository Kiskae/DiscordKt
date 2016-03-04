package net.serverpeon.discord.event.ban

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.User

/**
 * Fired when a user is banned from a server that the client is a member of.
 */
interface BanCreateEvent : Event {
    val guild: Guild
    val user: User
}