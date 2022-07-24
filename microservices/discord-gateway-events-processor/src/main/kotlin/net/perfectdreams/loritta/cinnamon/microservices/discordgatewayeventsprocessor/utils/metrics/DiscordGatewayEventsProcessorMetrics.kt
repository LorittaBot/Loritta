package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.metrics

import net.perfectdreams.loritta.cinnamon.platform.utils.metrics.PrometheusMetrics

object DiscordGatewayEventsProcessorMetrics : PrometheusMetrics() {
    val gatewayEventsReceived = createCounterWithLabels("gateway_events_received", "Gateway Events Received", "shard", "event")

    val activeEvents = createGaugeWithLabels("discordgatewayeventsprocessor_active_events", "Active Events")

    val executedModuleLatency = createHistogramWithLabels("executed_module_latency", "Executed Module Latency", "module", "event") {
        buckets(0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 7.5, 10.0, 15.0, 30.0, 45.0, 60.0, 75.0, 90.0)
    }

    val voiceConnections = createGaugeWithLabels("cinnamon_voice_connections_total", "How many voice connections are active")
    val invitesBlocked = createCounterWithLabels("cinnamon_invite_blocker_invites_blocked_total", "How many times an invite was blocked", "guild")
    val owoTriggered = createCounterWithLabels("cinnamon_owo_triggered_total", "How many times the OwO easter egg was triggered", "guild")
    val firstTriggered = createCounterWithLabels("cinnamon_first_triggered_total", "How many times the First easter egg was triggered", "guild")
}