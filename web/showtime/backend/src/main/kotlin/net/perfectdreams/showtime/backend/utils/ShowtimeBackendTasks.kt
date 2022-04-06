package net.perfectdreams.showtime.backend.utils

import net.perfectdreams.showtime.backend.ShowtimeBackend
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ShowtimeBackendTasks(private val showtime: ShowtimeBackend) {
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