package net.serverpeon.discord.internal.data.model

import com.google.common.collect.ImmutableList
import net.serverpeon.discord.interaction.Editable
import net.serverpeon.discord.interaction.PermissionException
import net.serverpeon.discord.internal.data.EventInput
import net.serverpeon.discord.internal.data.TransactionTristate
import net.serverpeon.discord.internal.rest.WrappedId
import net.serverpeon.discord.internal.rest.retro.Guilds.EditMemberRequest
import net.serverpeon.discord.internal.toFuture
import net.serverpeon.discord.internal.ws.data.inbound.Guilds
import net.serverpeon.discord.internal.ws.data.inbound.Misc
import net.serverpeon.discord.message.Message
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
                 override var forcedMute: Boolean) : Guild.Member, EventInput<MemberNode> {
    private val changeId = AtomicInteger(0)

    override val user: User
        get() = this

    override val roles: Observable<Role>
        get() = Observable.defer {
            // Sorting needs to happen in a deferred manner to ensure position changes get picked up
            Observable.from(internalRoles.sortedBy { it.position })
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

    override fun sendMessage(message: Message): CompletableFuture<PostedMessage> {
        return userNode.sendMessage(message)
    }

    override fun edit(): Guild.Member.Edit {
        checkPermission(guild, PermissionSet.Permission.MANAGE_ROLES)

        return Transaction(internalRoles.toMutableList())
    }

    override fun handler(): EventInput.Handler<MemberNode> {
        return MemberEventHandler
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

    private object MemberEventHandler : EventInput.Handler<MemberNode> {
        override fun presenceUpdate(target: MemberNode, e: Misc.PresenceUpdate) {
            target.userNode.handle(e)

            target.currentGame = e.game?.name
            target.status = mapStatus(e.status)
        }

        override fun voiceStateUpdate(target: MemberNode, e: Misc.VoiceStateUpdate) {
            e.update.let {
                target.deaf = it.deaf
                target.mute = it.mute
                target.forcedMute = it.self_mute
                target.forcedDeaf = it.self_deaf
            }
        }

        override fun guildMemberUpdate(target: MemberNode, e: Guilds.Members.Update) {
            target.changeId.incrementAndGet()
            target.internalRoles = generateRoleList(e.member.roles, target.guild.roleMap)
        }

        override fun guildRoleDelete(target: MemberNode, e: Guilds.Roles.Delete) {
            val roles = target.internalRoles
            if (roles.isNotEmpty() && roles.find { it.id == e.role_id } != null) {
                target.internalRoles = ImmutableList.copyOf(roles.filterNot {
                    it.id == e.role_id
                })
            }
        }
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
        internal fun mapStatus(status: Misc.PresenceUpdate.Status): Guild.Member.Status {
            return when (status) {
                Misc.PresenceUpdate.Status.ONLINE -> Guild.Member.Status.ONLINE
                Misc.PresenceUpdate.Status.IDLE -> Guild.Member.Status.IDLE
                Misc.PresenceUpdate.Status.OFFLINE -> Guild.Member.Status.OFFLINE
            }
        }

        internal fun generateRoleList(roles: Iterable<DiscordId<Role>>?,
                                      lookup: Map<DiscordId<Role>, RoleNode>): List<RoleNode> {
            return roles?.let { roles ->
                ImmutableList.copyOf(roles.map {
                    lookup[it]!!
                })
            } ?: ImmutableList.of()
        }
    }
}