package net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor.utils

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor.DirectMessageProcessor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DirectMessageProcessorTasks(private val m: DirectMessageProcessor) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val executorService = Executors.newScheduledThreadPool(2)

    private val pendingMessageProcessor = PendingImportantNotificationsProcessor(
        m.config.loritta,
        m.languageManager,
        m.services,
        m.rest
    )

    fun start() {
        // Pending Message Processor
        executorService.scheduleAtFixedRate(pendingMessageProcessor, 0, 1, TimeUnit.SECONDS)
    }

    fun shutdown() {
        executorService.shutdown()
    }
}