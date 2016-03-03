package net.serverpeon.discord.event.member

import net.serverpeon.discord.event.Event
import net.serverpeon.discord.model.Guild

/**
 *
 */
interface MemberLeftEvent : Event {
    val formerMember: Guild.Member

    val guild: Guild
        get() = formerMember.guild
}