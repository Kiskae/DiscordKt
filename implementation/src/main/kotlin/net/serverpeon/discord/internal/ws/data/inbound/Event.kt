package net.serverpeon.discord.internal.ws.data.inbound

import net.serverpeon.discord.internal.data.EventInput

interface Event {
    interface RefHolder<T> {
        var value: T?
    }

    fun <T : EventInput<T>> accept(visitor: T, handler: EventInput.Handler<T>)
}