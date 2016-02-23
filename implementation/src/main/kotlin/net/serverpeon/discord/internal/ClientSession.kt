package net.serverpeon.discord.internal

import com.google.common.eventbus.EventBus
import com.google.gson.Gson
import com.jakewharton.rxrelay.BehaviorRelay
import net.serverpeon.discord.DiscordClient
import net.serverpeon.discord.internal.data.DiscordNode
import net.serverpeon.discord.internal.rest.retro.ApiWrapper
import net.serverpeon.discord.internal.rest.retro.Auth
import net.serverpeon.discord.internal.rest.rxObservable
import net.serverpeon.discord.internal.ws.RetryHandler
import net.serverpeon.discord.internal.ws.client.DiscordWebsocket
import net.serverpeon.discord.internal.ws.client.Event
import net.serverpeon.discord.internal.ws.data.inbound.Misc
import net.serverpeon.discord.internal.ws.data.outbound.ConnectMsg
import net.serverpeon.discord.internal.ws.data.toObservable
import net.serverpeon.discord.model.Channel
import net.serverpeon.discord.model.DiscordId
import net.serverpeon.discord.model.Guild
import net.serverpeon.discord.model.User
import rx.Completable
import rx.Observable
import rx.Single
import rx.Subscription
import rx.observables.ConnectableObservable
import rx.subjects.BehaviorSubject
import java.util.concurrent.CompletableFuture
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ClientSession(apiSource: Single<ApiWrapper>,
                    gson: Gson,
                    private val eventBus: EventBus,
                    metadata: DiscordClient.Builder.UserMetadata,
                    private val retryHandler: RetryHandler) : DiscordClient {
    companion object {
        private const val DISCORD_API_VERSION = 3
    }

    private val apiWrapper: Observable<ApiWrapper> = BehaviorSubject.create<ApiWrapper>().apply {
        // Pass through the value and any potential errors, but not the complete() (Shuts down subject)
        apiSource.subscribe({
            onNext(it)
        }, {
            onError(it)
        })
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
            ).retry(retryHandler).onErrorResumeNext {
                // This will ensure the original exception is thrown if retries fail
                Observable.error(retryHandler.originalException)
            }
        }
    }.publish()

    //Fields for tracking the current state of the client
    private val closeFuture: CompletableFuture<Void> = CompletableFuture()
    private val eventStreamSubscription: DoOnce<Subscription> = DoOnce {
        eventStream.connect()
    }
    private val eventListener: DoOnce<EventPublisher> = DoOnce {
        EventPublisher(eventBus).apply {
            eventStream.flatMap { event ->
                model.map { model ->
                    this.call(event, model)
                }
            }.subscribe()
        }
    }
    private val sessionLock: Lock = ReentrantLock()

    private val model = BehaviorRelay.create<DiscordNode>().apply {
        // Set up connection to the eventStream for updates to the model
        eventStream.map { it.event }.filter {
            // Only pass events through to the model
            it is net.serverpeon.discord.internal.ws.data.inbound.Event
        }.flatMap { event ->
            if (event is Misc.Ready) {
                // If we receive a Ready event, generate a new model
                apiWrapper.map { api ->
                    val newModel = DiscordNode.from(event.data, api)
                    this.call(newModel) // Update the model
                    newModel
                }
            } else {
                // Otherwise we use the current version of the model
                this
            }.map { model ->
                // Pass the event to the model
                model.visit(event as net.serverpeon.discord.internal.ws.data.inbound.Event)
            }
        }.subscribe()
    }.first()

    init {
        // We consider receiving the Ready event an indicator that the connection was successful
        // So we reset the retry handler.
        eventStream.filter {
            it.event is Misc.Ready
        }.subscribe({
            retryHandler.reset()
        }, {
            // Fail the closeFuture if a failure manages to make it through the event stream
            closeFuture.completeExceptionally(it)
        }, {
            // If the event stream completes, just complete the closeFuture as well
            closeFuture.complete(null)
        })

        // If the event-stream hasn't been initialized this is also a failure condition
        apiWrapper.doOnError { closeFuture.completeExceptionally(it) }.subscribe()
    }

    override fun guilds(): Observable<Guild> {
        return ensureSafeModelAccess().andThen(model).flatMap { it.guilds }
    }

    fun repr(): Single<String> {
        return ensureSafeModelAccess().andThen(model).map { it.toString() }.toSingle()
    }

    override fun getGuildById(id: DiscordId<Guild>): Observable<Guild> {
        return ensureSafeModelAccess().andThen(model).flatMap {
            it.getGuildById(id)
        }
    }

    override fun getUserById(id: DiscordId<User>): Observable<User> {
        return ensureSafeModelAccess().andThen(model).flatMap {
            it.getUserById(id)
        }
    }

    override fun privateChannels(): Observable<Channel.Private> {
        return ensureSafeModelAccess().andThen(model).flatMap {
            it.privateChannels
        }
    }

    override fun getChannelById(id: DiscordId<Channel>): Observable<Channel> {
        return ensureSafeModelAccess().andThen(model).flatMap {
            it.getChannelById(id)
        }
    }

    override fun getPrivateChannelById(id: DiscordId<Channel>): Observable<Channel.Private> {
        return getChannelById(id).filter { it.isPrivate }.cast(Channel.Private::class.java)
    }

    override fun eventBus(): EventBus {
        return eventBus
    }

    override fun startEmittingEvents() {
        sessionLock.withLock {
            if (!closeFuture.isDone) {
                eventListener.getOrInit()
                // Activate the event stream
                eventStreamSubscription.getOrInit()
            }
        }
    }

    // Ensures the eventStream is initialized so we have access to the model
    // Failure conditions will result in a failed completable
    private fun ensureSafeModelAccess(): Completable {
        return Completable.defer {
            sessionLock.withLock {
                if (!closeFuture.isDone) {
                    eventStreamSubscription.getOrInit() //Ensure the event-stream is connected
                    Completable.complete()
                } else {
                    closeFuture().doOnTerminate { throw DiscordClient.AccessAfterCloseException() }
                }
            }
        }
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

    private fun internalShutdown(): Completable {
        return sessionLock.withLock {
            if (!closeFuture.isDone) {
                closeFuture.complete(null)
                if (eventStreamSubscription.invoked) {
                    // Unsubscribe closes the underlying websocket
                    eventStreamSubscription.getOrInit().unsubscribe()
                }
            }

            // Refer to closeFuture() for the final result
            closeFuture()
        }
    }

    override fun closeFuture(): Completable {
        return closeFuture.toObservable().toCompletable()
    }
}