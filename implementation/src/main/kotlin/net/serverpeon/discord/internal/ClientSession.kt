package net.serverpeon.discord.internal

import com.google.common.eventbus.EventBus
import com.google.gson.Gson
import com.jakewharton.rxrelay.BehaviorRelay
import net.serverpeon.discord.DiscordClient
import net.serverpeon.discord.internal.rest.retro.ApiWrapper
import net.serverpeon.discord.internal.rest.retro.Auth
import net.serverpeon.discord.internal.rest.rxObservable
import net.serverpeon.discord.internal.ws.client.DiscordWebsocket
import net.serverpeon.discord.internal.ws.client.Event
import net.serverpeon.discord.internal.ws.data.outbound.ConnectMsg
import net.serverpeon.discord.internal.ws.data.toObservable
import net.serverpeon.discord.model.Guild
import rx.Completable
import rx.Observable
import rx.Single
import rx.observables.ConnectableObservable
import java.util.concurrent.CompletableFuture

class ClientSession(apiSource: Single<ApiWrapper>,
                    gson: Gson,
                    private val eventBus: EventBus,
                    metadata: DiscordClient.Builder.UserMetadata) : DiscordClient {
    companion object {
        private const val DISCORD_API_VERSION = 3
    }

    private val closeFuture: CompletableFuture<Void> = CompletableFuture()
    private val apiWrapper: Observable<ApiWrapper> = BehaviorRelay.create<ApiWrapper>().apply {
        apiSource.subscribe(this)
    }.first()
    private val eventStream: ConnectableObservable<Event> = apiWrapper.flatMap {
        it.Gateway.wsEndpoint().rxObservable()
    }.flatMap { endPoint ->
        apiWrapper.flatMap { apiWrapper ->
            DiscordWebsocket.create(
                    ConnectMsg(
                            token = apiWrapper.token!!,
                            v = DISCORD_API_VERSION,
                            properties = ConnectMsg.Properties(
                                    operatingSystem = metadata.operatingSystem,
                                    device = metadata.device,
                                    browser = metadata.userAgent,
                                    referrer = "",
                                    referrerDomain = ""
                            ),
                            large_threshold = 100,
                            compress = true
                    ),
                    endPoint.url,
                    gson
            )
        }
    }.publish()

    override fun guilds(): Observable<Guild> {
        throw UnsupportedOperationException()
    }

    override fun eventBus(): EventBus {
        return eventBus
    }

    override fun logout(): Completable {
        return internalShutdown().andThen(apiWrapper).map {
            check(it.token != null) { "logout() called twice" }
            it.Auth.logout(Auth.LogoutRequest(it.token!!)).rxObservable().doOnCompleted {
                it.token = null // Clear expired token
            }
        }.toCompletable()
    }

    override fun close() {
        // Block until shutdown
        internalShutdown().await()
    }

    override fun closeAsync(): Completable {
        return internalShutdown()
    }

    override fun closeFuture(): Completable {
        return closeFuture.toObservable().toCompletable()
    }

    private fun internalShutdown(): Completable {
        //TODO: set atomicboolean to ensure it cannot be used afterwards
        //TODO: add system to shut down active event stream
        return Completable.complete()
    }
}