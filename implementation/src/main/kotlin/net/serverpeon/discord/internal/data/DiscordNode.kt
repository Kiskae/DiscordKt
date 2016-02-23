package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.rest.retro.ApiWrapper
import net.serverpeon.discord.internal.ws.data.inbound.Channels
import net.serverpeon.discord.internal.ws.data.inbound.Guilds
import net.serverpeon.discord.internal.ws.data.inbound.Misc
import net.serverpeon.discord.internal.ws.data.inbound.ReadyEventModel
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.Guild

class DiscordNode(val api: ApiWrapper) : EventProcessor {
    val userCache = UserCache()
    var guilds = createEmptyMap<Guild, GuildNode>()
    var channels = createEmptyMap<Channel, ChannelNode>()

    override fun acceptEvent(event: Any) {
        when (event) {
            is Guilds.General.Create -> {
                val guild = GuildNode.from(event.guild, this)
                guilds = guilds.immutableAdd(guild.id, guild)
            }
            is Guilds -> {
                guilds[event.id]!!.acceptEvent(event)
            }
            is Misc.PresenceUpdate -> {
                guilds[event.guild_id]!!.acceptEvent(event)
            }
            is Channels.Create -> {
                val newNode = if (event is Channels.Create.Public) {
                    guilds[event.channel.guild_id]?.let {
                        it.acceptEvent(event)
                        it.channels[event.channel.id]!!
                    }
                } else {
                    ChannelNode.from((event as Channels.Create.Private).channel, this)
                }
                channels = channels.immutableAdd(newNode.id, newNode)
            }
            is Channels.Update -> {
                channels[event.channel.id]!!.acceptEvent(event)
            }
            is Channels.Delete -> {
                val id = if (event is Channels.Delete.Public) {
                    guilds[event.channel.guild_id]?.acceptEvent(event)
                    event.channel.id
                } else {
                    (event as Channels.Delete.Private).channel.id
                }
                channels = channels.immutableRemove(id)
            }
        }
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

            //PRIVATE_CHANNELS
            val privateChannels = data.private_channels.map { ChannelNode.from(it, primaryNode) }

            primaryNode.guilds = guilds.toImmutableIdMap()
            primaryNode.channels = combineMaps(
                    guilds.flatMap { it.channels.values }.toImmutableIdMap(), // Create a single addressable map of guilds
                    privateChannels.toImmutableIdMap()
            )

            //TODO: SETTINGS?

            return primaryNode
        }
    }
}