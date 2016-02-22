package net.serverpeon.discord.internal.data

import com.google.common.collect.ImmutableMap
import com.jakewharton.rxrelay.BehaviorRelay
import net.serverpeon.discord.model.DiscordId

internal fun <T : DiscordId.Identifiable<T>, G : T> createIdMapRelay(): BehaviorRelay<Map<DiscordId<T>, G>> {
    return BehaviorRelay.create()
}

internal fun <T : DiscordId.Identifiable<T>, G : T> Iterable<G>.toImmutableIdMap(): Map<DiscordId<T>, G> {
    return ImmutableMap.builder<DiscordId<T>, G>().apply {
        forEach { put(it.id, it) } // Associate id to value
    }.build()
}