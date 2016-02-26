package net.serverpeon.discord.internal.data

import com.google.common.collect.ImmutableList
import net.serverpeon.discord.internal.ws.data.inbound.*
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.Role
import net.serverpeon.discord.model.User
import rx.Observable
import java.time.ZonedDateTime

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
    override val user: User
        get() = this

    override val roles: Observable<Role>
        get() = observableList<Role, RoleNode> {
            // Sorting needs to happen in a deferred manner to ensure position changes get picked up
            internalRoles.sortedBy { it.position }
        }.compose { roleObservable ->
            Observable.concat(Observable.defer {
                Observable.just(guild.everyoneRole)
            }, roleObservable)
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
}