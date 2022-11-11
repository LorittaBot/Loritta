package net.perfectdreams.loritta.deviousfun.utils

import it.unimi.dsi.fastutil.longs.Long2ObjectMaps
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.perfectdreams.loritta.deviouscache.data.LightweightSnowflake

class SnowflakeMap<T>(val expected: Int) {
    val backedMap = Long2ObjectMaps.synchronize(Long2ObjectOpenHashMap<T>(expected))
    val size
        get() = backedMap.size
    val keys
        get() = backedMap.keys
    val values
        get() = backedMap.values

    fun forEach(consumer: (LightweightSnowflake, T) -> Unit) = Long2ObjectMaps.fastForEach(backedMap) {
        consumer.invoke(LightweightSnowflake(it.longKey), it.value)
    }

    operator fun iterator() = SnowflakeMapIterator(this)

    operator fun get(snowflake: LightweightSnowflake): T? = backedMap[snowflake.value.toLong()]

    operator fun set(snowflake: LightweightSnowflake, value: T) {
        backedMap.put(snowflake.value.toLong(), value)
    }

    fun remove(snowflake: LightweightSnowflake) = backedMap.remove(snowflake.value.toLong())

    fun putAll(values: Map<LightweightSnowflake, T>) {
        backedMap.putAll(values.mapKeys { it.key.value.toLong() })
    }

    fun containsKey(id: LightweightSnowflake) = backedMap.containsKey(id.value.toLong())

    fun getOrPut(id: LightweightSnowflake, defaultValue: () -> T) = backedMap.getOrPut(id.value.toLong(), defaultValue)

    fun clear() = backedMap.clear()

    fun toMap() = backedMap.toMap().mapKeys { LightweightSnowflake(it.key) }

    class SnowflakeMapIterator<T>(val snowflakeMap: SnowflakeMap<T>) : Iterator<Pair<LightweightSnowflake, T>> {
        val iterator = snowflakeMap.backedMap.iterator()

        override fun hasNext() = iterator.hasNext()

        override fun next(): Pair<LightweightSnowflake, T> {
            val next = iterator.next()
            return Pair(
                LightweightSnowflake(next.key),
                next.value
            )
        }
    }
}

fun <T> SnowflakeMap(map: Map<LightweightSnowflake, T>): SnowflakeMap<T> {
    val snowflakeMap = SnowflakeMap<T>(map.size)
    snowflakeMap.putAll(map)
    return snowflakeMap
}