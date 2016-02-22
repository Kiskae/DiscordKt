package net.serverpeon.discord.internal

import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates

class DoOnce<T : Any>(private val action: () -> T) {
    private final val initialized = AtomicBoolean(false)
    private final var innerValue: T by Delegates.notNull<T>()

    val invoked: Boolean
        get() = initialized.get()

    fun getOrInit(): T {
        if (initialized.compareAndSet(false, true)) {
            innerValue = action()
        }
        return innerValue
    }
}