package net.serverpeon.discord

import com.google.common.eventbus.EventBus
import net.serverpeon.discord.model.Guild
import rx.Completable
import rx.Observable
import java.util.*

interface DiscordClient : AutoCloseable {
    fun guilds(): Observable<Guild>

    /**
     * Invalidates the internal session token and shuts down this client.
     *
     * After completion this client cannot be used to access Discord.
     */
    fun logout(): Completable

    companion object {
        private val builderLoader = ServiceLoader.load(DiscordClient.Builder::class.java)

        fun newBuilder(): DiscordClient.Builder {
            return builderLoader.firstOrNull() ?: throw IllegalStateException("DiscordClient implementation not found")
        }
    }

    interface Builder {
        /**
         * Use the specified login credentials to authenticate with Discord.
         *
         * These credentials will be used internally to authenticate with Discord and retrieve a session token.
         * This token will then be used for all future requests.
         *
         * @param email Email of a discord account
         * @param password Password of the discord account associated with the [email]
         * @return Fluent builder
         */
        fun login(email: String, password: String): Builder

        /**
         * Use a previously obtained session token for authentication.
         *
         * @param token Discord session token
         * @return Fluent builder
         */
        fun token(token: String): Builder

        fun eventBus(eventBus: EventBus): Builder

        fun build(): DiscordClient
    }
}