package net.perfectdreams.loritta.cinnamon.discord.utils.redis

import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.api.async.multi

class TransactionalRedisCommands(val commands: RedisAsyncCommands<String, String>) {
    suspend fun multi() = commands.multi {

    }
}