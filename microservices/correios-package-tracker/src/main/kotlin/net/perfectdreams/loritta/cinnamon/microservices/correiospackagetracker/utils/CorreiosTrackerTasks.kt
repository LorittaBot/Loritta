package net.perfectdreams.loritta.cinnamon.microservices.correiospackagetracker.utils

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.correiospackagetracker.CorreiosPackageTracker
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CorreiosTrackerTasks(private val m: CorreiosPackageTracker) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val executorService = Executors.newScheduledThreadPool(2)

    private val correiosPackageInfoUpdater = CorreiosPackageInfoUpdater(m)

    fun start() {
        executorService.scheduleAtFixedRate(correiosPackageInfoUpdater, 0, 15, TimeUnit.SECONDS)
    }

    fun shutdown() {
        executorService.shutdown()
    }
}