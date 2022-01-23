package net.perfectdreams.loritta.cinnamon.microservices.statscollector

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.statscollector.senders.DatabaseStatsSender
import net.perfectdreams.loritta.cinnamon.microservices.statscollector.senders.DiscordBotsStatsSender
import net.perfectdreams.loritta.cinnamon.microservices.statscollector.senders.TopggStatsSender
import net.perfectdreams.loritta.cinnamon.microservices.statscollector.utils.LorittaLegacyStatusResponse
import net.perfectdreams.loritta.cinnamon.microservices.statscollector.utils.StatsTasks
import net.perfectdreams.loritta.cinnamon.microservices.statscollector.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding

/**
 * Collects stats from Loritta (Legacy) and sends it to Stats Senders
 */
class StatsCollector(val config: RootConfig, val services: Pudding, val http: HttpClient) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val senders = listOf(
        TopggStatsSender(http, config.topgg.clientId, config.topgg.token),
        DiscordBotsStatsSender(http, config.discordBots.clientId, config.discordBots.token),
        DatabaseStatsSender(services)
    )

    fun start() {
        StatsTasks(this).start()
    }

    suspend fun getLorittaLegacyStatusFromAllClusters() = config.lorittaLegacyClusterUrls.map {
        try {
            val response = http.get<HttpResponse>("$it/api/v1/loritta/status") {
                userAgent("Loritta Cinnamon Stats Collector")
            }

            val body = response.readText()

            val data = Json.decodeFromString<LorittaLegacyStatusResponse>(body)
            logger.info { "Successfully retrieved data from Cluster ${data.id} (${data.name})!" }
            data
        } catch (e: Exception) {
            logger.warn(e) { "Cluster $it is offline!" }
            throw ClusterOfflineException(it)
        }
    }

    class ClusterOfflineException(val url: String) : RuntimeException()
}