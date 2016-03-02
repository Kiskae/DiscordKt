package net.serverpeon.discord.internal.ws.client

import net.serverpeon.discord.internal.createLogger
import net.serverpeon.discord.internal.kTrace
import net.serverpeon.discord.internal.send
import java.util.concurrent.CompletableFuture
import javax.websocket.Session

data class EventWrapper(private val session: Session, val event: Any) {
    companion object {
        private val logger = createLogger()
    }

    fun accessSession(): Session {
        return session;
    }

    fun respond(text: String): CompletableFuture<Void> {
        logger.kTrace { "Send: $text" }
        return session.send(text)
    }
}