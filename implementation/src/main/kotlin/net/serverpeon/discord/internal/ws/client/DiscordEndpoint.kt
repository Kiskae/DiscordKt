package net.serverpeon.discord.internal.ws.client

import rx.Subscriber
import rx.subscriptions.Subscriptions
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.util.zip.InflaterInputStream
import javax.websocket.*

internal class DiscordEndpoint(val translator: MessageTranslator, val subscriber: Subscriber<in Event>) : Endpoint() {
    override fun onOpen(session: Session, config: EndpointConfig) {
        // Allow closing the connection with Rx
        subscriber.add(Subscriptions.create {
            session.close(
                    CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Subscriber.unsubscribe() called")
            )
        })

        // Indicate we're about to start sending messages
        subscriber.onNext(Event(session, StartEvent))

        // Add message forwarder
        session.addMessageHandler(TextHandler(session))
        session.addMessageHandler(BinaryHandler(session))
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
                subscriber.onNext(Event(session, it))
            }
        }
    }

    object StartEvent

    override fun onClose(session: Session, closeReason: CloseReason) {
        if (closeReason.closeCode != CloseReason.CloseCodes.NORMAL_CLOSURE) {
            subscriber.onError(IllegalStateException(closeReason.reasonPhrase))
        } else {
            subscriber.onCompleted()
        }
    }

    override fun onError(session: Session, thr: Throwable) {
        subscriber.onError(thr)
    }
}