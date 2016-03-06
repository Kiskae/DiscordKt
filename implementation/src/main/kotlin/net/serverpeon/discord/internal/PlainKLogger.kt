package net.serverpeon.discord.internal

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.Marker
import org.slf4j.MarkerFactory
import kotlin.reflect.KClass

interface KLogger {
    fun error(th: Throwable? = null, msg: () -> String)

    fun warn(th: Throwable? = null, msg: () -> String)

    fun info(th: Throwable? = null, msg: () -> String)

    fun debug(th: Throwable? = null, msg: () -> String)

    fun trace(th: Throwable? = null, msg: () -> String)

    fun marked(markerId: String): KLogger
}

class PlainKLogger private constructor(private val logger: Logger) : KLogger {
    override fun error(th: Throwable?, msg: () -> String) {
        if (logger.isErrorEnabled) {
            logger.error(msg(), th)
        }
    }

    override fun warn(th: Throwable?, msg: () -> String) {
        if (logger.isWarnEnabled) {
            logger.warn(msg(), th)
        }
    }

    override fun info(th: Throwable?, msg: () -> String) {
        if (logger.isInfoEnabled) {
            logger.info(msg(), th)
        }
    }

    override fun debug(th: Throwable?, msg: () -> String) {
        if (logger.isDebugEnabled) {
            logger.debug(msg(), th)
        }
    }

    override fun trace(th: Throwable?, msg: () -> String) {
        if (logger.isTraceEnabled) {
            logger.trace(msg(), th)
        }
    }

    override fun marked(markerId: String): KLogger {
        return Marked(MarkerFactory.getMarker(markerId))
    }

    private inner class Marked(private val marker: Marker) : KLogger {
        override fun error(th: Throwable?, msg: () -> String) {
            if (logger.isErrorEnabled) {
                logger.error(marker, msg(), th)
            }
        }

        override fun warn(th: Throwable?, msg: () -> String) {
            if (logger.isWarnEnabled) {
                logger.warn(marker, msg(), th)
            }
        }

        override fun info(th: Throwable?, msg: () -> String) {
            if (logger.isInfoEnabled) {
                logger.info(marker, msg(), th)
            }
        }

        override fun debug(th: Throwable?, msg: () -> String) {
            if (logger.isDebugEnabled) {
                logger.debug(marker, msg(), th)
            }
        }

        override fun trace(th: Throwable?, msg: () -> String) {
            if (logger.isTraceEnabled) {
                logger.trace(marker, msg(), th)
            }
        }

        override fun marked(markerId: String): KLogger {
            return Marked(MarkerFactory.getMarker(markerId).apply {
                add(marker)
            })
        }
    }

    companion object {
        fun create(clazz: KClass<*>): KLogger {
            return PlainKLogger(LoggerFactory.getLogger(clazz.java))
        }
    }
}

inline fun <reified T : Any> loggerFor(): KLogger {
    return PlainKLogger.create(T::class)
}