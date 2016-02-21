package net.serverpeon.discord.event

import net.serverpeon.discord.model.Guild

interface MemberEvent {
    val guild: Guild
    val user: Guild.Member

    interface Joined : MemberEvent
    interface Changed : MemberEvent
    interface Kicked : MemberEvent
}