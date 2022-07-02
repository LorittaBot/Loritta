package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils

import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DiscordGatewayEventsProcessorTasks(private val m: DiscordGatewayEventsProcessor) {
    private val executorService = Executors.newScheduledThreadPool(4)
    private val eventAnalytics = EventAnalyticsTask(m)
    val processDiscordGatewayEvents = ProcessDiscordGatewayEvents(m, m.queueDatabase.hikariDataSource)

    fun start() {
        // Runs on a infinite loop
        executorService.submit(processDiscordGatewayEvents)

        // Every 5s
        executorService.scheduleAtFixedRate(eventAnalytics, 0L, 5L, TimeUnit.SECONDS)
    }

    fun shutdown() {
        executorService.shutdown()
    }
}