package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.rest.retro.ApiWrapper
import net.serverpeon.discord.internal.ws.data.inbound.*
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.User
import rx.Observable
import kotlin.properties.Delegates

class DiscordNode(val api: ApiWrapper) : Event.Visitor {
    internal val userCache = UserCache()
    internal var guildMap = createEmptyMap<Guild, GuildNode>()
    internal var channelMap = createEmptyMap<Channel, ChannelNode>()
    internal var self: WhoamiNode by Delegates.notNull()

    val guilds: Observable<Guild>
        get() = observableList { guildMap.values }

    fun getGuildById(id: DiscordId<Guild>): Observable<Guild> {
        return observableLookup(id) { guildMap[it] }
    }

    fun getUserById(id: DiscordId<User>): Observable<User> {
        return observableLookup(id) {
            userCache.retrieve(it)
        }
    }

    fun getChannelById(id: DiscordId<Channel>): Observable<Channel> {
        return observableLookup(id) {
            channelMap[it]
        }
    }

    val privateChannels: Observable<Channel>
        get() = observableList { channelMap.values }.filter { it.isPrivate } //TODO: cast

    override fun guildCreate(e: Guilds.General.Create) {
        check(e.guild.id !in guildMap) { "Guild created twice? $e" }

        val guild = GuildNode.from(e.guild, this)
        guildMap = guildMap.immutableAdd(guild.id, guild)
    }

    override fun channelCreate(e: Channels.Create.Public) {
        check(e.channel.id !in channelMap) { "Channel created twice? $e" }

        super.channelCreate(e)

        guildMap[e.channel.guild_id]?.let { channelMap[e.channel.id] }?.let { channel ->
            channelMap = channelMap.immutableAdd(channel.id, channel)
        } ?: IllegalStateException("Created channel but not? $e")
    }

    override fun channelCreate(e: Channels.Create.Private) {
        //FIXME: this event gets send twice
        //check(e.channel.id !in channels)
        if (e.channel.id in channelMap) return

        val channel = ChannelNode.from(e.channel, this)
        channelMap = channelMap.immutableAdd(channel.id, channel)
    }

    override fun channelDelete(e: Channels.Delete.Public) {
        super.channelDelete(e)
        channelMap = channelMap.immutableRemove(e.channel.id)
    }

    override fun channelDelete(e: Channels.Delete.Private) {
        channelMap = channelMap.immutableRemove(e.channel.id)
    }

    override fun guildDelete(e: Guilds.General.Delete) {
        val guild = guildMap[e.guild.id]!!
        channelMap = channelMap.immutableRemoveKeys(guild.channelMap.keys)
        e.value = guildMap[guild.id]
        guildMap = guildMap.immutableRemove(guild.id)
    }

    override fun userUpdate(e: Misc.UserUpdate) {
        // Forward to self
        self.visit(e)
    }

    override fun ready(e: Misc.Ready) {
        // Block this event here
    }

    override fun wireToGuild(id: DiscordId<Guild>, e: Event) {
        guildMap[id]!!.visit(e)
    }

    override fun wireToChannel(id: DiscordId<Channel>, e: Event) {
        channelMap[id]!!.visit(e)
    }

    override fun toString(): String {
        return "Root(guilds=${guildMap.values}, privateChannels=${channelMap.values.filter { it.isPrivate }})"
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

            primaryNode.guildMap = guilds.toImmutableIdMap()
            primaryNode.channelMap = combineMaps(
                    guilds.flatMap { it.channelMap.values }.toImmutableIdMap(), // Create a single addressable map of guilds
                    privateChannels.toImmutableIdMap()
            )

            //TODO: SETTINGS?

            return primaryNode
        }
    }
}