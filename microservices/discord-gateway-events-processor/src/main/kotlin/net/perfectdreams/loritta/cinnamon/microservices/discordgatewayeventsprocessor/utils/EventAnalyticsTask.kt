package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.ProcessDiscordEventsModule

class EventAnalyticsTask(private val m: DiscordGatewayEventsProcessor) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun run() {
        printStats(m.starboardModule)
        printStats(m.addFirstToNewChannelsModule)
    }

    private fun printStats(module: ProcessDiscordEventsModule) {
        logger.info { "Module ${module::class.simpleName} stats: ${module.activeEvents.size} active events; launched events: ${module.launchedEvents}; consumer restarts: ${module.consumerRestarts}; ${module.activeEvents}" }
    }
}