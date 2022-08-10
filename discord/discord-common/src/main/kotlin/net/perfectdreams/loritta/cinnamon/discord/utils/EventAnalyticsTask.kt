package net.perfectdreams.loritta.cinnamon.discord.utils

import mu.KLogger
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon

class EventAnalyticsTask(private val m: LorittaCinnamon) : Runnable {
    companion object {
        val logger = KotlinLogging.logger {}
    }

    override fun run() {
        val mb = 1024 * 1024
        val runtime = Runtime.getRuntime()

        for (analyticHandler in m.analyticHandlers)
            analyticHandler.send(logger)

        logger.info { "Active Events (${m.activeEvents.size})" }
        logger.info { "Used Memory: ${(runtime.totalMemory() - runtime.freeMemory()) / mb}MiB" }
        logger.info { "Free Memory: ${runtime.freeMemory() / mb}MiB" }
        logger.info { "Total Memory: ${runtime.totalMemory() / mb}MiB" }
        logger.info { "Max Memory: ${runtime.maxMemory() / mb}MiB" }
    }

    fun interface AnalyticHandler {
        fun send(logger: KLogger)
    }
}