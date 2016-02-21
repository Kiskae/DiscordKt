package net.serverpeon.discord

import net.serverpeon.discord.model.Guild
import rx.Observable
import java.util.*

interface DiscordClient : AutoCloseable {
    fun guilds(): Observable<Guild>

    companion object {
        private val builderLoader = ServiceLoader.load(DiscordClient.Builder::class.java)

        fun newBuilder(): DiscordClient.Builder {
            return builderLoader.firstOrNull() ?: throw IllegalStateException("DiscordClient implementation not found")
        }
    }

    interface Builder {
        //TODO: auth/token provide
        //TODO: eventBus for event publication
        fun build(): DiscordClient
    }
}