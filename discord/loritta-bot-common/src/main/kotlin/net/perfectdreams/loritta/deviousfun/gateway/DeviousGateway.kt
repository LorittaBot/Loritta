package net.perfectdreams.loritta.deviousfun.gateway

import dev.kord.gateway.DefaultGateway
import dev.kord.gateway.Event
import dev.kord.gateway.on
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import net.perfectdreams.loritta.deviousfun.JDA
import net.perfectdreams.loritta.deviousfun.listeners.KordListener
import kotlin.time.Duration

/**
 * A devious gateway instance
 */
class DeviousGateway(
    val jda: JDA,
    val kordGateway: DefaultGateway,
    val shardId: Int
) {
    val events: SharedFlow<Event>
        get() = kordGateway.events

    val ping: StateFlow<Duration?>
        get() = kordGateway.ping

    init {
        // Register the JDA-like event and cache listener
        KordListener(jda, this)
    }
}

public inline fun <reified T : Event> DeviousGateway.on(
    scope: CoroutineScope = this.kordGateway,
    crossinline consumer: suspend T.() -> Unit
) = kordGateway.on(scope, consumer)