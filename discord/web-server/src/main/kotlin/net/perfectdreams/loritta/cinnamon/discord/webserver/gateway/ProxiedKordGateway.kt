package net.perfectdreams.loritta.cinnamon.discord.webserver.gateway

import com.zaxxer.hikari.HikariDataSource
import dev.kord.gateway.Command
import dev.kord.gateway.Event
import dev.kord.gateway.Gateway
import dev.kord.gateway.GatewayConfiguration
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.DiscordGatewayCommandNotification
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.LorittaNotification
import org.jetbrains.exposed.sql.TextColumnType
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

/**
 * Proxied Kord Gateway, sending events via [hikariDataSource].
 */
class ProxiedKordGateway(
    private val shardId: Int,
    private val hikariDataSource: HikariDataSource
) : Gateway {
    override val events = MutableSharedFlow<Event>(extraBufferCapacity = Int.MAX_VALUE) // The extraBufferCapacity is the same used in Kord's DefaultGatewayBuilder!

    override suspend fun send(command: Command) {
        hikariDataSource.connection.use {
            val statement = it.prepareStatement("SELECT pg_notify(?, ?)")

            statement.setString(1, "gateway_commands_shard_$shardId")
            statement.setString(
                2,
                Json.encodeToString(
                    Json.encodeToJsonElement(
                        Command.SerializationStrategy,
                        command
                    ).jsonObject
                )
            )

            statement.execute()

            it.commit()
        }
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