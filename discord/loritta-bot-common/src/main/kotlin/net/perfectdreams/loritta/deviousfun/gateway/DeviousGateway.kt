package net.perfectdreams.loritta.deviousfun.gateway

import dev.kord.gateway.DefaultGateway
import dev.kord.gateway.Event
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import net.perfectdreams.loritta.deviousfun.DeviousShard
import net.perfectdreams.loritta.deviousfun.listeners.KordListener
import kotlin.time.Duration

/**
 * A devious gateway instance
 */
class DeviousGateway(
    val kordGateway: DefaultGateway,
    val shardId: Int,
    val status: MutableStateFlow<Status>
) {
    enum class Status {
        INITIALIZING,
        WAITING_TO_CONNECT,
        WAITING_FOR_BUCKET,
        IDENTIFYING,
        RESUMING,
        CONNECTED,
        RECONNECTING,
        DISCONNECTED,
        UNKNOWN
    }

    val logger = KotlinLogging.logger {}

    val events: SharedFlow<Event>
        get() = kordGateway.events

    val ping: StateFlow<Duration?>
        get() = kordGateway.ping
}