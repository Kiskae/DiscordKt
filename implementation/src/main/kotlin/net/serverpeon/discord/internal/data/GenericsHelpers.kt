package net.serverpeon.discord.internal.data

import com.google.common.collect.ImmutableMap
import com.jakewharton.rxrelay.BehaviorRelay
import net.serverpeon.discord.model.DiscordId

internal fun <T : DiscordId.Identifiable<T>, G : T> createIdMapRelay(): BehaviorRelay<Map<DiscordId<T>, G>> {
    return BehaviorRelay.create()
}

internal fun <T : DiscordId.Identifiable<T>, G : T> createEmptyMap(): Map<DiscordId<T>, G> {
    return ImmutableMap.of()
}

internal fun <T : DiscordId.Identifiable<T>, G : T> Iterable<G>.toImmutableIdMap(): Map<DiscordId<T>, G> {
    return ImmutableMap.builder<DiscordId<T>, G>().apply {
        forEach { put(it.id, it) } // Associate id to value
    }.build()
}

internal fun <K, V> Map<K, V>.immutableAdd(key: K, value: V): Map<K, V> {
    return ImmutableMap.builder<K, V>().putAll(this).put(key, value).build()
}

internal fun <K, V> Map<K, V>.immutableRemove(key: K): Map<K, V> {
    return ImmutableMap.builder<K, V>().apply {
        for (e in entries) {
            if (e.key != key) {
                put(e)
            }
        }
    }.build()
}

internal fun <K, V> combineMaps(vararg maps: Map<K, V>): Map<K, V> {
    return ImmutableMap.builder<K, V>().apply {
        maps.forEach { putAll(it) }
    }.build()
}