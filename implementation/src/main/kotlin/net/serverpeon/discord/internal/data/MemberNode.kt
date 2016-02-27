package net.serverpeon.discord.internal.data

import com.google.common.collect.ImmutableList
import net.serverpeon.discord.interaction.Editable
import net.serverpeon.discord.interaction.PermissionException
import net.serverpeon.discord.internal.rest.data.WrappedId
import net.serverpeon.discord.internal.rest.retro.Guilds.EditMemberRequest
import net.serverpeon.discord.internal.toFuture
import net.serverpeon.discord.internal.ws.data.inbound.*
import net.serverpeon.discord.model.*
import rx.Observable
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger

class MemberNode(override val guild: GuildNode,
                 val userNode: UserNode,
                 var internalRoles: List<RoleNode>,
                 override val joinedAt: ZonedDateTime,
                 override var status: Guild.Member.Status,
                 override var currentGame: String?,
                 override var deaf: Boolean,
                 override var mute: Boolean,
                 override var forcedDeaf: Boolean,
                 override var forcedMute: Boolean) : Guild.Member, Event.Visitor {
    private val changeId = AtomicInteger(0)

    override val user: User
        get() = this

    override val roles: Observable<Role>
        get() = observableList<Role, RoleNode> {
            // Sorting needs to happen in a deferred manner to ensure position changes get picked up
            internalRoles.sortedBy { it.position }
        }.compose { roleObservable ->
            Observable.concat(roleObservable, Observable.defer {
                Observable.just(guild.everyoneRole)
            })
        }.cast(Role::class.java)

    override val discriminator: String
        get() = userNode.discriminator
    override val avatar: DiscordId<User.Avatar>?
        get() = userNode.avatar
    override val id: DiscordId<User>
        get() = userNode.id
    override val username: String
        get() = userNode.username

    override fun presenceUpdate(e: Misc.PresenceUpdate) {
        userNode.visit(e)

        currentGame = e.game?.name
        status = mapStatus(e.status)
    }

    override fun voiceStateUpdate(e: Misc.VoiceStateUpdate) {
        e.update.let {
            deaf = it.deaf
            mute = it.mute
            forcedMute = it.self_mute
            forcedDeaf = it.self_deaf
        }
    }

    override fun guildMemberUpdate(e: Guilds.Members.Update) {
        changeId.incrementAndGet()
        internalRoles = generateRoleList(e.member.roles, guild.roleMap)
    }

    override fun guildRoleDelete(e: Guilds.Roles.Delete) {
        if (internalRoles.isNotEmpty() && internalRoles.find { it.id == e.role_id } != null) {
            internalRoles = ImmutableList.copyOf(internalRoles.filterNot {
                it.id == e.role_id
            })
        }
    }

    override fun toString(): String {
        return "Member(user=$userNode, guild=${guild.id}, roles=$internalRoles, joinedAt=$joinedAt, status=$status, currentGame=$currentGame)"
    }

    override fun kick(): CompletableFuture<Void> {
        checkPermission(guild, PermissionSet.Permission.KICK_MEMBERS)

        return guild.root.api.Guilds.kickMember(WrappedId(guild.id), WrappedId(id)).toFuture()
    }

    override fun ban(clearLastXDays: Int?): CompletableFuture<DiscordId<User>> {
        checkPermission(guild, PermissionSet.Permission.BAN_MEMBERS)

        return guild.root.api.Guilds.addBan(WrappedId(guild.id), WrappedId(id), clearLastXDays).toFuture().thenApply {
            id
        }
    }

    override fun edit(): Guild.Member.Edit {
        checkPermission(guild, PermissionSet.Permission.MANAGE_ROLES)

        return Transaction(internalRoles.toMutableList())
    }

    inner class Transaction(override var roles: MutableList<Role>) : Guild.Member.Edit {
        private var aborted: TransactionTristate = TransactionTristate.AWAIT
        private val changeIdAtInit = changeId.get()

        override fun commit(): CompletableFuture<Guild.Member> {
            if (changeId.compareAndSet(changeIdAtInit, changeIdAtInit + 1)) {
                throw Editable.ResourceChangedException(this@MemberNode)
            } else if (aborted == TransactionTristate.ABORTED) {
                throw Editable.AbortedTransactionException()
            } else if (aborted == TransactionTristate.COMPLETED) {
                throw IllegalStateException("Don't call complete() twice")
            } else {
                aborted = TransactionTristate.COMPLETED
                return guild.root.api.Guilds.editMember(WrappedId(guild.id), WrappedId(id), EditMemberRequest(
                        roles = roles.map { it.id }
                )).toFuture().thenApply {
                    this@MemberNode
                } //Can't do anything about this :|
            }
        }

        override fun abort() {
            if (aborted == TransactionTristate.AWAIT) {
                aborted = TransactionTristate.ABORTED
            } else if (aborted == TransactionTristate.COMPLETED) {
                throw IllegalArgumentException("äbort() after complete()")
            }
        }
    }

    companion object {
        fun from(model: MemberModel,
                 presence: ReadyEventModel.ExtendedGuild.Presence?,
                 voiceState: VoiceStateModel?,
                 guildNode: GuildNode,
                 root: DiscordNode): MemberNode {
            return MemberNode(
                    guildNode,
                    root.userCache.retrieve(model.user.id, model.user),
                    generateRoleList(model.roles, guildNode.roleMap),
                    model.joined_at,
                    presence?.status?.let { mapStatus(it) } ?: Guild.Member.Status.OFFLINE,
                    presence?.game?.name,
                    voiceState?.deaf ?: false,
                    voiceState?.mute ?: false,
                    voiceState?.self_deaf ?: false,
                    voiceState?.self_mute ?: false
            )
        }

        private fun mapStatus(status: Misc.PresenceUpdate.Status): Guild.Member.Status {
            return when (status) {
                Misc.PresenceUpdate.Status.ONLINE -> Guild.Member.Status.ONLINE
                Misc.PresenceUpdate.Status.IDLE -> Guild.Member.Status.IDLE
                Misc.PresenceUpdate.Status.OFFLINE -> Guild.Member.Status.OFFLINE
            }
        }

        private fun generateRoleList(roles: Iterable<DiscordId<Role>>?,
                                     lookup: Map<DiscordId<Role>, RoleNode>): List<RoleNode> {
            return roles?.let { roles ->
                ImmutableList.copyOf(roles.map {
                    lookup[it]!!
                })
            } ?: ImmutableList.of()
        }
    }

    fun checkPermission(guild: GuildNode, permission: PermissionSet.Permission) {
        if (permission !in guild.resolvePermissions(this)) {
            throw PermissionException(permission)
        }
    }

    fun checkPermission(channel: ChannelNode.Public, permission: PermissionSet.Permission) {
        if (permission !in channel.permissionsFor(this)) {
            throw PermissionException(permission)
        }
    }
}