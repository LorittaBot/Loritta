package net.perfectdreams.loritta.cinnamon.discord.utils.metrics

import io.prometheus.client.Counter
import io.prometheus.client.Histogram
import java.time.Duration

/**
 * Used to hold Prometheus instrumentations
 */
object Prometheus {
    val UPDATE_PERIOD = Duration.ofSeconds(5L)

    val EXECUTED_COMMAND_LATENCY_COUNT = Histogram.build()
        .name("executed_command_latency")
        .help("Executed Command Latency")
        .labelNames("command", "executor")
        .buckets(0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 7.5, 10.0, 15.0, 30.0)
        .create()

    val AUTOMATICALLY_DEFERRED_COUNT = Counter.build()
        .name("automatically_deferred_count")
        .help("Count of requests that were automatically deferred")
        .labelNames("command", "executor")
        .create()

    fun register() {
        JFRExports.register()

        EXECUTED_COMMAND_LATENCY_COUNT.register<Counter>()
        AUTOMATICALLY_DEFERRED_COUNT.register<Counter>()
    }
}