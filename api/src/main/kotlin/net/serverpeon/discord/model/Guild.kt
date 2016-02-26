package net.serverpeon.discord.model

import rx.Observable
import java.time.ZonedDateTime

/**
 * Discord represents individual servers as "Guilds"
 *
 * Each guild has a number of channels that can either be [Channel.Type.VOICE] or [Channel.Type.TEXT] channels.
 * They also contain a list of members with assigned permissions that can be queried on a channel-by-channel basis.
 */
interface Guild : DiscordId.Identifiable<Guild> {
    /**
     * Retrieves a list of all channels in this guild.
     */
    val channels: Observable<Channel.Public>

    /**
     *
     */
    fun getChannelById(id: DiscordId<Channel>): Observable<Channel.Public>

    fun getChannelByName(name: String): Observable<Channel.Public>

    val members: Observable<Member>

    fun getMemberById(id: DiscordId<User>): Observable<Member>

    fun getMemberByName(name: String): Observable<Member>

    interface Member : User, VoiceState {
        val guild: Guild

        val joinedAt: ZonedDateTime

        val roles: Observable<Role>

        val status: Status

        val currentGame: String?

        /**
         *
         */
        fun permissionsFor(channel: Channel.Public): PermissionSet {
            return channel.permissionsFor(this)
        }

        enum class Status {
            ONLINE,
            IDLE,
            OFFLINE,
            UNKNOWN
        }
    }
}