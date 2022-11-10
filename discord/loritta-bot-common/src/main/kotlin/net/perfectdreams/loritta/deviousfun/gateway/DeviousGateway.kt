package net.perfectdreams.loritta.deviousfun.gateway

import dev.kord.gateway.DefaultGateway
import dev.kord.gateway.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.perfectdreams.loritta.deviousfun.DeviousFun
import net.perfectdreams.loritta.deviousfun.listeners.KordListener
import kotlin.time.Duration

/**
 * A devious gateway instance
 */
class DeviousGateway(
    val deviousFun: DeviousFun,
    val kordGateway: DefaultGateway,
    val identifyRateLimiter: ParallelIdentifyRateLimiter,
    val shardId: Int
) {
    enum class Status {
        INITIALIZING,
        WAITING_TO_CONNECT,
        WAITING_FOR_BUCKET,
        IDENTIFYING,
        RESUMING,
        CONNECTED,
        RECONNECTING,
        DISCONNECTED
    }

    val logger = KotlinLogging.logger {}

    val events: SharedFlow<Event>
        get() = kordGateway.events

    val ping: StateFlow<Duration?>
        get() = kordGateway.ping

    val status: MutableStateFlow<Status> = MutableStateFlow(Status.INITIALIZING)

    init {
        // Register the JDA-like event and cache listener
        KordListener(deviousFun, this)
    }
}