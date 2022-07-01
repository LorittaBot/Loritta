package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor

class EventAnalyticsTask(private val m: DiscordGatewayEventsProcessor) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    var lastEventCountCheck = 0

    override fun run() {
        val totalEventsProcessed = m.tasks.processDiscordGatewayEvents.totalEventsProcessed
        logger.info { "Total Discord Events processed: $totalEventsProcessed; (+${totalEventsProcessed - lastEventCountCheck})" }
        lastEventCountCheck = totalEventsProcessed
        logger.info { "Active Events (${m.activeEvents.size}): ${m.activeEvents}" }
    }
}