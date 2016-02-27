package net.serverpeon.discord.model

import net.serverpeon.discord.interaction.Deletable
import net.serverpeon.discord.interaction.Editable
import net.serverpeon.discord.interaction.PermissionException
import rx.Observable
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture

/**
 * Discord represents individual servers as "Guilds"
 *
 * Each guild has a number of channels that can either be [Channel.Type.VOICE] or [Channel.Type.TEXT] channels.
 * They also contain a list of members with assigned permissions that can be queried on a channel-by-channel basis.
 */
interface Guild : DiscordId.Identifiable<Guild>, Editable<Guild, Guild.Edit>, Deletable {
    /**
     *
     */
    val name: String

    /**
     * Retrieves a list of all channels in this guild.
     */
    val channels: Observable<Channel.Public>

    /**
     *
     */
    fun getChannelById(id: DiscordId<Channel>): Observable<Channel.Public>

    /**
     *
     */
    fun getChannelByName(name: String): Observable<Channel.Public>

    /**
     *
     */
    val members: Observable<Member>

    /**
     *
     */
    fun getMemberById(id: DiscordId<User>): Observable<Member>

    /**
     *
     */
    fun getMemberByName(name: String): Observable<Member>

    /**
     *
     */
    val emoji: Observable<Emoji>

    /**
     *
     */
    val selfAsMember: Member

    /**
     *
     * @throws PermissionException if the user does not have [PermissionSet.Permission.BAN_MEMBERS]
     */
    @Throws(PermissionException::class)
    fun unban(id: DiscordId<User>): CompletableFuture<Void>

    interface Edit : Editable.Transaction<Edit, Guild> {
        /**
         * Name of the guild, must be between 2 and 100 characters long.
         */
        var name: String

        /**
         * Region in which the server is deployed.
         */
        var region: String //FIXME: Region object

        /**
         * Channel in which users will be placed if they do not speak for [afkTimeout] time.
         */
        var afkChannel: Channel.Public?

        /**
         * Amount of time after which a user is considered AFK.
         *
         * Valid values are 60 seconds, 300 seconds (5 minutes), 900 seconds (15 minutes), 1800 seconds (30 minutes)
         * and 3600 seconds (1 hour)
         */
        var afkTimeout: Duration

        //TODO: icon setting, requires encoding as Base64 + type handling
    }

    interface Member : User, VoiceState, Editable<Member, Member.Edit> {
        /**
         *
         */
        val guild: Guild

        /**
         *
         */
        val joinedAt: ZonedDateTime

        /**
         *
         */
        val roles: Observable<Role>

        /**
         *
         */
        val status: Status

        /**
         *
         */
        val currentGame: String?

        /**
         *
         */
        fun permissionsFor(channel: Channel.Public): PermissionSet {
            return channel.permissionsFor(this)
        }

        /**
         * @throws PermissionException if the user does not have [PermissionSet.Permission.KICK_MEMBERS]
         */
        @Throws(PermissionException::class)
        fun kick(): CompletableFuture<Void>

        /**
         *
         * @throws PermissionException if the user does not have [PermissionSet.Permission.BAN_MEMBERS]
         */
        @Throws(PermissionException::class)
        fun ban(clearLastXDays: Int? = null): CompletableFuture<DiscordId<User>>

        interface Edit : Editable.Transaction<Edit, Member> {
            var roles: MutableList<Role>
        }

        enum class Status {
            ONLINE,
            IDLE,
            OFFLINE
        }
    }
}