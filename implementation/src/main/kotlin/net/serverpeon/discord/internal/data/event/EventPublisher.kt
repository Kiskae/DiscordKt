package net.serverpeon.discord.internal.data.event

import com.google.common.cache.CacheBuilder
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.eventbus.EventBus
import net.serverpeon.discord.event.Event
import net.serverpeon.discord.internal.data.model.DiscordNode
import net.serverpeon.discord.internal.data.model.MessageNode
import net.serverpeon.discord.internal.jsonmodels.MessageModel
import net.serverpeon.discord.internal.ws.client.EventWrapper
import net.serverpeon.discord.internal.ws.data.inbound.Channels
import net.serverpeon.discord.internal.ws.data.inbound.Guilds
import net.serverpeon.discord.internal.ws.data.inbound.Messages
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.PostedMessage
import rx.Observable
import rx.functions.Action2
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

internal class EventPublisher(val eventBus: EventBus) : Action2<EventWrapper, DiscordNode> {
    private val scheduler = Schedulers.computation()

    private val transformerMap = buildTransformers {
        val messageCache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.SECONDS) // Allow for 5 second delay for embed message
                .build<DiscordId<PostedMessage>, PostedMessage>()

        fun createMessageAndCache(msg: MessageModel, model: DiscordNode): MessageNode {
            return MessageNode.create(msg, model).apply {
                messageCache.put(id, this) // Cache the message for potential Embed updates
            }
        }

        // Channels
        mapSimple<Channels.Create.Public> { event, model ->
            ChannelCreatePublic(model.channelMap[event.channel.id]!! as Channel.Public)
        }
        mapSimple<Channels.Create.Private> { event, model ->
            ChannelCreatePrivate(model.channelMap[event.channel.id]!! as Channel.Private)
        }
        mapSimple<Channels.Update> { event, model ->
            ChannelUpdate(model.channelMap[event.channel.id]!! as Channel.Public)
        }
        mapSimple<Channels.Delete.Public> { event, model ->
            ChannelDeletePublic(event.value!!)
        }
        mapSimple<Channels.Delete.Private> { event, model ->
            ChannelDeletePrivate(event.value!!)
        }

        // Bans
        mapSimple<Guilds.Bans.Add> { event, model ->
            BanAdded(model.guildMap[event.guild_id]!!, model.userCache.retrieve(event.user))
        }
        mapSimple<Guilds.Bans.Remove> { event, model ->
            BanRemoved(model.guildMap[event.guild_id]!!, model.userCache.retrieve(event.user))
        }

        // Guilds
        mapSimple<Guilds.General.Create> { event, model ->
            GuildCreate(model.guildMap[event.guild.id]!!)
        }
        mapSimple<Guilds.General.Delete> { event, model ->
            GuildDelete(event.value!!)
        }
        mapSimple<Guilds.General.Update> { event, model ->
            GuildUpdate(model.guildMap[event.guild.id]!!)
        }

        // Members
        mapSimple<Guilds.Members.Add> { event, model ->
            MemberJoined(model.guildMap[event.member.guild_id]!!.memberMap[event.member.user.id]!!)
        }
        mapSimple<Guilds.Members.Update> { event, model ->
            MemberUpdate(model.guildMap[event.member.guild_id]!!.memberMap[event.member.user.id]!!)
        }
        map<Guilds.Members.Remove> { event, model ->
            // Member might not exist in model :|
            event.value?.let {
                Observable.just(MemberLeft(it))
            } ?: Observable.empty()
        }

        // Messages
        mapSimple<Messages.Create> { event, model ->
            MessageCreated(createMessageAndCache(event.msg, model))
        }
        map<Messages.Update> { event, model ->
            if (event.msg.content != null) {
                // Regular update, user-triggered
                Observable.just(MessageEdited(createMessageAndCache(event.msg.toMessageModel(), model)))
            } else {
                // Otherwise automated embed update
                // -> Retrieve the message from the cache
                messageCache.getIfPresent(event.msg.id)?.let {
                    Observable.just(MessageEmbed(it, ImmutableList.of(event.msg.embeds)))
                } ?: Observable.empty()
            }
        }
        mapSimple<Messages.Delete> { event, model ->
            MessageDeleted(model.channelMap[event.channel_id]!! as Channel.Text, event.id)
        }

        // Roles
        mapSimple<Guilds.Roles.Create> { event, model ->
            val guild = model.guildMap[event.guild_id]!!
            RoleAdded(guild.roleMap[event.role.id]!!, guild)
        }
        mapSimple<Guilds.Roles.Update> { event, model ->
            val guild = model.guildMap[event.guild_id]!!
            RoleEdited(guild.roleMap[event.role.id]!!, guild)
        }
        mapSimple<Guilds.Roles.Delete> { event, model ->
            val guild = model.guildMap[event.guild_id]!!
            RoleDeleted(event.value!!, guild)
        }
    }

    override fun call(eventHolder: EventWrapper, model: DiscordNode) {
        transformerMap[eventHolder.event.javaClass]?.let {
            processEvent(it(eventHolder, model))
        }
    }

    private fun processEvent(eventData: Observable<*>) {
        eventData.subscribeOn(scheduler).subscribe { eventBus.post(it) }
    }

    internal class DSL {
        internal val builder = ImmutableMap.builder<Class<*>, (EventWrapper, DiscordNode) -> Observable<out Event>>()

        inline fun <reified T : Any> map(noinline creator: (T, DiscordNode) -> Observable<out Event>) {
            builder.put(T::class.java, { wrapper, model ->
                creator(wrapper.event as T, model)
            })
        }

        inline fun <reified T : Any> mapSimple(noinline creator: (T, DiscordNode) -> Event) {
            map<T> { event, model ->
                Observable.just(creator(event, model))
            }
        }
    }

    companion object {
        private fun buildTransformers(
                init: DSL.() -> Unit
        ): Map<Class<*>, (EventWrapper, DiscordNode) -> Observable<out Event>> {
            val dsl = DSL()
            dsl.init()
            return dsl.builder.build()
        }
    }
}