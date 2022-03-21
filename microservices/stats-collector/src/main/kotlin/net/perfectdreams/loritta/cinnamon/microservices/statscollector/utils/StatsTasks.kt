package net.perfectdreams.loritta.cinnamon.microservices.statscollector.utils

import net.perfectdreams.loritta.cinnamon.microservices.statscollector.StatsCollector
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class StatsTasks(private val m: StatsCollector) {
    private val executorService = Executors.newScheduledThreadPool(1)

    fun start() {
        executorService.scheduleWithFixedDelay(
            LorittaStatsCollector(m),
            0L,
            1L,
            TimeUnit.MINUTES
        )
    }

    fun shutdown() {
        executorService.shutdown()
    }
}