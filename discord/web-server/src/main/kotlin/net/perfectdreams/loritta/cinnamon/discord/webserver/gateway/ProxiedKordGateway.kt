package net.perfectdreams.loritta.cinnamon.discord.webserver.gateway

import com.zaxxer.hikari.HikariDataSource
import dev.kord.gateway.Command
import dev.kord.gateway.Event
import dev.kord.gateway.Gateway
import dev.kord.gateway.GatewayConfiguration
import io.lettuce.core.api.StatefulRedisConnection
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import net.perfectdreams.loritta.cinnamon.discord.webserver.LorittaCinnamonWebServer
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.DiscordGatewayCommandNotification
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.LorittaNotification
import org.jetbrains.exposed.sql.TextColumnType
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

/**
 * Proxied Kord Gateway, sending events via [redisConnection].
 */
class ProxiedKordGateway(
    private val loritta: LorittaCinnamonWebServer,
    private val shardId: Int,
    private val redisConnection: StatefulRedisConnection<String, String>,
) : Gateway {
    override val events = MutableSharedFlow<Event>(extraBufferCapacity = Int.MAX_VALUE) // The extraBufferCapacity is the same used in Kord's DefaultGatewayBuilder!

    private val syncCommands = redisConnection.sync()

    override suspend fun send(command: Command) {
        syncCommands.rpush(
            loritta.redisKey("discord_gateway_commands_shard_$shardId"),
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