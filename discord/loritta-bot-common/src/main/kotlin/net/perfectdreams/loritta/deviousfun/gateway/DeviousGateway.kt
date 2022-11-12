package net.perfectdreams.loritta.deviousfun.gateway

import dev.kord.gateway.DefaultGateway
import dev.kord.gateway.Event
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration

/**
 * A devious gateway instance
 */
class DeviousGateway(
    val kordGateway: DefaultGateway,
    val shardId: Int,
    val status: MutableStateFlow<Status>,
    val receivedEventsJob: Job,
    val receivedEvents: EventsChannel
) {
    enum class Status {
        INITIALIZING,
        WAITING_TO_CONNECT,
        WAITING_FOR_IDENTIFY_LOCK,
        WAITING_FOR_BUCKET,
        IDENTIFYING,
        RESUMING,
        CONNECTED,
        RECONNECTING,
        DISCONNECTED,
        UNKNOWN
    }

    val logger = KotlinLogging.logger {}

    val ping: StateFlow<Duration?>
        get() = kordGateway.ping
}