package net.perfectdreams.loritta.cinnamon.discord.webserver.gateway

import dev.kord.gateway.Command
import dev.kord.gateway.Event
import dev.kord.gateway.Gateway
import dev.kord.gateway.GatewayConfiguration
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import net.perfectdreams.loritta.cinnamon.discord.utils.RedisKeys
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

/**
 * Proxied Kord Gateway, sending events via [redisCommands].
 */
@OptIn(ExperimentalLettuceCoroutinesApi::class)
class ProxiedKordGateway(
    private val redisKeys: RedisKeys,
    private val shardId: Int,
    private val redisCommands: RedisCoroutinesCommands<String, String>,
) : Gateway {
    override val events = MutableSharedFlow<Event>(extraBufferCapacity = Int.MAX_VALUE) // The extraBufferCapacity is the same used in Kord's DefaultGatewayBuilder!

    override suspend fun send(command: Command) {
        redisCommands.rpush(
            redisKeys.discordGatewayCommands(shardId),
            Json.encodeToString(
                Json.encodeToJsonElement(
                    Command.SerializationStrategy,
                    command
                ).jsonObject
            )
        )
    }

    // We don't need to implement these
    override val coroutineContext: CoroutineContext // Unused
        get() = TODO("Not yet implemented")
    override val ping: StateFlow<Duration?> // Unused
        get() = TODO("Not yet implemented")

    override suspend fun detach() {
        TODO("Not yet implemented")
    }

    override suspend fun start(configuration: GatewayConfiguration) {
        TODO("Not yet implemented")
    }

    override suspend fun stop() {
        TODO("Not yet implemented")
    }
}