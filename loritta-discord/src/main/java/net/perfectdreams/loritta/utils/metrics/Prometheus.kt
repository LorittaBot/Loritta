package net.perfectdreams.loritta.utils.metrics

import io.prometheus.client.Collector
import io.prometheus.client.Counter
import io.prometheus.client.Gauge
import io.prometheus.client.Histogram
import io.prometheus.client.hotspot.ThreadExports
import java.time.Duration

/**
 * Used to hold Prometheus instrumentations
 */
object Prometheus {
    val UPDATE_PERIOD = Duration.ofSeconds(5L)

    val GUILD_COUNT: Gauge = Gauge.build()
            .name("guilds")
            .help("Guild Count")
            .create()

    val USER_COUNT: Gauge = Gauge.build()
            .name("users")
            .help("User Count")
            .create()
            .register()

    val RECEIVED_GUILD_MESSAGES: Counter = Counter.build()
            .name("received_guild_messages")
            .help("Received Messages (all users + bot)")
            .labelNames("shard")
            .create()
            .register()

    val RECEIVED_PRIVATE_MESSAGES: Counter = Counter.build()
            .name("received_private_messages")
            .help("Received Private Messages")
            .create()
            .register()

    val RECEIVED_JDA_EVENTS: Counter = Counter.build()
            .name("received_jda_events")
            .help("Received JDA Events")
            .labelNames("shard")
            .create()
            .register()

    val GATEWAY_PING: Gauge = Gauge.build()
            .name("gateway_ping")
            .help("Discord Gateway Ping")
            .labelNames("shard")
            .create()
            .register()

    val SHARD_EVENTS: Counter = Counter.build()
            .name("shard_events")
            .help("JDA Shard Event")
            .labelNames("shard", "event_type")
            .create()
            .register()

    val COMMAND_LATENCY: Histogram = Histogram.build()
            .name("command_latency")
            .help("Time it takes to execute a command")
            .labelNames("name")
            .create()
            .register()

    fun register() {
        ThreadExports().register<Collector>()
        JFRExports.register()

        GUILD_COUNT.register<Gauge>()
        USER_COUNT.register<Gauge>()
        RECEIVED_GUILD_MESSAGES.register<Counter>()
        RECEIVED_PRIVATE_MESSAGES.register<Counter>()
        RECEIVED_JDA_EVENTS.register<Counter>()
        GATEWAY_PING.register<Gauge>()
        SHARD_EVENTS.register<Counter>()
        COMMAND_LATENCY.register<Histogram>()
    }
}