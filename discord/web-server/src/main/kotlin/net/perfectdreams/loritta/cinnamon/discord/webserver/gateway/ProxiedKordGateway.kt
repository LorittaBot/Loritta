package net.perfectdreams.loritta.cinnamon.discord.webserver.gateway

import dev.kord.gateway.Command
import dev.kord.gateway.Event
import dev.kord.gateway.Gateway
import dev.kord.gateway.GatewayConfiguration
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.DiscordGatewayCommandNotification
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.LorittaNotification
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

/**
 * Proxied Kord Gateway, sending events via [Pudding].
 */
class ProxiedKordGateway(
    private val shardId: Int,
    private val pudding: Pudding
) : Gateway {
    override val events = MutableSharedFlow<Event>(extraBufferCapacity = Int.MAX_VALUE) // The extraBufferCapacity is the same used in Kord's DefaultGatewayBuilder!

    override suspend fun send(command: Command) {
        pudding.notify(
            DiscordGatewayCommandNotification(
                UUID.randomUUID().toString(),
                shardId,
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