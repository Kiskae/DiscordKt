package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.ws.data.inbound.ReadyEventModel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Guild

class GuildNode(val root: DiscordNode, override val id: DiscordId<Guild>) : Guild, EventProcessor {
    override fun acceptEvent(event: Any) {
        throw UnsupportedOperationException()
    }

    companion object {
        fun from(data: ReadyEventModel.ExtendedGuild, root: DiscordNode): GuildNode {
            return GuildNode(root, data.id)
        }
    }
}