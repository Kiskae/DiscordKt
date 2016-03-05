package net.serverpeon.discord.internal.data.model

import com.google.common.collect.ImmutableMap
import net.serverpeon.discord.interaction.Editable
import net.serverpeon.discord.internal.data.*
import net.serverpeon.discord.internal.jsonmodels.ChannelModel
import net.serverpeon.discord.internal.rest.WrappedId
import net.serverpeon.discord.internal.rest.retro.Guilds.ChannelCreationRequest
import net.serverpeon.discord.internal.rest.retro.Guilds.EditGuildRequest
import net.serverpeon.discord.internal.rxObservable
import net.serverpeon.discord.internal.toFuture
import net.serverpeon.discord.internal.ws.data.inbound.Channels
import net.serverpeon.discord.internal.ws.data.inbound.Event
import net.serverpeon.discord.internal.ws.data.inbound.Guilds
import net.serverpeon.discord.internal.ws.data.inbound.Misc
import net.serverpeon.discord.model.*
import rx.Observable
import java.time.Duration
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.properties.Delegates

class GuildNode internal constructor(val root: DiscordNode,
                                     override val id: DiscordId<Guild>,
                                     override var name: String,
                                     val ownerId: DiscordId<User>,
                                     override var region: Region) : Guild, EventInput<GuildNode> {
    private val changeId = AtomicInteger(0)
    internal var channelMap = createEmptyMap<Channel, ChannelNode.Public>()
    internal var roleMap = createEmptyMap<Role, RoleNode>()
        set(e: Map<DiscordId<Role>, RoleNode>) {
            if (e.isNotEmpty()) {
                // Turns out @everyone is not always position -1
                everyoneRole = e.values.sortedBy { it.position }.first()
            }
            field = e
        }
    internal var everyoneRole: RoleNode by Delegates.notNull()
    internal var memberMap = createEmptyMap<User, MemberNode>()
    internal var emojiMap = createEmptyMap<Emoji, EmojiNode>()

    override fun getChannelById(id: DiscordId<Channel>): Observable<Channel.Public> {
        return observableLookup(id) { channelMap[it] }
    }

    override val channels: Observable<Channel.Public>
        get() = Observable.defer {
            Observable.from<Channel.Public>(channelMap.values)
        }

    override fun getChannelByName(name: String): Observable<Channel.Public> {
        val sanitizedName = ChannelNode.sanitizeChannelName(name)
        return channels.filter { it.name == sanitizedName }
    }

    override fun createChannel(name: String, type: Channel.Type): CompletableFuture<Channel.Public> {
        check(type != Channel.Type.PRIVATE) { "Cannot create a PRIVATE channel in a public guild." }

        selfAsMember.checkPermission(this, PermissionSet.Permission.MANAGE_CHANNELS)

        return root.api.Guilds.createChannel(WrappedId(id), ChannelCreationRequest(
                name = name,
                type = type.let {
                    if (it == Channel.Type.TEXT) {
                        ChannelModel.Type.TEXT
                    } else {
                        ChannelModel.Type.VOICE
                    }
                }
        )).toFuture().thenApply { Builder.channel(it, this) }
    }

    override val roles: Observable<Role>
        get() = Observable.defer {
            Observable.from<Role>(roleMap.values)
        }

    override fun createRole(): CompletableFuture<Role.Edit> {
        return root.api.Guilds.createRole(WrappedId(id))
                .toFuture()
                .thenApply {
                    Builder.role(it, this)
                }.thenApply { it.edit() }
    }

    override val members: Observable<Guild.Member>
        get() = Observable.defer {
            Observable.from<Guild.Member>(memberMap.values)
        }

    override fun getMemberById(id: DiscordId<User>): Observable<Guild.Member> {
        return observableLookup(id) {
            memberMap[id]
        }
    }

    override fun getMemberByName(name: String): Observable<Guild.Member> {
        return members.filter { it.username == name }
    }

    override val emoji: Observable<Emoji>
        get() = Observable.defer { Observable.from<Emoji>(emojiMap.values) }

    override fun unban(id: DiscordId<User>): CompletableFuture<Void> {
        selfAsMember.checkPermission(this, PermissionSet.Permission.BAN_MEMBERS)

        return root.api.Guilds.removeBan(
                WrappedId(this.id),
                WrappedId(id)
        ).toFuture()
    }

    fun resolvePermissions(member: Guild.Member): PermissionSet {
        return if (member.id == ownerId) {
            return PermissionSet.ALL
        } else {
            member.roles.map {
                it.permissions
            }.reduce(PermissionSet.ZERO, { p1, p2 ->
                p1.with(p2) // Fold all roles together
            }).toBlocking().first()
        }
    }

    override fun edit(): Guild.Edit {
        selfAsMember.checkPermission(this, PermissionSet.Permission.MANAGE_SERVER)

        return Transaction(name, region, null, Duration.ZERO)
    }

    override fun delete(): CompletableFuture<Void> {
        selfAsMember.checkPermission(this, PermissionSet.Permission.MANAGE_SERVER)

        return root.api.Guilds.deleteGuild(WrappedId(id)).toFuture().thenApply {
            null
        }
    }

    override fun leave(): CompletableFuture<Void> {
        return root.api.Me.leaveGuild(WrappedId(id)).toFuture()
    }

    override fun getActiveInvites(): Observable<Invite.Details> {
        selfAsMember.checkPermission(this, PermissionSet.Permission.MANAGE_SERVER)
        return root.api.Guilds.getGuildInvites(WrappedId(id)).rxObservable().flatMapIterable {
            it
        }.map {
            Builder.invite(it, root)
        }
    }

    override val selfAsMember: MemberNode
        get() = memberMap[root.self.id]!!

    override fun toString(): String {
        return "Guild(id=$id, channels=${channelMap.values}, roles=${roleMap.values}, membersNo=${memberMap.size})"
    }

    fun wireToUser(id: DiscordId<User>, event: Event) {
        memberMap[id]?.handle(event)
    }

    override fun wireToChannel(id: DiscordId<Channel>, event: Event) {
        channelMap[id]!!.handle(event)
    }

    override fun handler(): EventInput.Handler<GuildNode> {
        return GuildEventHandler
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as GuildNode

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    private object GuildEventHandler : EventInput.Handler<GuildNode> {

        override fun guildUpdate(target: GuildNode, e: Guilds.General.Update) {
            e.guild.let {
                target.name = it.name
                target.region = RegionNode(it.region)
            }
        }

        override fun guildMemberAdd(target: GuildNode, e: Guilds.Members.Add) {
            check(e.member.user.id !in target.memberMap) { "Redundant member adds: $e" }

            target.memberMap += Builder.member(e.member, null, null, target)
        }

        override fun guildMemberUpdate(target: GuildNode, e: Guilds.Members.Update) {
            target.wireToUser(e.member.user.id, e)
        }

        override fun guildMemberRemove(target: GuildNode, e: Guilds.Members.Remove) {
            // FIXME: member might not exist when the REMOVE event happens
            // check(e.member.user.id in target.memberMap) { "Trying to remove non-existent member: $e" }
            target.memberMap[e.member.user.id]?.let {
                e.value = it
                target.memberMap -= it.userNode.id
            }
        }

        override fun guildBanAdd(target: GuildNode, e: Guilds.Bans.Add) {
            // Ignored for now
        }

        override fun guildBanRemove(target: GuildNode, e: Guilds.Bans.Remove) {
            // Ignored for now
        }

        override fun guildRoleCreate(target: GuildNode, e: Guilds.Roles.Create) {
            check(e.role.id !in target.roleMap) { "Duplicate role create $e" }

            target.roleMap += Builder.role(e.role, target)
        }

        override fun guildRoleUpdate(target: GuildNode, e: Guilds.Roles.Update) {
            target.roleMap[e.role.id]!!.handle(e)
        }

        override fun guildRoleDelete(target: GuildNode, e: Guilds.Roles.Delete) {
            check(e.role_id in target.roleMap) { "Attempt to remove non-existent role: $e" }
            target.memberMap.values.forEach { it.handle(e) }
            e.value = target.roleMap[e.role_id]
            target.roleMap -= e.role_id
        }

        override fun guildEmojiUpdate(target: GuildNode, e: Guilds.EmojiUpdate) {
            target.emojiMap = e.emojis.map { Builder.emoji(it, target) }.toImmutableIdMap()
        }

        override fun guildIntegrationsUpdate(target: GuildNode, e: Guilds.IntegrationsUpdate) {
            // Only a notification, ignored for now
        }

        override fun presenceUpdate(target: GuildNode, e: Misc.PresenceUpdate) {
            if (e.user.id in target.memberMap) {
                target.wireToUser(e.user.id, e)
            } else if (e.user.username != null) {
                // If the user is unknown, then the presence-update is used to insert their data
                target.memberMap += Builder.member(e, target)
            } // else does happen, but only in race conditions w/ people getting kicked.
        }

        override fun voiceStateUpdate(target: GuildNode, e: Misc.VoiceStateUpdate)
                = target.wireToUser(e.update.user_id, e)

        override fun channelCreate(target: GuildNode, e: Channels.Create.Public) {
            check(e.channel.id !in target.channelMap) { "Double channel creation: $e" }

            target.channelMap += Builder.channel(e.channel, target)
        }

        override fun channelDelete(target: GuildNode, e: Channels.Delete.Public) {
            check(e.channel.id in target.channelMap) { "Removing non-existent channel: $e" }
            e.value = target.channelMap[e.channel.id]
            target.channelMap -= e.channel.id
        }

        override fun guildMemberChunks(target: GuildNode, e: Misc.MembersChunk) {
            val memberMap = ImmutableMap.builder<DiscordId<User>, MemberNode>().apply {
                // Import override members
                e.members.forEach {
                    put(it.user.id, Builder.member(it, null, null, target))
                }

                val overriddenMembers = build().keys

                // Copy all members that were not overridden
                target.memberMap.filterKeys {
                    it !in overriddenMembers
                }.forEach {
                    put(it)
                }
            }.build()

            // Replace original memberMap
            target.memberMap = memberMap
        }
    }

    inner class Transaction(override var name: String, region: Region,
                            afkChannel: Channel.Public?, afkTimeout: Duration) : Guild.Edit {
        private var completed = AtomicBoolean(false)
        private val changeIdAtInit = changeId.get()
        private val changed = EnumSet.noneOf(GuildEditFlags::class.java)

        override var region: Region = region
            set(value) {
                field = value
                changed.add(GuildEditFlags.REGION)
            }
        override var afkChannel: Channel.Public? = afkChannel
            set(value) {
                field = value
                changed.add(GuildEditFlags.AFK_CHANNEL)
            }
        override var afkTimeout: Duration = afkTimeout
            set(value) {
                field = value
                changed.add(GuildEditFlags.AFK_TIMEOUT)
            }

        override fun commit(): CompletableFuture<Guild> {
            if (changeId.compareAndSet(changeIdAtInit, changeIdAtInit + 1)) {
                throw Editable.ResourceChangedException(this@GuildNode)
            } else if (completed.getAndSet(true)) {
                throw IllegalStateException("Don't call complete() twice")
            } else {
                return root.api.Guilds.editGuild(WrappedId(id), EditGuildRequest(
                        name = name,
                        region = if (GuildEditFlags.REGION in changed) region else null,
                        afk_channel_id = if (GuildEditFlags.AFK_CHANNEL in changed) afkChannel?.let { it.id } else null,
                        afk_timeout = if (GuildEditFlags.AFK_TIMEOUT in changed) afkTimeout else null
                )).toFuture().thenApply { Builder.guild(it, root) }
            }
        }
    }


    private enum class GuildEditFlags {
        REGION,
        AFK_CHANNEL,
        AFK_TIMEOUT
    }

    class EmojiNode(
            val roles: List<Role>,
            override val name: String,
            override val imported: Boolean,
            override val id: DiscordId<Emoji>,
            override val mustBeEscaped: Boolean) : Emoji {
        override val restrictedTo: Observable<Role>
            get() = Observable.defer { Observable.from(roles) }

        override fun toString(): String {
            return "<:$name:${id.repr}>"
        }
    }
}