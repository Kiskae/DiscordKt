package net.serverpeon.discord.internal.data.model

import net.serverpeon.discord.internal.createLogger
import net.serverpeon.discord.internal.data.*
import net.serverpeon.discord.internal.jsonmodels.ReadyEventModel
import net.serverpeon.discord.internal.kDebug
import net.serverpeon.discord.internal.rest.retro.ApiWrapper
import net.serverpeon.discord.internal.rest.retro.Guilds.CreateGuildRequest
import net.serverpeon.discord.internal.rxObservable
import net.serverpeon.discord.internal.toFuture
import net.serverpeon.discord.internal.ws.data.inbound.Channels
import net.serverpeon.discord.internal.ws.data.inbound.Event
import net.serverpeon.discord.internal.ws.data.inbound.Guilds
import net.serverpeon.discord.internal.ws.data.inbound.Misc
import net.serverpeon.discord.model.*
import rx.Observable
import java.util.concurrent.CompletableFuture
import kotlin.properties.Delegates

class DiscordNode(val api: ApiWrapper) : EventInput<DiscordNode>, ClientModel {
    internal val userCache = UserCache(this)
    internal var guildMap = createEmptyMap<Guild, GuildNode>()
    internal var channelMap = createEmptyMap<Channel, ChannelNode<*>>()
    internal var self: SelfNode by Delegates.notNull()

    val guilds: Observable<Guild>
        get() = Observable.defer {
            Observable.from<Guild>(guildMap.values)
        }

    override fun getGuildById(id: DiscordId<Guild>): Observable<Guild> {
        return observableLookup(id) { guildMap[it] }
    }

    override fun getUserById(id: DiscordId<User>): Observable<User> {
        return observableLookup(id) {
            userCache.retrieve(it)
        }
    }

    override fun getChannelById(id: DiscordId<Channel>): Observable<Channel> {
        return observableLookup(id) {
            channelMap[it]
        }
    }

    val privateChannels: Observable<Channel.Private>
        get() = Observable.defer {
            Observable.from(channelMap.values)
        }.filter {
            it.isPrivate
        }.map { it as Channel.Private }

    override fun createGuild(name: String, region: Region): CompletableFuture<Guild> {
        return api.Guilds.createGuild(CreateGuildRequest(
                name = name,
                region = region
        )).toFuture().thenApply {
            Builder.guild(it, this)
        }
    }

    override fun guilds(): Observable<Guild> {
        return guilds
    }

    override fun privateChannels(): Observable<Channel.Private> {
        return privateChannels
    }

    override fun getPrivateChannelById(id: DiscordId<Channel>): Observable<Channel.Private> {
        return getChannelById(id).filter {
            it.isPrivate
        }.map {
            it as Channel.Private
        }
    }

    override fun getPrivateChannelByUser(userId: DiscordId<User>): Observable<Channel.Private> {
        return privateChannels().first {
            it.recipient.id == userId
        }
    }

    override fun getAvailableServerRegions(): Observable<Region> {
        return api.Voice.serverRegions()
                .rxObservable()
                .flatMapIterable {
                    it
                }.map { RegionNode.create(it) }
    }

    override fun handler(): EventInput.Handler<DiscordNode> {
        return Handler
    }

    private object Handler : EventInput.Handler<DiscordNode> {
        override fun channelCreate(target: DiscordNode, e: Channels.Create.Public) {
            check(e.channel.id !in target.channelMap) { "Channel created twice? $e" }

            super.channelCreate(target, e)

            logger.kDebug { "Channel created: ${e.channel.id.repr}#${e.channel.name}" }

            target.guildMap[e.channel.guild_id]?.let {
                target.channelMap[e.channel.id]
            }?.let { channel ->
                target.channelMap += channel
            } ?: IllegalStateException("Created channel but not? $e")
        }

        override fun channelCreate(target: DiscordNode, e: Channels.Create.Private) {
            //FIXME: this event gets send twice
            //check(e.channel.id !in channels)
            if (e.channel.id in target.channelMap) return

            logger.kDebug { "Channel created: ${e.channel.id.repr}#${e.channel.recipient.username}-${e.channel.recipient.discriminator}" }

            target.channelMap += Builder.channel(e.channel, target)
        }

        override fun guildCreate(target: DiscordNode, e: Guilds.General.Create) {
            check(e.guild.id !in target.guildMap) { "Guild created twice? $e" }

            logger.kDebug { "Guild created: ${e.guild.id.repr}#${e.guild.name}" }

            Builder.guild(e.guild, target).apply {
                target.guildMap += this
                target.channelMap = combineMaps(target.channelMap, this.channelMap)
            }
        }

        override fun guildDelete(target: DiscordNode, e: Guilds.General.Delete) {
            val guild = target.guildMap[e.guild.id]!!

            e.value = guild

            // Clear references
            target.channelMap -= guild.channelMap.keys
            target.guildMap -= guild.id
        }

        override fun channelDelete(target: DiscordNode, e: Channels.Delete.Public) {
            super.channelDelete(target, e)
            target.channelMap -= e.channel.id
        }

        override fun channelDelete(target: DiscordNode, e: Channels.Delete.Private) {
            e.value = target.channelMap[e.channel.id] as? ChannelNode.Private
            target.channelMap -= e.channel.id
        }

        override fun userUpdate(target: DiscordNode, e: Misc.UserUpdate) {
            target.self.handle(e)
        }

        override fun ready(target: DiscordNode, e: Misc.Ready) {
            // Block this event here
        }
    }

    override fun wireToGuild(id: DiscordId<Guild>, event: Event) {
        guildMap[id]!!.handle(event)
    }

    override fun wireToChannel(id: DiscordId<Channel>, event: Event) {
        channelMap[id]!!.handle(event)
    }

    override fun toString(): String {
        return "Root(guilds=${guildMap.values}, privateChannels=${channelMap.values.filter { it.isPrivate }})"
    }

    companion object {
        private val logger = createLogger()

        fun build(data: ReadyEventModel, api: ApiWrapper) = Builder.root(data, api)
    }
}