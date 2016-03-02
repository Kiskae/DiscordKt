package net.serverpeon.discord.internal

import com.google.common.collect.ImmutableMap
import com.google.common.eventbus.EventBus
import net.serverpeon.discord.internal.data.model.DiscordNode
import net.serverpeon.discord.internal.ws.client.EventWrapper
import rx.Observable
import rx.functions.Action2
import rx.schedulers.Schedulers

internal class EventPublisher(val eventBus: EventBus) : Action2<EventWrapper, DiscordNode> {
    private val scheduler = Schedulers.computation()
    private val transformerMap: Map<Class<*>, (EventWrapper, DiscordNode) -> Observable<Any>> =
            ImmutableMap.builder<Class<*>, (EventWrapper, DiscordNode) -> Observable<Any>>().apply {

            }.build()

    override fun call(eventHolder: EventWrapper, model: DiscordNode) {
        transformerMap[eventHolder.event.javaClass]?.let {
            processEvent(it(eventHolder, model))
        }
    }

    private fun processEvent(eventData: Observable<*>) {
        eventData.subscribeOn(scheduler).subscribe { eventBus.post(it) }
    }
}