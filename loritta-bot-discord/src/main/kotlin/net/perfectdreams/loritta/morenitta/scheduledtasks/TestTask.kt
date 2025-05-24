package net.perfectdreams.loritta.morenitta.scheduledtasks

import mu.KotlinLogging

class TestTask : NamedRunnableCoroutine {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override val taskName = "test-task"

    override suspend fun run() {
        logger.info { "hewwo!!!" }
    }
}