package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.rest.retro.ApiWrapper
import net.serverpeon.discord.internal.ws.data.inbound.ReadyEventModel
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.Guild

class DiscordNode(val api: ApiWrapper) : EventProcessor {
    val userCache = UserCache()
    private val internalGuilds = createIdMapRelay<Guild, GuildNode>()
    val guilds = internalGuilds.first()
    private val internalPrivateChannels = createIdMapRelay<Channel, ChannelNode>()
    val privateChannels = internalGuilds.first()

    override fun acceptEvent(event: Any) {
        //TODO: distribute events to relevant locations
        println(event)
    }

    companion object {
        fun from(data: ReadyEventModel, api: ApiWrapper): DiscordNode {
            val primaryNode = DiscordNode(api)

            //GUILDS
            val guilds = data.guilds.map { GuildNode.from(it, primaryNode) }
            primaryNode.internalGuilds.call(guilds.toImmutableIdMap())

            //SELF
            val whoami = WhoamiNode.from(data.user, primaryNode)
            //TODO: how does this need to be updated....

            //PRIVATE_CHANNELS
            val privateChannels = data.private_channels.map { ChannelNode.from(it, primaryNode) }
            primaryNode.internalPrivateChannels.call(privateChannels.toImmutableIdMap())

            //TODO: SETTINGS?

            return primaryNode
        }
    }
}