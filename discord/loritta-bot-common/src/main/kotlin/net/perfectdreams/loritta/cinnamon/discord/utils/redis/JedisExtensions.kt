package net.perfectdreams.loritta.cinnamon.discord.utils.redis

import redis.clients.jedis.Jedis
import redis.clients.jedis.TransactionBase

fun TransactionBase.hsetOrDelIfMapIsEmpty(key: String, map: Map<String, String>) {
    if (map.isNotEmpty())
        hset(key, map)
    else
        del(key)
}

fun Jedis.hsetOrDelIfMapIsEmpty(key: String, map: Map<String, String>) {
    if (map.isNotEmpty())
        hset(key, map)
    else
        del(key)
}

fun Jedis.hsetByteArrayOrDelIfMapIsEmpty(key: String, map: Map<String, ByteArray>) {
    if (map.isNotEmpty())
        hsetByteArray(key, map)
    else
        del(key)
}

fun TransactionBase.hsetByteArrayOrDelIfMapIsEmpty(key: String, map: Map<String, ByteArray>) {
    if (map.isNotEmpty())
        hsetByteArray(key, map)
    else
        del(key)
}

fun Jedis.hsetByteArray(key: String, field: String, value: ByteArray) = hset(key.toByteArray(Charsets.UTF_8), field.toByteArray(Charsets.UTF_8), value)
fun Jedis.hsetByteArray(key: String, map: Map<String, ByteArray>) = hset(key.toByteArray(Charsets.UTF_8), map.mapKeys { it.key.toByteArray(Charsets.UTF_8) })

fun TransactionBase.hsetByteArray(key: String, field: String, value: ByteArray) = hset(key.toByteArray(Charsets.UTF_8), field.toByteArray(Charsets.UTF_8), value)
fun TransactionBase.hsetByteArray(key: String, map: Map<String, ByteArray>) = hset(key.toByteArray(Charsets.UTF_8), map.mapKeys { it.key.toByteArray(Charsets.UTF_8) })

fun Jedis.hgetByteArray(key: String, field: String) = hget(key.toByteArray(Charsets.UTF_8), field.toByteArray(Charsets.UTF_8))
fun Jedis.hgetAllByteArray(key: String) = hgetAll(key.toByteArray(Charsets.UTF_8)).mapKeys { it.key.toString(Charsets.UTF_8) }