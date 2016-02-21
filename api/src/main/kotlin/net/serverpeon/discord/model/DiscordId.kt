package net.serverpeon.discord.model

import rx.Observable
import rx.Single

data class DiscordId<T : DiscordId.Identifiable<T>>(val id: String) {
    fun <A : T> findIn(source: Observable<A>): Single<A> {
        return source.first { it.id == this }.toSingle()
    }

    interface Identifiable<T : Identifiable<T>> {
        val id: DiscordId<T>
    }
}