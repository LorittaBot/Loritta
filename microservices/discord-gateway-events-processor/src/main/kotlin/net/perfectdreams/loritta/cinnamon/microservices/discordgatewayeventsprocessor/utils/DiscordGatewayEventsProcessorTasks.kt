package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils

import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DiscordGatewayEventsProcessorTasks(m: DiscordGatewayEventsProcessor) {
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