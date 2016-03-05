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
import net.serverpeon.discord.internal.ws.data.outbound.RequestMembersMsg
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.subjects.BehaviorSubject
import rx.subscriptions.Subscriptions
import java.net.URI
import java.util.concurrent.TimeUnit
import javax.websocket.Session

object DiscordWebsocket {
    private val logger = createLogger()

    object MembersReady

    fun create(connectMsg: ConnectMsg, socketUrl: URI, gson: Gson): Observable<EventWrapper> {
        return startEventStream(connectMsg, socketUrl, DiscordHandlers.create(gson), gson)
    }

    private fun startEventStream(
            connectMsg: ConnectMsg,
            socketUrl: URI,
            translator: MessageTranslator,
            gson: Gson
    ): Observable<EventWrapper> {
        return Observable.defer {
            val replacementEventStreams = BehaviorSubject.create<Observable<EventWrapper>>()

            Observable.create<EventWrapper> { sub ->
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

                    initKeepAlive(ready.data.heartbeat_interval, it.session, gson, sub)

                    // If we're part of any 'large' guilds, then request the full member list
                    val largeGuilds = ready.data.guilds.filter { it.large }
                    if (largeGuilds.isNotEmpty()) {
                        // Set up delayed event that'll signal when members are loaded.
                        val totalChunks = largeGuilds.map { Math.ceil(it.member_count / 1000.0).toInt() }.sum()
                        sub.add(emitMembersWhenReady(it.session, totalChunks, eventSource, sub))

                        val requestMembersObservable = it.session.send(gson.toJson(RequestMembersMsg(
                                largeGuilds.map { it.id }
                        ).toPayload())).toObservable()

                        sub.add(requestMembersObservable.doOnError {
                            sub.onError(it)
                        }.subscribe())
                    } else {
                        // No members to retrieve, emit ready
                        sub.onNext(EventWrapper(it.session, MembersReady))
                    }

                    // Set up reconnect listener
                    setupReconnectBehaviour(
                            ready.data.session_id, eventSource,
                            replacementEventStreams, translator, gson
                    )
                }.subscribe()

                eventSource.connect { subscription ->
                    logger.kDebug { "Initial event stream established" }
                    sub.add(subscription)
                }
            }.concatWith(Observable.concat(replacementEventStreams))
        }
    }

    private fun emitMembersWhenReady(session: Session,
                                     totalExpectedChunks: Int,
                                     eventSource: Observable<EventWrapper>,
                                     emitTo: Subscriber<in EventWrapper>): Subscription {
        logger.kDebug { "Expecting $totalExpectedChunks MEMBERS_CHUNK events" }

        // Listen for MembersChunk events, count until [totalExpectedChunks] and complete
        val lastExpectedChunkEvent = eventSource.filter {
            it.event is Misc.MembersChunk
        }.take(totalExpectedChunks).ignoreElements()
        // Always emit after 30 seconds
        val reasonableTimeout = Observable.timer(30, TimeUnit.SECONDS).ignoreElements()

        return Observable.amb(lastExpectedChunkEvent, reasonableTimeout).doOnCompleted {
            logger.kDebug { "Members fully loaded" }
            emitTo.onNext(EventWrapper(session, MembersReady))
        }.subscribe()
    }

    private fun reconnectEventStream(
            sessionId: String,
            sequenceNumber: Int,
            socketUrl: URI,
            translator: MessageTranslator,
            gson: Gson,
            replacementEventStreams: BehaviorSubject<Observable<EventWrapper>>
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

            // Listen for resume event and re-init the heartbeat
            eventSource.skip(1).first().doOnNext {
                val resumed = it.event as Misc.Resumed

                initKeepAlive(resumed.heartbeat_interval, it.session, gson, sub)
            }

            // Set up reconnect listener again
            setupReconnectBehaviour(sessionId, eventSource, replacementEventStreams, translator, gson)

            eventSource.connect { subscription ->
                logger.kDebug { "Resume event stream established" }
                sub.add(subscription)
            }
        }
    }

    private fun setupReconnectBehaviour(sessionId: String,
                                        currentEventSource: Observable<EventWrapper>,
                                        replacementEventStreams: BehaviorSubject<Observable<EventWrapper>>,
                                        translator: MessageTranslator,
                                        gson: Gson
    ): Subscription {
        return currentEventSource.first {
            it.event is ReconnectCommand
        }.doOnNext { ev ->
            val reconnect = ev.event as ReconnectCommand

            // This closes the previous event-stream as well.
            ev.session.close()

            //Generate and publish new event stream.
            replacementEventStreams.onNext(reconnectEventStream(
                    sessionId,
                    reconnect.sequence!!,
                    reconnect.url,
                    translator,
                    gson,
                    replacementEventStreams
            ))
        }.subscribe()
    }

    private fun initKeepAlive(timeout: Long,
                              session: Session,
                              gson: Gson,
                              sub: Subscriber<*>) {
        logger.kDebug { "Sending keep alive every ${timeout}ms" }

        // Create KeepAlive thread separately from parent observable
        //  should hopefully sever the root of Misc.Ready
        val keepAlive = Observable.interval(timeout, TimeUnit.MILLISECONDS).flatMap {
            session.send(gson.toJson(KeepaliveMsg.toPayload())).toObservable()
        }

        sub.add(Subscriptions.create {
            logger.kDebug { "Should shut down keepalive now" }
        })

        // If keep-alive errors pass to parent, also unsubscribe when parent does.
        sub.add(keepAlive.doOnError {
            sub.onError(it)
        }.subscribe())
    }
}