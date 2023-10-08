package net.perfectdreams.loritta.morenitta.gateway

import dev.kord.gateway.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.JDAImpl
import kotlin.time.Duration

/**
 * Proxied Kord Gateway, sending events via [JDA].
 */
class JDAProxiedKordGateway(private val jda: JDAImpl) : Gateway {
    override val events = MutableSharedFlow<Event>(extraBufferCapacity = Int.MAX_VALUE) // The extraBufferCapacity is the same used in Kord's DefaultGatewayBuilder!
    override val coroutineContext = SupervisorJob()

    override suspend fun send(command: Command) {
        jda.client.send(
            DataObject.fromJson(
                Json.encodeToString(
                    Json.encodeToJsonElement(
                        Command.SerializationStrategy,
                        command
                    ).jsonObject
                )
            )
        )
    }

    // We don't need to implement these
    override val ping: StateFlow<Duration?> // Unused
        get() = TODO("Not yet implemented")

    override suspend fun detach() {
        TODO("Not yet implemented")
    }

    override suspend fun resume(configuration: GatewayResumeConfiguration) {
        TODO("Not yet implemented")
    }

    override suspend fun start(configuration: GatewayConfiguration) {
        TODO("Not yet implemented")
    }

    override suspend fun stop() {
        TODO("Not yet implemented")
    }

    override suspend fun stop(closeReason: WebSocketCloseReason): GatewayResumeConfiguration {
        TODO("Not yet implemented")
    }
}