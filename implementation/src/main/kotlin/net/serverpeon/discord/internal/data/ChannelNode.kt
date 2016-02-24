package net.serverpeon.discord.internal.data

import net.serverpeon.discord.internal.rest.data.ChannelModel
import net.serverpeon.discord.internal.ws.data.inbound.Channels
import net.serverpeon.discord.internal.ws.data.inbound.Event
import net.serverpeon.discord.internal.ws.data.inbound.Misc
import net.serverpeon.discord.internal.ws.data.inbound.PrivateChannelModel
import net.serverpeon.discord.model.*
import rx.Observable
import java.util.*

abstract class ChannelNode private constructor(val root: DiscordNode,
                                               override val id: DiscordId<Channel>) : Channel, Event.Visitor {
    override fun typingStart(e: Misc.TypingStart) {
        // Ignored, not something we need to represent
    }

    override fun toString(): String {
        return "Channel(id=$id)"
    }

    class Public internal constructor(root: DiscordNode,
                                      id: DiscordId<Channel>,
                                      override val guild: GuildNode,
                                      override var topic: String,
                                      override val type: Channel.Type,
                                      override var name: String,
                                      private var overrides: LinkedHashMap<DiscordId<*>, OverrideData>
    ) : ChannelNode(root, id), Channel.Public {
        override val members: Observable<Guild.Member> = Observable.empty() //TODO: implement voice state
        override val memberOverrides: Observable<Channel.ResolvedPermission<Guild.Member>>
            get() = filterAndMapOverrides(shouldBeMember = true) { id ->
                val user = guild.memberMap[id]!!
                val perms = permissionsFor(user)
                Channel.ResolvedPermission(user, perms)
            }

        override val roleOverrides: Observable<Channel.ResolvedPermission<Role>>
            get() = filterAndMapOverrides(shouldBeMember = false) { id ->
                val role = guild.roleMap[id]!!
                val perms = permissionsFor(role)
                Channel.ResolvedPermission(role, perms)
            }

        private fun <T : DiscordId.Identifiable<T>, G : T> filterAndMapOverrides(
                shouldBeMember: Boolean,
                objectLookup: (DiscordId<T>) -> Channel.ResolvedPermission<G>
        ): Observable<Channel.ResolvedPermission<G>> {
            return observableList<User, MutableMap.MutableEntry<DiscordId<*>, OverrideData>> {
                overrides.entries
            }.filter {
                it.value.isMember == shouldBeMember
            }.map {
                @Suppress("UNCHECKED_CAST")
                objectLookup(it.key as DiscordId<T>)
            }
        }

        override fun permissionsFor(role: Role): PermissionSet {
            val overrides = overrides[role.id]
            return overrides?.let {
                role.permissions.without(it.deny).with(it.allow)
            } ?: role.permissions
        }

        override fun permissionsFor(member: Guild.Member): PermissionSet {
            return member.roles.map {
                permissionsFor(it)
            }.reduce(PermissionSet.ZERO, { p1, p2 ->
                p1.with(p2) // Fold all roles together
            }).map { rolePerms ->
                val overrides = overrides[member.id]
                overrides?.let {
                    rolePerms.without(it.deny).with(it.allow)
                } ?: rolePerms
            }.toBlocking().first() //Everything SHOULD be available, so this won't cause problems (famous last words)
        }

        override val isPrivate: Boolean
            get() = false

        override fun channelUpdate(e: Channels.Update) {
            e.channel.let {
                this.name = it.name
                this.topic = it.topic ?: ""
                this.overrides = translateOverrides(it.permission_overwrites)
            }
        }

        override fun voiceStateUpdate(e: Misc.VoiceStateUpdate) {
            // Pass through to member
            guild.memberMap[e.update.user_id]?.voiceStateUpdate(e)
        }
    }

    data class OverrideData(val allow: PermissionSet, val deny: PermissionSet, val isMember: Boolean)

    class Private internal constructor(root: DiscordNode,
                                       id: DiscordId<Channel>,
                                       override val recipient: UserNode) : ChannelNode(root, id), Channel.Private {
        override val type: Channel.Type
            get() = Channel.Type.PRIVATE
        override val isPrivate: Boolean
            get() = true
    }

    companion object {
        fun from(channel: ChannelModel, guild: GuildNode, root: DiscordNode): ChannelNode.Public {
            return ChannelNode.Public(
                    root,
                    channel.id,
                    guild,
                    channel.topic ?: "",
                    if (channel.type == ChannelModel.Type.TEXT) Channel.Type.TEXT else Channel.Type.VOICE,
                    channel.name,
                    translateOverrides(channel.permission_overwrites)
            )
        }

        fun from(privateChannel: PrivateChannelModel, root: DiscordNode): ChannelNode.Private {
            return ChannelNode.Private(
                    root,
                    privateChannel.id,
                    root.userCache.retrieve(privateChannel.recipient)
            )
        }

        private fun translateOverrides(
                permission_overwrites: List<ChannelModel.PermissionOverwrites>
        ): LinkedHashMap<DiscordId<*>, OverrideData> {
            val pairs = permission_overwrites.map {
                DiscordId<Role>(it.id) to OverrideData(it.allow, it.deny, it.type == "member")
            }.toTypedArray()
            return linkedMapOf(*pairs)
        }
    }
}