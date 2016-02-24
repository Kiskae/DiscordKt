package net.serverpeon.discord.model

import rx.Observable
import java.time.ZonedDateTime

interface Guild : DiscordId.Identifiable<Guild> {
    val channels: Observable<Channel.Public>

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