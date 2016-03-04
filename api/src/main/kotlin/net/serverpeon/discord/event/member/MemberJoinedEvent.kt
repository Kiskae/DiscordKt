package net.serverpeon.discord.event.member

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Guild

/**
 * Fired when a new user joins a server.
 */
interface MemberJoinedEvent : Event {
    val member: Guild.Member

    val guild: Guild
        get() = member.guild
}