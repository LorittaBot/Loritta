package net.perfectdreams.loritta.cinnamon.pudding.utils

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PuddingTasks(private val pudding: Pudding) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val executorService = Executors.newScheduledThreadPool(1)

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

        if (pudding.messageQueueListener != null) {
            executorService.scheduleWithFixedDelay(
                MessageQueuePoller(pudding),
                0L,
                1L,
                TimeUnit.SECONDS
            )
        } else {
            logger.warn { "Not starting the Message Queue Poller because there isn't a Message Queue Listener set..." }
        }
    }

    fun shutdown() {
        executorService.shutdown()
    }
}