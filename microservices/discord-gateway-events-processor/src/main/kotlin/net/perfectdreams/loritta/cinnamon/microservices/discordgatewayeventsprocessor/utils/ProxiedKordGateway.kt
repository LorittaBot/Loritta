package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils

import dev.kord.gateway.Command
import dev.kord.gateway.Event
import dev.kord.gateway.Gateway
import dev.kord.gateway.GatewayConfiguration
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.gatewayproxy.GatewayProxy
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

/**
 * Proxied Kord Gateway, wrapping a [GatewayProxy] connection, used for Kord Voice.
 */
class ProxiedKordGateway(
    private val shardId: Int,
    private val proxy: GatewayProxy
) : Gateway {
    override val events = MutableSharedFlow<Event>()

    override suspend fun send(command: Command) {
        proxy.send(shardId, command)
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