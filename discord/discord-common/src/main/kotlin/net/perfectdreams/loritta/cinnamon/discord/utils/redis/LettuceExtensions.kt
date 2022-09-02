package net.perfectdreams.loritta.cinnamon.discord.utils.redis

import io.lettuce.core.KeyValue
import io.lettuce.core.Value
import io.lettuce.core.api.sync.RedisHashCommands

val <T> Value<T>.valueOrNull: T?
    get() = this.getValueOrElse(null)

fun <K, V> List<KeyValue<K, V>>.toMap() = this.associate {
    it.key to it
}

fun <K, V> RedisHashCommands<K, V>.hsetIfMapNotEmpty(key: K, map: Map<K, V>) {
    if (map.isNotEmpty())
        hset(key, map)
}