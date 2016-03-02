package net.serverpeon.discord.internal.ws.client

import com.google.gson.Gson
import net.serverpeon.discord.internal.createLogger
import net.serverpeon.discord.internal.kDebug
import net.serverpeon.discord.internal.kTrace
import net.serverpeon.discord.internal.toObservable
import net.serverpeon.discord.internal.ws.data.inbound.Misc
import net.serverpeon.discord.internal.ws.data.outbound.ConnectMsg
import net.serverpeon.discord.internal.ws.data.outbound.KeepaliveMsg
import org.glassfish.tyrus.client.ClientManager
import rx.Observable
import rx.Subscriber
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import java.net.URI
import java.util.concurrent.TimeUnit

object DiscordWebsocket {
    private val client: ClientManager by lazy { ClientManager.createClient() }
    private val logger = createLogger()

    fun create(connectMsg: ConnectMsg, socketUrl: URI, gson: Gson): Observable<EventWrapper> {
        return Observable.create<EventWrapper> { sub ->
            val connectableRx = openWebsocketStream(BehaviorSubject.create(socketUrl), gson).publish()

            // StartEvent is the first event
            connectableRx.first().flatMap { startEvent ->
                check(startEvent.event === DiscordEndpoint.StartEvent)

                startEvent.respond(gson.toJson(connectMsg.toPayload())).toObservable()
            }.connectTo(sub)

            // ReadyEvent is the second event
            connectableRx.skip(1).first().flatMap {
                check(it.event is Misc.Ready)
                initKeepAlive(it, gson)
            }.connectTo(sub)

            sub.add(connectableRx.subscribe(sub)) // Event passthrough to upper subscriber

            // Finally begin emitting from the base observable
            connectableRx.connect { subscription ->
                logger.kDebug { "Event stream initialized" }
                sub.add(subscription)
            }
        }
    }

    private fun initKeepAlive(e: EventWrapper, gson: Gson): Observable<Void> {
        val timeout = (e.event as Misc.Ready).data.heartbeat_interval
        logger.kTrace { "Sending keep alive every ${timeout}ms" }
        return Observable.interval(timeout, TimeUnit.MILLISECONDS).flatMap {
            e.respond(gson.toJson(KeepaliveMsg.toPayload())).toObservable()
        }
    }

    private fun <T> Observable<T>.connectTo(sub: Subscriber<*>) {
        sub.add(this.doOnError { sub.onError(it) }.subscribe())
    }

    private fun openWebsocketStream(urlObservable: BehaviorSubject<URI>, gson: Gson): Observable<EventWrapper> {
        return Observable.create { sub ->
            val endpoint = DiscordEndpoint(DiscordHandlers.create(gson), sub)
            urlObservable.map { socketUrl ->
                client.asyncConnectToServer(endpoint, socketUrl)
            }.flatMap { openFuture ->
                Observable.from(openFuture).subscribeOn(Schedulers.newThread())
            }.connectTo(sub)
        }
    }
}