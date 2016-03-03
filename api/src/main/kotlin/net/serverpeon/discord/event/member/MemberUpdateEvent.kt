package net.serverpeon.discord.event.member

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Guild

/**
 *
 */
interface MemberUpdateEvent : Event {
    val member: Guild.Member

    val guild: Guild
        get() = member.guild
}