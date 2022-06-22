package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.ProcessDiscordEventsModule

class EventAnalyticsTask(private val m: DiscordGatewayEventsProcessor) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    var lastEventCountCheck = 0

    override fun run() {
        val totalEventsProcessed = m.tasks.processDiscordGatewayEvents.totalEventsProcessed
        logger.info { "Total Discord Events processed: $totalEventsProcessed; (+${totalEventsProcessed - lastEventCountCheck})" }
        lastEventCountCheck = totalEventsProcessed
        printStats(m.starboardModule)
        printStats(m.addFirstToNewChannelsModule)
        printStats(m.discordCacheModule)
    }

    private fun printStats(module: ProcessDiscordEventsModule) {
        logger.info { "Module ${module::class.simpleName} stats: ${module.activeEvents.size} active events; ${module.activeEvents}" }
    }
}