package net.serverpeon.discord.internal.data

import com.google.common.collect.ImmutableList
import net.serverpeon.discord.interaction.Editable
import net.serverpeon.discord.internal.rest.data.ChannelModel
import net.serverpeon.discord.internal.rest.data.GuildModel
import net.serverpeon.discord.internal.rest.data.RegionModel
import net.serverpeon.discord.internal.rest.data.WrappedId
import net.serverpeon.discord.internal.rest.retro.Guilds.ChannelCreationRequest
import net.serverpeon.discord.internal.rest.retro.Guilds.EditGuildRequest
import net.serverpeon.discord.internal.toFuture
import net.serverpeon.discord.internal.ws.data.inbound.*
import net.serverpeon.discord.model.*
import rx.Observable
import java.time.Duration
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger
import kotlin.properties.Delegates

class GuildNode(val root: DiscordNode, override val id: DiscordId<Guild>, override var name: String,
                val ownerId: DiscordId<User>,
                override var region: Region) : Guild, Event.Visitor {
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
        get() = observableList<Channel, Channel.Public> { channelMap.values }

    override fun channelCreate(e: Channels.Create.Public) {
        check(e.channel.id !in channelMap) { "Double channel creation: $e" }

        val channel = ChannelNode.from(e.channel, this, root)
        channelMap = channelMap.immutableAdd(channel.id, channel)
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
        )).toFuture().thenApply { ChannelNode.from(it, this, root) }
    }

    override val roles: Observable<Role>
        get() = observableList<Role, Role> { roleMap.values }

    override fun createRole(): CompletableFuture<Role.Edit> {
        return root.api.Guilds.createRole(WrappedId(id))
                .toFuture()
                .thenApply {
                    RoleNode.from(it, this, root)
                }.thenApply { it.edit() }
    }

    override val members: Observable<Guild.Member>
        get() = observableList<Guild, Guild.Member> { memberMap.values }

    override fun getMemberById(id: DiscordId<User>): Observable<Guild.Member> {
        return observableLookup(id) {
            memberMap[id]
        }
    }

    override fun getMemberByName(name: String): Observable<Guild.Member> {
        return members.filter { it.username == name }
    }

    override val emoji: Observable<Emoji>
        get() = observableList<Emoji, Emoji> { emojiMap.values }

    override fun guildUpdate(e: Guilds.General.Update) {
        e.guild.let {
            name = it.name
            region = RegionNode(it.region)
        }
    }

    override fun guildMemberAdd(e: Guilds.Members.Add) {
        check(e.member.user.id !in memberMap) { "Redundant member adds: $e" }

        val newMember = MemberNode.from(e.member, null, null, this, root)
        memberMap = memberMap.immutableAdd(newMember.id, newMember)
    }

    override fun guildMemberUpdate(e: Guilds.Members.Update) = wireToUser(e.member.user.id, e)

    override fun guildMemberRemove(e: Guilds.Members.Remove) {
        check(e.member.user.id in memberMap) { "Trying to remove non-existent member: $e" }
        e.value = memberMap[e.member.user.id]
        memberMap = memberMap.immutableRemove(e.member.user.id)
    }

    override fun guildBanAdd(e: Guilds.Bans.Add) {
        // Ignored for now
    }

    override fun guildBanRemove(e: Guilds.Bans.Remove) {
        // Ignored for now
    }

    override fun guildRoleCreate(e: Guilds.Roles.Create) {
        check(e.role.id !in roleMap) { "Duplicate role create $e" }

        val role = RoleNode.from(e.role, this, root)
        roleMap = roleMap.immutableAdd(role.id, role)
    }

    override fun guildRoleUpdate(e: Guilds.Roles.Update) {
        roleMap[e.role.id]!!.visit(e)
    }

    override fun guildRoleDelete(e: Guilds.Roles.Delete) {
        check(e.role_id in roleMap) { "Attempt to remove non-existent role: $e" }
        memberMap.values.forEach { it.visit(e) }
        e.value = roleMap[e.role_id]
        roleMap = roleMap.immutableRemove(e.role_id)
    }

    override fun guildEmojiUpdate(e: Guilds.EmojiUpdate) {
        emojiMap = e.emojis.map { parseEmoji(it, this) }.toImmutableIdMap()
    }

    override fun guildIntegrationsUpdate(e: Guilds.IntegrationsUpdate) {
        // Only a notification, ignored for now
    }

    override fun presenceUpdate(e: Misc.PresenceUpdate) = wireToUser(e.user.id, e)

    override fun voiceStateUpdate(e: Misc.VoiceStateUpdate) = wireToUser(e.update.user_id, e)

    override fun wireToUser(id: DiscordId<User>, e: Event) {
        memberMap[id]?.visit(e)
    }

    override fun channelDelete(e: Channels.Delete.Public) {
        check(e.channel.id in channelMap) { "Removing non-existent channel: $e" }
        e.value = channelMap[e.channel.id]
        channelMap.immutableRemove(e.channel.id)
    }

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

    inner class Transaction(override var name: String, region: Region,
                            afkChannel: Channel.Public?, afkTimeout: Duration) : Guild.Edit {
        private var aborted: TransactionTristate = TransactionTristate.AWAIT
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
            } else if (aborted == TransactionTristate.ABORTED) {
                throw Editable.AbortedTransactionException()
            } else if (aborted == TransactionTristate.COMPLETED) {
                throw IllegalStateException("Don't call complete() twice")
            } else {
                aborted = TransactionTristate.COMPLETED
                return root.api.Guilds.editGuild(WrappedId(id), EditGuildRequest(
                        name = name,
                        region = if (GuildEditFlags.REGION in changed) region else null,
                        afk_channel_id = if (GuildEditFlags.AFK_CHANNEL in changed) afkChannel?.let { it.id } else null,
                        afk_timeout = if (GuildEditFlags.AFK_TIMEOUT in changed) afkTimeout else null
                )).toFuture().thenApply { GuildNode.from(it, root) }
            }
        }

        override fun abort() {
            if (aborted == TransactionTristate.AWAIT) {
                aborted = TransactionTristate.ABORTED
            } else if (aborted == TransactionTristate.COMPLETED) {
                throw IllegalArgumentException("abort() after complete()")
            }
        }
    }

    private enum class GuildEditFlags {
        REGION,
        AFK_CHANNEL,
        AFK_TIMEOUT
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

    override val selfAsMember: MemberNode
        get() = memberMap[root.self.id]!!

    override fun toString(): String {
        return "Guild(id=$id, channels=${channelMap.values}, roles=${roleMap.values}, membersNo=${memberMap.size})"
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

    class RegionNode(override val id: String) : Region {
        override val continent: Region.Continent by lazy { guessContinent(id) }

        override fun toString(): String {
            return "Region(id='$id')"
        }
    }

    companion object {
        private fun guessContinent(regionId: String): Region.Continent {
            return when (regionId) {
                "amsterdam" -> Region.Continent.EUROPE
                "frankfurt" -> Region.Continent.EUROPE
                "london" -> Region.Continent.EUROPE
                "singapore" -> Region.Continent.ASIA
                "sydney" -> Region.Continent.AUSTRALIA
                else -> {
                    if (regionId.startsWith("us-")) {
                        Region.Continent.NORTH_AMERICA
                    } else {
                        Region.Continent.UNKNOWN
                    }
                }
            }
        }

        fun from(data: RegionModel): RegionNode {
            return RegionNode(data.id)
        }

        private fun parseEmoji(model: GuildModel.DataEmoji, guildNode: GuildNode): EmojiNode {
            return EmojiNode(
                    ImmutableList.copyOf(model.roles.map { guildNode.roleMap[it]!! }),
                    model.name,
                    model.managed,
                    model.id,
                    model.require_colons
            )
        }

        fun from(data: GuildModel, root: DiscordNode): GuildNode {
            val guildNode = GuildNode(root, data.id, data.name, data.owner_id, RegionNode(data.region))
            return guildNode
        }

        fun from(data: ReadyEventModel.ExtendedGuild, root: DiscordNode): GuildNode {
            val guildNode = GuildNode(root, data.id, data.name, data.owner_id, RegionNode(data.region))

            val presences = data.presences.map { it.user.id to it }.toMap()
            val voiceStates = data.voice_states.map { it.user_id to it }.toMap()

            guildNode.channelMap = data.channels.map { ChannelNode.from(it, guildNode, root) }.toImmutableIdMap()
            guildNode.roleMap = data.roles.map { RoleNode.from(it, guildNode, root) }.toImmutableIdMap()
            guildNode.memberMap = data.members.map {
                MemberNode.from(it, presences[it.user.id], voiceStates[it.user.id], guildNode, root)
            }.toImmutableIdMap()
            guildNode.emojiMap = data.emojis.map { parseEmoji(it, guildNode) }.toImmutableIdMap()

            return guildNode
        }
    }
}