package net.serverpeon.discord.internal

import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified A : Any> A.createLogger(): Logger {
    return LoggerFactory.getLogger(A::class.java)
}

fun Logger.kInfo(th: Throwable? = null, msg: () -> String) {
    if (this.isInfoEnabled) {
        this.info(msg(), th)
    }
}

fun Logger.kDebug(th: Throwable? = null, msg: () -> String) {
    if (this.isDebugEnabled) {
        this.debug(msg(), th)
    }
}

fun Logger.kWarn(th: Throwable? = null, msg: () -> String) {
    if (this.isWarnEnabled) {
        this.warn(msg(), th)
    }
}

fun Logger.kError(th: Throwable? = null, msg: () -> String) {
    if (this.isErrorEnabled) {
        this.error(msg(), th)
    }
}

fun Logger.kTrace(th: Throwable? = null, msg: () -> String) {
    if (this.isTraceEnabled) {
        this.trace(msg(), th)
    }
}