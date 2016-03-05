package net.serverpeon.discord.internal.ws.client

import com.google.gson.Gson
import net.serverpeon.discord.internal.createLogger
import net.serverpeon.discord.internal.kDebug
import net.serverpeon.discord.internal.send
import net.serverpeon.discord.internal.toObservable
import net.serverpeon.discord.internal.ws.data.inbound.Misc
import net.serverpeon.discord.internal.ws.data.outbound.ConnectMsg
import net.serverpeon.discord.internal.ws.data.outbound.KeepaliveMsg
import net.serverpeon.discord.internal.ws.data.outbound.ReconnectMsg
import rx.Observable
import java.net.URI
import java.util.concurrent.TimeUnit
import javax.websocket.Session

object DiscordWebsocket {
    private val logger = createLogger()

    fun create(connectMsg: ConnectMsg, socketUrl: URI, gson: Gson): Observable<EventWrapper> {
        return startEventStream(connectMsg, socketUrl, DiscordHandlers.create(gson), gson)
    }

    private fun startEventStream(
            connectMsg: ConnectMsg,
            socketUrl: URI,
            translator: MessageTranslator,
            gson: Gson
    ): Observable<EventWrapper> {
        return Observable.create { sub ->
            val eventSource = DiscordEndpoint.create(translator, socketUrl).publish()

            // Pass events and any errors from eventSource directly to parent
            eventSource.subscribe(sub)

            // StartEvent is the first event
            eventSource.first().flatMap { startEvent ->
                check(startEvent.event === DiscordEndpoint.StartEvent)

                startEvent.session.send(gson.toJson(connectMsg.toPayload())).toObservable()
            }.doOnError { sub.onError(it) }.subscribe()

            // ReadyEvent is the second event
            eventSource.skip(1).first().doOnNext {
                val ready = it.event as Misc.Ready

                ready.data.guilds.forEach { guild ->
                    logger.kDebug { "Member_count: ${guild.member_count}, Members_reported: ${guild.members.size}" }
                }

                // Create KeepAlive thread separately from parent observable
                //  should hopefully sever the root of Misc.Ready
                val keepAliveObservable = keepAlive(ready.data.heartbeat_interval, it.session, gson)

                // If keep-alive errors pass to parent, also unsubscribe when parent does.
                sub.add(keepAliveObservable.doOnError {
                    sub.onError(it)
                }.subscribe())
            }.subscribe()

            eventSource.connect { subscription ->
                logger.kDebug { "Initial event stream established" }
                sub.add(subscription)
            }
        }
    }

    private fun reconnectEventStream(
            sessionId: String,
            sequenceNumber: Int,
            socketUrl: URI,
            translator: MessageTranslator,
            gson: Gson
    ): Observable<EventWrapper> {
        return Observable.create { sub ->
            val eventSource = DiscordEndpoint.create(translator, socketUrl).publish()

            // Pass events and any errors from eventSource directly to parent
            eventSource.subscribe(sub)

            // Send the reconnect payload
            eventSource.first().flatMap { startEvent ->
                check(startEvent.event === DiscordEndpoint.StartEvent)

                startEvent.session.send(gson.toJson(ReconnectMsg(
                        sessionId,
                        sequenceNumber
                ).toPayload())).toObservable()
            }.doOnError { sub.onError(it) }.subscribe()

            // TODO: check for 'resumed' event & re-init keepalive

            eventSource.connect { subscription ->
                logger.kDebug { "Resume event stream established" }
                sub.add(subscription)
            }
        }
    }

    private fun keepAlive(timeout: Long,
                          session: Session,
                          gson: Gson): Observable<Void> {
        logger.kDebug { "Sending keep alive every ${timeout}ms" }
        return Observable.interval(timeout, TimeUnit.MILLISECONDS).flatMap {
            session.send(gson.toJson(KeepaliveMsg.toPayload())).toObservable()
        }
    }
}