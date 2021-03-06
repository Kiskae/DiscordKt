package net.serverpeon.discord.internal.ws

import net.serverpeon.discord.internal.loggerFor
import rx.functions.Func2
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class RetryHandler(private val maxRetries: Int = 3) : Func2<Int, Throwable, Boolean> {
    private val logger = loggerFor<RetryHandler>()
    private val retriesBase = AtomicInteger(-1)
    private val firstException = AtomicReference<Throwable>(null)
    val originalException: Throwable
        get() = firstException.get() ?: throw IllegalStateException("Exception inception")

    override fun call(retries: Int, throwable: Throwable): Boolean {
        if (firstException.get() == null) {
            retriesBase.set(retries)
            firstException.set(throwable)
            logger.debug(throwable) { "Starting retries, original exception:" }
        }

        val retry = retries - retriesBase.get()
        logger.debug { "Retry [$retry]" }

        // Stop retrying once we've tried more than maxRetries
        return retry >= maxRetries
    }

    fun reset() {
        firstException.set(null)
    }
}