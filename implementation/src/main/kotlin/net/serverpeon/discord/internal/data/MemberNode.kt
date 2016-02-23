package net.serverpeon.discord.internal.data

import com.google.common.collect.ImmutableList
import net.serverpeon.discord.internal.ws.data.inbound.Guilds
import net.serverpeon.discord.internal.ws.data.inbound.MemberModel
import net.serverpeon.discord.internal.ws.data.inbound.Misc
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.Role
import net.serverpeon.discord.model.User
import rx.Observable
import java.time.ZonedDateTime

class MemberNode(val guildNode: GuildNode,
                 val userNode: UserNode,
                 var internalRoles: List<RoleNode>,
                 override val joinedAt: ZonedDateTime) : Guild.Member, EventProcessor {
    override val roles: Observable<Role>
        get() = Observable.defer<Role> {
            Observable.from(internalRoles)
        }
    override val discriminator: String
        get() = userNode.discriminator
    override val avatar: DiscordId<User.Avatar>?
        get() = userNode.avatar
    override val id: DiscordId<User>
        get() = userNode.id
    override val username: String
        get() = userNode.username

    override fun acceptEvent(event: Any) {
        when (event) {
            is Misc.PresenceUpdate -> {
                userNode.acceptEvent(event)
            }
            is Guilds.Members.Update -> {
                internalRoles = generateRoleList(event.member.roles, guildNode.roles)
                //TODO: when adding mute/deaf, update those too
            }
            is Guilds.Roles.Delete -> {
                if (internalRoles.find { it.id == event.role_id } != null) {
                    internalRoles = ImmutableList.copyOf(internalRoles.filterNot {
                        it.id == event.role_id
                    })
                }
            }
        }
    }

    companion object {
        fun from(model: MemberModel,
                 guildNode: GuildNode,
                 root: DiscordNode): MemberNode {
            return MemberNode(
                    guildNode,
                    root.userCache.retrieve(model.user.id, model.user),
                    generateRoleList(model.roles, guildNode.roles),
                    model.joined_at
            )
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