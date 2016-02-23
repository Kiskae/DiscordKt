package net.serverpeon.discord.model

import rx.Observable
import java.time.ZonedDateTime

interface Guild : DiscordId.Identifiable<Guild> {
    val channels: Observable<Channel>
    fun getChannelById(id: DiscordId<Channel>): Observable<Channel>

    interface Member : User {
        val joinedAt: ZonedDateTime
        val roles: Observable<Role>

        //TODO: lastState (enum)
        //TODO: currentGame (String?)
    }
}