package net.serverpeon.discord.event

import net.serverpeon.discord.model.Guild

interface GuildEvent {
    val guild: Guild

    interface New : GuildEvent
    interface Changed : GuildEvent
    interface Deleted : GuildEvent
}