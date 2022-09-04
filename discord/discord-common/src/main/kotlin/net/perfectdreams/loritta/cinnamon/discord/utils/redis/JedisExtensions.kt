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