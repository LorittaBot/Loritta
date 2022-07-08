package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor

class EventAnalyticsTask(private val m: DiscordGatewayEventsProcessor) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private var lastEventCountCheck = 0
    private val eventsGatewayCount = m.gatewayProxies.map {
        it.url to 0
    }.toMap().toMutableMap()

    override fun run() {
        val mb = 1024 * 1024
        val runtime = Runtime.getRuntime()

        val totalEventsProcessed = m.gatewayProxies.sumOf { it.totalEventsReceived.get() }
        logger.info { "Total Discord Events processed: $totalEventsProcessed; (+${totalEventsProcessed - lastEventCountCheck})" }
        lastEventCountCheck = totalEventsProcessed

        for (gateway in m.gatewayProxies) {
            val gatewayEventsProcessed = gateway.totalEventsReceived.get()
            val previousEventsProcessed = eventsGatewayCount[gateway.url] ?: 0
            logger.info { "Discord Events processed on ${gateway.url}: $gatewayEventsProcessed; (+${gatewayEventsProcessed - previousEventsProcessed})" }
            eventsGatewayCount[gateway.url] = gatewayEventsProcessed
        }

        logger.info { "Active Events (${m.activeEvents.size})" }
        logger.info { "Used Memory: ${(runtime.totalMemory() - runtime.freeMemory()) / mb}MiB" }
        logger.info { "Free Memory: ${runtime.freeMemory() / mb}MiB" }
        logger.info { "Total Memory: ${runtime.totalMemory() / mb}MiB" }
        logger.info { "Max Memory: ${runtime.maxMemory() / mb}MiB" }
    }
}