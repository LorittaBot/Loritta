package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.metrics

import net.perfectdreams.loritta.cinnamon.platform.utils.metrics.PrometheusMetrics

object DiscordGatewayEventsProcessorMetrics : PrometheusMetrics() {
    val gatewayEventsReceived = createCounterWithLabels("gateway_events_received", "Gateway Events Received", "shard", "event")

    val activeEvents = createGaugeWithLabels("discordgatewayeventsprocessor_active_events", "Active Events")

    val executedModuleLatency = createHistogramWithLabels("executed_module_latency", "Executed Module Latency", "module", "event") {
        name("executed_module_latency")
        help("Executed Module Latency")
        buckets(0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 7.5, 10.0, 15.0, 30.0, 45.0, 60.0, 75.0, 90.0)
    }
}