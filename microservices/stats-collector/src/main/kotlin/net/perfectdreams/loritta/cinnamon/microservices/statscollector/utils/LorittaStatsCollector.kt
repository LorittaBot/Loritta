package net.perfectdreams.loritta.cinnamon.microservices.statscollector.utils

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.statscollector.StatsCollector

class LorittaStatsCollector(val m: StatsCollector) : RunnableCoroutineWrapper() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun runCoroutine() {
        try {
            logger.info { "Collecting analytics data from Loritta Legacy..." }
            val statuses = m.getLorittaLegacyStatusFromAllClusters()

            var guildCount = 0L

            for (statusJob in statuses) {
                try {
                    val status = statusJob.await()

                    val areAllShardsAreReady = status.shards.any { it.status == "CONNECTED" }
                    if (!areAllShardsAreReady) {
                        logger.warn { "Shards in ${status.id} (${status.name}) are not ready! Skipping analytics collection task..." }
                        return
                    }

                    guildCount += status.shards.sumOf { it.guildCount }
                } catch (e: StatsCollector.ClusterOfflineException) {
                    logger.warn(e) { "Cluster ${e.url} is offline! Skipping analytics collection task..." }
                    return
                }
            }

            val jobs = m.senders.map {
                it to m.launch { it.send(guildCount) }
            }

            jobs.forEach {
                try {
                    it.second.join()
                    logger.info { "Successfully sent Loritta Legacy's analytics data to ${it.first}!" }
                } catch (e: Exception) {
                    logger.warn(e) { "Something went wrong while trying to send Loritta Legacy's analytics data to ${it.first}!" }
                }
            }
        } catch (e: Exception) {
            logger.warn { "Something went wrong while collecting and sending analytics data!" }
        }
    }
}