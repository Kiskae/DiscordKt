package net.serverpeon.discord.internal

import com.google.common.collect.ImmutableMap
import com.google.common.eventbus.EventBus
import net.serverpeon.discord.internal.data.DiscordNode
import net.serverpeon.discord.internal.ws.client.Event
import rx.Observable
import rx.functions.Action2
import rx.schedulers.Schedulers

internal class EventPublisher(val eventBus: EventBus) : Action2<Event, DiscordNode> {
    private val scheduler = Schedulers.computation()
    private val transformerMap: Map<Class<*>, (Event, DiscordNode) -> Observable<Any>> =
            ImmutableMap.builder<Class<*>, (Event, DiscordNode) -> Observable<Any>>().apply {

            }.build()

    override fun call(eventHolder: Event, model: DiscordNode) {
        transformerMap[eventHolder.event.javaClass]?.let {
            processEvent(it(eventHolder, model))
        }
    }

    private fun processEvent(eventData: Observable<*>) {
        eventData.subscribeOn(scheduler).subscribe { eventBus.post(it) }
    }
}