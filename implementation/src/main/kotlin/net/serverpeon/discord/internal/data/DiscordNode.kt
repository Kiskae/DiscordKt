package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.rest.retro.ApiWrapper
import net.serverpeon.discord.internal.ws.data.inbound.*
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Guild
import kotlin.properties.Delegates

class DiscordNode(val api: ApiWrapper) : Event.Visitor {
    val userCache = UserCache()
    var guilds = createEmptyMap<Guild, GuildNode>()
    var channels = createEmptyMap<Channel, ChannelNode>()
    var self: WhoamiNode by Delegates.notNull()

    override fun guildCreate(e: Guilds.General.Create) {
        check(e.guild.id !in guilds) { "Guild created twice? $e" }

        val guild = GuildNode.from(e.guild, this)
        guilds = guilds.immutableAdd(guild.id, guild)
    }

    override fun channelCreate(e: Channels.Create.Public) {
        check(e.channel.id !in channels) { "Channel created twice? $e" }

        super.channelCreate(e)

        guilds[e.channel.guild_id]?.let { channels[e.channel.id] }?.let { channel ->
            channels = channels.immutableAdd(channel.id, channel)
        } ?: IllegalStateException("Created channel but not? $e")
    }

    override fun channelCreate(e: Channels.Create.Private) {
        //FIXME: this event gets send twice
        //check(e.channel.id !in channels)
        if (e.channel.id in channels) return

        val channel = ChannelNode.from(e.channel, this)
        channels = channels.immutableAdd(channel.id, channel)
    }

    override fun channelDelete(e: Channels.Delete.Public) {
        super.channelDelete(e)
        channels = channels.immutableRemove(e.channel.id)
    }

    override fun channelDelete(e: Channels.Delete.Private) {
        channels = channels.immutableRemove(e.channel.id)
    }

    override fun guildDelete(e: Guilds.General.Delete) {
        val guild = guilds[e.guild.id]!!
        channels = channels.immutableRemoveKeys(guild.channels.keys)
        guilds = guilds.immutableRemove(guild.id)
    }

    override fun userUpdate(e: Misc.UserUpdate) {
        // Forward to self
        self.visit(e)
    }

    override fun ready(e: Misc.Ready) {
        // Block this event here
    }

    override fun wireToGuild(id: DiscordId<Guild>, e: Event) {
        guilds[id]!!.visit(e)
    }

    override fun wireToChannel(id: DiscordId<Channel>, e: Event) {
        channels[id]!!.visit(e)
    }

    override fun toString(): String {
        return "Root(guilds=${guilds.values}, privateChannels=${channels.values.filter { it.isPrivate }})"
    }

    companion object {
        fun from(data: ReadyEventModel, api: ApiWrapper): DiscordNode {
            val primaryNode = DiscordNode(api)

            //SELF
            val whoami = primaryNode.userCache.retrieve(
                    data.user.id,
                    WhoamiNode.from(data.user, primaryNode)
            ) as WhoamiNode
            primaryNode.self = whoami

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