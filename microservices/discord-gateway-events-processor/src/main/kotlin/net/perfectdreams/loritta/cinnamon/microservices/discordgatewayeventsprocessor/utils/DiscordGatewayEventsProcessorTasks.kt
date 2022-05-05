package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DiscordGatewayEventsProcessorTasks(private val m: DiscordGatewayEventsProcessor) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val executorService = Executors.newScheduledThreadPool(2)
    private val eventAnalytics = EventAnalyticsTask(m)

    fun start() {
        // Every 5s
        executorService.scheduleAtFixedRate(eventAnalytics, 0L, 5L, TimeUnit.SECONDS)
    }

    fun shutdown() {
        executorService.shutdown()
    }
}