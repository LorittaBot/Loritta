package net.perfectdreams.loritta.cinnamon.discord.utils.redis

import redis.clients.jedis.TransactionBase

fun TransactionBase.hsetIfMapNotEmpty(key: String, map: Map<String, String>) {
    if (map.isNotEmpty())
        hset(key, map)
}