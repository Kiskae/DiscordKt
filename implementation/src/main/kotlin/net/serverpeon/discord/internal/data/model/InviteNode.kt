package net.serverpeon.discord.internal.data.model

import net.serverpeon.discord.interaction.PermissionException
import net.serverpeon.discord.internal.rest.WrappedId
import net.serverpeon.discord.internal.toFuture
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Invite
import net.serverpeon.discord.model.PermissionSet
import net.serverpeon.discord.model.User
import java.time.Instant
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture

open class InviteNode internal constructor(val root: DiscordNode,
                                           override val guild: Invite.GuildSpec,
                                           override val channel: Invite.ChannelSpec,
                                           override val humanReadableId: DiscordId<Invite>?,
                                           override val id: DiscordId<Invite>) : Invite {
    override fun accept(): CompletableFuture<Void> {
        return root.api.Invites.acceptInvite(WrappedId(id)).toFuture().thenAccept { }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InviteNode) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Invite(id=${humanReadableId ?: id}, channel=$channel, guild=$guild)"
    }

    class Rich internal constructor(root: DiscordNode,
                                    guild: Invite.GuildSpec,
                                    channel: Invite.ChannelSpec,
                                    humanReadableId: DiscordId<Invite>?,
                                    id: DiscordId<Invite>,
                                    override val inviter: User,
                                    override val revoked: Boolean,
                                    override val temporary: Boolean,
                                    override val createdAt: ZonedDateTime,
                                    override val uses: Int,
                                    override val maxUses: Int,
                                    override val expiresAt: Instant?
    ) : InviteNode(root, guild, channel, humanReadableId, id), Invite.Details {
        override fun revoke(): CompletableFuture<Void> {
            // If the channel is unknown, we are not able to delete it.
            val target = root.channelMap[channel.id] ?: throw PermissionException(PermissionSet.Permission.MANAGE_CHANNEL)
            target.checkPermission(PermissionSet.Permission.MANAGE_CHANNEL)

            return root.api.Invites.deleteInvite(WrappedId(id)).toFuture().thenAccept { }
        }
    }
}