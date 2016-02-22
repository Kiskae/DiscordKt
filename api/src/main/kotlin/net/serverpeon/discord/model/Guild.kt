package net.serverpeon.discord.model

import rx.Observable
import java.time.ZonedDateTime

interface Guild : DiscordId.Identifiable<Guild> {

    interface Member : User {
        val joinedAt: ZonedDateTime
        val roles: Observable<Role>
    }
}