package net.serverpeon.discord.internal.ws.client

import net.serverpeon.discord.internal.loggerFor
import org.glassfish.tyrus.client.ClientManager
import rx.Observable
import rx.Subscriber
import rx.exceptions.Exceptions
import rx.schedulers.Schedulers
import rx.subscriptions.Subscriptions
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.net.URI
import java.util.zip.InflaterInputStream
import javax.websocket.*

internal class DiscordEndpoint private constructor(
        val translator: MessageTranslator,
        val subscriber: Subscriber<in EventWrapper>
) : Endpoint() {
    private val logger = loggerFor<DiscordEndpoint>()

    override fun onOpen(session: Session, config: EndpointConfig) {
        // Add message forwarder
        session.addMessageHandler(TextHandler(session))
        session.addMessageHandler(BinaryHandler(session))

        // Allow closing the connection with Rx
        subscriber.add(Subscriptions.create {
            logger.trace { "Closing websocket session due to Rx.unsubscribe()" }
            session.close(
                    CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Subscriber.unsubscribe() called")
            )
        })

        // Indicate we're about to start sending messages
        subscriber.onNext(EventWrapper(session, StartEvent))
    }

    private inner class TextHandler(val session: Session) : MessageHandler.Whole<Reader> {
        override fun onMessage(reader: Reader) = handleEvent(reader, session)
    }

    private inner class BinaryHandler(val session: Session) : MessageHandler.Whole<InputStream> {
        override fun onMessage(src: InputStream) = handleEvent(InputStreamReader(InflaterInputStream(src)), session)
    }

    private fun handleEvent(reader: Reader, session: Session) {
        if (!subscriber.isUnsubscribed) {
            translator.translate(reader)?.let {
                try {
                    subscriber.onNext(EventWrapper(session, it))
                } catch (ex: Exception) {
                    Exceptions.throwOrReport(ex, subscriber, it)
                }
            }
        }
    }

    object StartEvent

    override fun onClose(session: Session, closeReason: CloseReason) {
        logger.trace { "Endpoint[${session.requestURI}] closed: $closeReason" }

        if (subscriber.isUnsubscribed) return

        if (closeReason.closeCode != CloseReason.CloseCodes.NORMAL_CLOSURE) {
            subscriber.onError(IllegalStateException(closeReason.reasonPhrase))
        } else {
            subscriber.onCompleted()
        }
    }

    override fun onError(session: Session, thr: Throwable) {
        logger.trace(thr) { "Endpoint[${session.requestURI}] encountered an exception" }

        if (subscriber.isUnsubscribed) return

        subscriber.onError(thr)
    }

    companion object {
        private val client: ClientManager by lazy { ClientManager.createClient() }

        fun create(translator: MessageTranslator, socketUrl: URI): Observable<EventWrapper> {
            return Observable.create<EventWrapper> { sub ->
                // Create endpoint which will push events to the subscriber
                val endpoint = DiscordEndpoint(translator, sub)

                // Connect to the discord server.
                val sessionFuture = client.asyncConnectToServer(endpoint, socketUrl)

                // Ensure an error during creation is passed to the subscriber
                //  also ensure the future is correctly cancelled if the subscriber unsubscribes
                sub.add(Observable.from(sessionFuture)
                        .subscribeOn(Schedulers.newThread())
                        .doOnError { sub.onError(it) }
                        .subscribe())
            }
        }
    }
}