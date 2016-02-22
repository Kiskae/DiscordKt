package net.serverpeon.discord.internal.data

import rx.functions.Action1

interface EventProcessor : Action1<Any> {
    override fun call(event: Any) = acceptEvent(event)

    fun acceptEvent(event: Any)
}