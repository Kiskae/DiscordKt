package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.rest.retro.ApiWrapper
import net.serverpeon.discord.internal.ws.data.inbound.ReadyEventModel
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.Guild

class DiscordNode(val api: ApiWrapper) : EventProcessor {
    val userCache = UserCache()
    var guilds = createEmptyMap<Guild, GuildNode>()
    var privateChannels = createEmptyMap<Channel, ChannelNode>()

    override fun acceptEvent(event: Any) {
        //TODO: distribute events to relevant locations
        println(event)
    }

    companion object {
        fun from(data: ReadyEventModel, api: ApiWrapper): DiscordNode {
            val primaryNode = DiscordNode(api)

            //SELF
            val whoami = primaryNode.userCache.retrieve(
                    data.user.id,
                    WhoamiNode.from(data.user, primaryNode)
            ) as WhoamiNode

            //GUILDS
            val guilds = data.guilds.map { GuildNode.from(it, primaryNode) }
            primaryNode.guilds = guilds.toImmutableIdMap()

            //PRIVATE_CHANNELS
            val privateChannels = data.private_channels.map { ChannelNode.from(it, primaryNode) }
            primaryNode.privateChannels = privateChannels.toImmutableIdMap()

            //TODO: SETTINGS?

            return primaryNode
        }
    }
}