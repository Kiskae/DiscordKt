package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.ws.data.inbound.ReadyEventModel
import net.serverpeon.discord.model.*

class GuildNode(val root: DiscordNode, override val id: DiscordId<Guild>) : Guild, EventProcessor {
    var channels = createEmptyMap<Channel, ChannelNode>()
    var roles = createEmptyMap<Role, RoleNode>()
    var members = createEmptyMap<User, MemberNode>()

    override fun acceptEvent(event: Any) {
        throw UnsupportedOperationException()
    }

    companion object {
        fun from(data: ReadyEventModel.ExtendedGuild, root: DiscordNode): GuildNode {
            val guildNode = GuildNode(root, data.id)

            val channels = data.channels.map { ChannelNode.from(it, root) }
            guildNode.channels = channels.toImmutableIdMap()

            val roles = data.roles.map { RoleNode.from(it, root) }
            guildNode.roles = roles.toImmutableIdMap()

            val members = data.members.map { MemberNode.from(it, guildNode, root) }
            guildNode.members = members.toImmutableIdMap()

            return guildNode
        }
    }
}