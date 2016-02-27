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
     * Name of the guild, it appears this can contain pretty much any characters.
     */
    val name: String

    /**
     * The region in which this server is located.
     */
    val region: Region

    /**
     * Retrieves a list of all channels in this guild.
     */
    val channels: Observable<Channel.Public>

    /**
     * Attempts to find and return the channel with the given id.
     *
     * @param id Identifier of the targeted channel.
     * @return Either an empty observable or one that returns the channel with the given id.
     */
    fun getChannelById(id: DiscordId<Channel>): Observable<Channel.Public>

    /**
     * Lazily attempt to find a channel with the given name, if the availability of channels changes between calls to
     * [Observable.subscribe] then this method can return different results.
     *
     * This can return multiple channels since channel names are not unique.
     *
     * When looking for a specific channel prefer [getChannelById].
     *
     * @param name Name of the channel for which to search.
     * @return Lazy observable representing all channels that have the given name as [Channel.Public.name].
     */
    fun getChannelByName(name: String): Observable<Channel.Public>

    /**
     * Creates a new channel of the specified type.
     *
     * @param name Name of the new channel, must be between 2 and 100 characters.
     * @param type Type of the new channel, must be either [Channel.Type.TEXT] or [Channel.Type.VOICE]
     * @return Future of the newly created channel.
     * @throws PermissionException if the permission [PermissionSet.Permission.MANAGE_CHANNELS] is missing.
     */
    @Throws(PermissionException::class)
    fun createChannel(name: String, type: Channel.Type): CompletableFuture<Channel.Public>

    /**
     * Retrieves a list of all members that are a part of this guild.
     */
    val members: Observable<Member>

    /**
     * Attempts to find a member with the given [id] within this server.
     */
    fun getMemberById(id: DiscordId<User>): Observable<Member>

    /**
     * Finds all members with the username that matches [name].
     * Since people can share usernames this method can return multiple members.
     */
    fun getMemberByName(name: String): Observable<Member>

    /**
     * Retrieve a list of all roles defined on this server.
     */
    val roles: Observable<Role>

    /**
     * Creates a new role on the server.
     * Since roles are created and then edited to give them content, this method directly returns the
     * [Role] editing interface.
     *
     * @throws PermissionException if requirement [PermissionSet.Permission.MANAGE_ROLES] is missing.
     */
    @Throws(PermissionException::class)
    fun createRole(): CompletableFuture<Role.Edit>

    //TODO: role reordering

    /**
     * Retrieves a list of emoji which is made available to a group of members within this server.
     */
    val emoji: Observable<Emoji>

    /**
     * Retrieves the member profile of the logged in user.
     */
    val selfAsMember: Member

    /**
     * Unban user with the given [id].
     *
     * @throws PermissionException if the user does not have [PermissionSet.Permission.BAN_MEMBERS]
     */
    @Throws(PermissionException::class)
    fun unban(id: DiscordId<User>): CompletableFuture<Void>

    /**
     * Causes the client's user to leave this server.
     *
     * This will probably fail if the user is also the owner.
     */
    fun leave(): CompletableFuture<Void>

    interface Edit : Editable.Transaction<Edit, Guild> {
        /**
         * Name of the guild, must be between 2 and 100 characters long.
         */
        var name: String

        /**
         * Region in which the server is deployed.
         */
        var region: Region

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
         * Guild to which this member belongs
         */
        val guild: Guild

        /**
         * DateTime when this user first joined the guild.
         */
        val joinedAt: ZonedDateTime

        /**
         * The set of guild roles that have been assigned to this user.
         */
        val roles: Observable<Role>

        /**
         * Availability status of this member
         */
        val status: Status

        /**
         * Game that the user is currently reporting to Discord.
         */
        val currentGame: String?

        /**
         * Resolves the active set of permissions for the given channel for this user.
         */
        fun permissionsFor(channel: Channel.Public): PermissionSet {
            return channel.permissionsFor(this)
        }

        /**
         * Kicks this user from the guild.
         *
         * @throws PermissionException if the user does not have [PermissionSet.Permission.KICK_MEMBERS]
         */
        @Throws(PermissionException::class)
        fun kick(): CompletableFuture<Void>

        /**
         * Ban this user from the guild.
         *
         * @throws PermissionException if the user does not have [PermissionSet.Permission.BAN_MEMBERS]
         */
        @Throws(PermissionException::class)
        fun ban(clearLastXDays: Int? = null): CompletableFuture<DiscordId<User>>

        interface Edit : Editable.Transaction<Edit, Member> {
            /**
             * Change this set to add/remove roles from this user.
             */
            var roles: MutableList<Role>
        }

        enum class Status {
            ONLINE,
            IDLE,
            OFFLINE
        }
    }
}