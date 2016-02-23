package net.serverpeon.discord.internal.data

import com.google.common.collect.ImmutableMap
import com.jakewharton.rxrelay.BehaviorRelay
import net.serverpeon.discord.model.DiscordId
import rx.Observable

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

internal fun <K, V> Map<K, V>.immutableRemoveKeys(keys: Set<K>): Map<K, V> {
    return ImmutableMap.builder<K, V>().apply {
        for (e in entries) {
            if (e.key !in keys) {
                put(e)
            }
        }
    }.build()
}

inline fun <T : DiscordId.Identifiable<T>> observableLookup(
        id: DiscordId<T>,
        crossinline lookup: (DiscordId<T>) -> T?
): Observable<T> {
    return Observable.defer {
        lookup(id)?.let { Observable.just(it) } ?: Observable.empty()
    }
}

inline fun <T : DiscordId.Identifiable<T>> observableList(crossinline provider: () -> Iterable<T>): Observable<T> {
    return Observable.defer {
        Observable.from(provider())
    }
}

internal fun <K, V> combineMaps(vararg maps: Map<K, V>): Map<K, V> {
    return ImmutableMap.builder<K, V>().apply {
        maps.forEach { putAll(it) }
    }.build()
}