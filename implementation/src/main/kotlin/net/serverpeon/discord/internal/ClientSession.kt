package net.serverpeon.discord.internal

import com.google.common.eventbus.EventBus
import com.google.gson.Gson
import com.jakewharton.rxrelay.BehaviorRelay
import net.serverpeon.discord.DiscordClient
import net.serverpeon.discord.internal.rest.retro.ApiWrapper
import net.serverpeon.discord.internal.rest.retro.Auth
import net.serverpeon.discord.internal.rest.rxObservable
import net.serverpeon.discord.internal.ws.client.Event
import net.serverpeon.discord.internal.ws.data.toObservable
import net.serverpeon.discord.model.Guild
import rx.Completable
import rx.Observable
import rx.Single
import rx.observables.ConnectableObservable
import java.util.concurrent.CompletableFuture

class ClientSession(apiSource: Single<ApiWrapper>, gson: Gson, eventBus: EventBus) : DiscordClient {
    private val closeFuture: CompletableFuture<Void> = CompletableFuture()
    private val apiWrapper: Observable<ApiWrapper> = BehaviorRelay.create { sub ->
        apiSource.subscribe(sub)
    }
    private val eventStream: ConnectableObservable<Event> = apiWrapper.flatMap {
        it.Gateway.wsEndpoint().rxObservable()
    }.flatMap { endPoint ->
        apiWrapper.flatMap { apiWrapper ->
            // DiscordWebsocket.create(
            // null, //TODO: set info
            // endPoint.url,
            // gson
            // ).retry() //TODO: implement retry policy
            Observable.never<Event>()
        }
    }.publish()

    override fun guilds(): Observable<Guild> {
        throw UnsupportedOperationException()
    }

    override fun eventBus(): EventBus {
        throw UnsupportedOperationException()
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