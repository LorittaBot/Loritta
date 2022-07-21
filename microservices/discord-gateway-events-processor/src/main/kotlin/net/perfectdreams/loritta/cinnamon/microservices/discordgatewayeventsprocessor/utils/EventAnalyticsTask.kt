package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils

import kotlinx.coroutines.debug.DebugProbes
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.charset.StandardCharsets


class EventAnalyticsTask(private val m: DiscordGatewayEventsProcessor) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private var lastEventCountCheck = 0
    private val eventsGatewayCount = m.gatewayProxies.associate {
        it to 0
    }.toMutableMap()

    override fun run() {
        val mb = 1024 * 1024
        val runtime = Runtime.getRuntime()

        val totalEventsProcessed = m.gatewayProxies.sumOf { it.totalEventsReceived.get() }
        logger.info { "Total Discord Events processed: $totalEventsProcessed; (+${totalEventsProcessed - lastEventCountCheck})" }
        lastEventCountCheck = totalEventsProcessed

        for (gateway in m.gatewayProxies) {
            val gatewayEventsProcessed = gateway.totalEventsReceived.get()
            val previousEventsProcessed = eventsGatewayCount[gateway] ?: 0
            logger.info { "Discord Events processed on [${gateway.state} (${gateway.connectionTries})] ${gateway.url}: $gatewayEventsProcessed; (+${gatewayEventsProcessed - previousEventsProcessed}); Last connection: ${gateway.lastConnection}; Last disconnection: ${gateway.lastDisconnection}; Last event received at: ${gateway.lastEventReceivedAt}" }
            eventsGatewayCount[gateway] = gatewayEventsProcessed
        }

        logger.info { "Active Events (${m.activeEvents.size})" }
        logger.info { "Used Memory: ${(runtime.totalMemory() - runtime.freeMemory()) / mb}MiB" }
        logger.info { "Free Memory: ${runtime.freeMemory() / mb}MiB" }
        logger.info { "Total Memory: ${runtime.totalMemory() / mb}MiB" }
        logger.info { "Max Memory: ${runtime.maxMemory() / mb}MiB" }
    }
}