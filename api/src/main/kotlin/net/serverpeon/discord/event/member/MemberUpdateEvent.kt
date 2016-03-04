package net.serverpeon.discord.event.member

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Guild

/**
 * Fired when a user changes their properties (such as their username)
 */
interface MemberUpdateEvent : Event {
    val member: Guild.Member

    val guild: Guild
        get() = member.guild
}