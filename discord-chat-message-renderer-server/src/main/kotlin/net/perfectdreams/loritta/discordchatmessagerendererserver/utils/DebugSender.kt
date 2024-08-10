package net.perfectdreams.loritta.discordchatmessagerendererserver.utils

import mu.KotlinLogging

class DebugSender : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun run() {
        val mb = 1024 * 1024
        val runtime = Runtime.getRuntime()

        logger.info { "Used Memory: ${(runtime.totalMemory() - runtime.freeMemory()) / mb}" }
        logger.info { "Free Memory: ${runtime.freeMemory() / mb}" }
        logger.info { "Total Memory: ${runtime.totalMemory() / mb}" }
        logger.info { "Max Memory: ${runtime.maxMemory() / mb}" }
    }
}