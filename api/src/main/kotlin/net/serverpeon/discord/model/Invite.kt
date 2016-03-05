package net.serverpeon.discord.model

import net.serverpeon.discord.interaction.PermissionException
import java.net.URI
import java.time.Instant
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture

/**
 * Represents a unique token which allows a user to join a specific guild/channel.
 *
 * These tokens are the only way by which users can join a guild, so it also acts as a method of access control.
 */
interface Invite : DiscordId.Identifiable<Invite> {
    /**
     * Description of the guild this invite is associated with.
     */
    val guild: GuildSpec

    data class GuildSpec(val id: DiscordId<Guild>, val name: String)

    /**
     * Description of the specific channel this invite is associated with.
     */
    val channel: ChannelSpec

    data class ChannelSpec(val id: DiscordId<Channel>, val name: String, val type: Channel.Type)

    /**
     * If enabled at creation time, the invite will include a human-readable alternative to [id].
     */
    val humanReadableId: DiscordId<Invite>?

    /**
     * Generates an URL that can be used to accept this invite through a browser or by retrieving this object using
     * [ClientModel.getInvite] and then calling [Invite.accept].
     */
    fun generateURI(): URI {
        val code = humanReadableId ?: id
        return URI.create("https://discord.gg/${code.repr}")
    }

    /**
     * Accept the invite on the user that the client is logged in with.
     * If the invite has not yet expired then the client will be added to the appropriate [Guild].
     */
    fun accept(): CompletableFuture<Void>

    interface Details : Invite {
        /**
         * The user which created this invite.
         */
        val inviter: User

        /**
         * Whether this invite has been revoked, if True then calls to [accept] will fail.
         *
         * Use [revoke] to revoke this invite.
         */
        val revoked: Boolean

        /**
         * Whether this invite only gives temporary membership to the invitee.
         */
        val temporary: Boolean

        /**
         * Moment when this invite was created.
         */
        val createdAt: ZonedDateTime

        /**
         * The number of times this invite has been used.
         */
        val uses: Int

        /**
         * Number of times this invite can be used, infinite if 0.
         */
        val maxUses: Int

        /**
         * Moment at which this invite will expire, after which it cannot be used anymore.
         */
        val expiresAt: Instant?

        /**
         * Revoke this invitation.
         *
         * After successfully calling this method the invite will be [revoked] and cannot be [accept]ed to join the
         * channel anymore.
         *
         * @throws PermissionException If the client does not have [PermissionSet.Permission.MANAGE_CHANNEL] for the
         *                             channel associated with the invite.
         */
        @Throws(PermissionException::class)
        fun revoke(): CompletableFuture<Void>
    }
}