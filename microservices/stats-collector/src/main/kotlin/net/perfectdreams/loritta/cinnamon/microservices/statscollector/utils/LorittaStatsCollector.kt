package net.perfectdreams.loritta.cinnamon.microservices.statscollector.utils

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.statscollector.StatsCollector

class LorittaStatsCollector(val m: StatsCollector) : RunnableCoroutineWrapper() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun runCoroutine() {
        try {
            logger.info { "Collecting stats data from Loritta Legacy..." }
            val statuses = try {
                m.getLorittaLegacyStatusFromAllClusters()
            } catch (e: StatsCollector.ClusterOfflineException) {
                logger.warn(e) { "Cluster ${e.url} is offline! Skipping stats collection task..." }
                return
            }

            var guildCount = 0L

            for (status in statuses) {
                val areAllShardsAreReady = status.shards.all { it.status == "CONNECTED" }
                if (!areAllShardsAreReady) {
                    logger.warn { "Shards in ${status.id} (${status.name}) are not ready! Skipping stats collection task..." }
                    return
                }

                guildCount += status.shards.sumOf { it.guildCount }
            }

            m.senders.forEach {
                try {
                    it.send(guildCount)
                    logger.info { "Successfully sent Loritta Legacy's stats data to ${it}!" }
                } catch (e: Exception) {
                    logger.warn(e) { "Something went wrong while trying to send Loritta Legacy's stats data to ${it}!" }
                }
            }
        } catch (e: Exception) {
            logger.warn { "Something went wrong while collecting and sending stats data!" }
        }
    }
}