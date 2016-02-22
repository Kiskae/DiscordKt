package net.serverpeon.discord.internal

import com.google.common.eventbus.EventBus
import net.serverpeon.discord.internal.ws.client.Event
import rx.functions.Action1

internal class EventPublisher(val eventBus: EventBus) : Action1<Event> {
    override fun call(event: Event) {
        throw UnsupportedOperationException()
    }
}