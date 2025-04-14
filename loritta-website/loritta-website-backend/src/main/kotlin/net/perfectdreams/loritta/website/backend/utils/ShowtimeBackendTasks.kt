package net.perfectdreams.loritta.website.backend.utils

import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LorittaWebsiteBackendTasks(private val showtime: LorittaWebsiteBackend) {
    private val executorService = Executors.newScheduledThreadPool(1)

    fun start() {
        executorService.scheduleWithFixedDelay(
            LastFmTracker(showtime),
            0L,
            1L,
            TimeUnit.MINUTES
        )
    }

    fun shutdown() {
        executorService.shutdown()
    }
}