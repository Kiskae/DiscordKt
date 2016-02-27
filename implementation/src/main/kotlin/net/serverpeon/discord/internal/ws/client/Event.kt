package net.serverpeon.discord.internal.ws.client

import net.serverpeon.discord.internal.createLogger
import net.serverpeon.discord.internal.kTrace
import java.io.IOException
import java.util.concurrent.CompletableFuture
import javax.websocket.Session

data class Event(private val session: Session, val event: Any) {
    companion object {
        private val logger = createLogger()
    }

    fun accessSession(): Session {
        return session;
    }

    fun respond(text: String): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        logger.kTrace { "Send: $text" }
        if (session.isOpen) {
            session.asyncRemote.sendText(text, { result ->
                if (result.isOK) {
                    future.complete(null)
                } else {
                    future.completeExceptionally(result.exception)
                }
            })
        } else {
            future.completeExceptionally(IOException("Trying to send to a closed socket."))
        }

        return future
    }
}