package net.perfectdreams.loritta.cinnamon.pudding.utils

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PuddingTasks(private val pudding: Pudding) {
    val executorService = Executors.newScheduledThreadPool(1)

    fun start() {
        executorService.scheduleWithFixedDelay(
            PartitionCreator(pudding),
            0L,
            1L,
            TimeUnit.DAYS
        )

        executorService.scheduleWithFixedDelay(
            AutoExpireInteractionsData(pudding),
            0L,
            1L,
            TimeUnit.MINUTES
        )
    }

    fun shutdown() {
        executorService.shutdown()
    }
}