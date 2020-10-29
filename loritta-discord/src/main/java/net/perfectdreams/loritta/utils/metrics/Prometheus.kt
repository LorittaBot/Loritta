package net.perfectdreams.loritta.utils.metrics

import io.prometheus.client.Counter
import io.prometheus.client.Gauge
import io.prometheus.client.Histogram
import java.time.Duration

/**
 * Used to hold Prometheus instrumentations
 */
object Prometheus {
    val UPDATE_PERIOD = Duration.ofSeconds(5L)

    val GUILD_COUNT: Gauge = Gauge.build()
            .name("guilds")
            .help("Guild Count")
            .labelNames("shard")
            .create()

    val USER_COUNT: Gauge = Gauge.build()
            .name("users")
            .help("User Count")
            .labelNames("shard")
            .create()

    val RECEIVED_GUILD_MESSAGES: Counter = Counter.build()
            .name("received_guild_messages")
            .help("Received Messages (all users + bot)")
            .labelNames("shard")
            .create()

    val RECEIVED_PRIVATE_MESSAGES: Counter = Counter.build()
            .name("received_private_messages")
            .help("Received Private Messages")
            .create()

    val RECEIVED_JDA_EVENTS: Counter = Counter.build()
            .name("received_jda_events")
            .help("Received JDA Events")
            .labelNames("shard")
            .create()

    val GATEWAY_PING: Gauge = Gauge.build()
            .name("gateway_ping")
            .help("Discord Gateway Ping")
            .labelNames("shard")
            .create()

    val SHARD_EVENTS: Counter = Counter.build()
            .name("shard_events")
            .help("JDA Shard Event")
            .labelNames("shard", "event_type")
            .create()

    val COMMAND_LATENCY: Histogram = Histogram.build()
            .name("command_latency")
            .help("Time it takes to execute a command")
            .labelNames("name")
            .create()

    val SHARD_STATUS: Gauge = Gauge.build()
            .name("shard_status")
            .help("JDA Shard Status")
            .labelNames("shard")
            .create()

    fun register() {
        JFRExports.register()

        GUILD_COUNT.register<Gauge>()
        USER_COUNT.register<Gauge>()
        RECEIVED_GUILD_MESSAGES.register<Counter>()
        RECEIVED_PRIVATE_MESSAGES.register<Counter>()
        RECEIVED_JDA_EVENTS.register<Counter>()
        GATEWAY_PING.register<Gauge>()
        SHARD_EVENTS.register<Counter>()
        COMMAND_LATENCY.register<Histogram>()
        SHARD_STATUS.register<Gauge>()
    }
}