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

    /**
     * Like [close], but does not block the current thread.
     */
    fun closeAsync(): Completable

    /**
     * Returns a completable that will resolve when the client eventually shuts down.
     * This does **NOT** close the client itself.
     *
     * Modelled after Netty's closeFuture() feature: [Channel#closeFuture()](http://netty.io/4.0/api/io/netty/channel/Channel.html#closeFuture())
     */
    fun closeFuture(): Completable

    /**
     * Returns the event bus on which this client will publish events.
     */
    fun eventBus(): EventBus

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

        /**
         * DiscordKt will emit events to the given EventBus instead of creating an EventBus itself.
         *
         * @property eventBus The [EventBus] to use for event publishing
         * @return Fluent builder
         */
        fun eventBus(eventBus: EventBus): Builder

        /**
         * Construct the client
         */
        fun build(): DiscordClient
    }
}