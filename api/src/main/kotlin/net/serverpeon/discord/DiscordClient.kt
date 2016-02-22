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
         * @param eventBus The [EventBus] to use for event publishing
         * @return Fluent builder
         */
        fun eventBus(eventBus: EventBus): Builder

        /**
         * Specify the metadata that should be sent to Discord with requests.
         *
         * @param metadata The desired metadata
         * @return Fluent builder
         */
        fun metadata(metadata: UserMetadata): Builder

        /**
         * Specify the number of times the client will attempt to reconnect after a disconnect.
         *
         * If the client successfully reconnects between retries then the try counter is reset.
         *
         * @param sequentialRetries Amount of times the client should attempt to reconnect.
         * @return Fluent builder
         */
        fun retries(sequentialRetries: Int): Builder

        /**
         * @property userAgent String identifying the user, defaults to "DiscordKt <Implementation Version>"
         * @property device String representing the type of device that is running the client, defaults to
         *                  `System.getProperty("os.arch")`
         * @property operatingSystem String representing the operating system version, defaults to
         *                           `"${System.getProperty("os.name")} ${System.getProperty("os.version")}"`
         */
        data class UserMetadata @JvmOverloads constructor(
                val userAgent: String,
                val device: String = System.getProperty("os.arch"),
                val operatingSystem: String = "${System.getProperty("os.name")} ${System.getProperty("os.version")}"
        )

        /**
         * Construct the client
         */
        fun build(): DiscordClient
    }
}