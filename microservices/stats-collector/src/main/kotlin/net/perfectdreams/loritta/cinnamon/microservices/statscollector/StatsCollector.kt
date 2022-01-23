package net.perfectdreams.loritta.cinnamon.microservices.statscollector

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.statscollector.endpoints.DatabaseStatsSender
import net.perfectdreams.loritta.cinnamon.microservices.statscollector.endpoints.DiscordBotsStatsSender
import net.perfectdreams.loritta.cinnamon.microservices.statscollector.endpoints.TopggStatsSender
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

    var shuttingDown = false

    val senders = listOf(
        TopggStatsSender(http, config.topgg.clientId, config.topgg.token),
        DiscordBotsStatsSender(http, config.discordBots.clientId, config.discordBots.token),
        DatabaseStatsSender(services)
    )

    fun start() {
        StatsTasks(this).start()
    }

    fun getLorittaLegacyStatusFromAllClusters() = config.lorittaLegacyClusterUrls.map {
        async {
            try {
                val response = http.get<HttpResponse>("$it/api/v1/loritta/status") {
                    userAgent("Loritta Cinnamon Analytics Collector")
                }

                val body = response.readText()

                Json.decodeFromString<LorittaLegacyStatusResponse>(body)
            } catch (e: Exception) {
                logger.warn(e) { "Cluster $it is offline!" }
                throw ClusterOfflineException(it)
            }
        }
    }

    val jobs = mutableListOf<Job>()

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        if (shuttingDown)
            error("Application is shutting down!")

        val job = GlobalScope.launch(block = block)
        jobs.add(job)
        job.invokeOnCompletion {
            jobs.remove(job)
        }
        return job
    }

    fun <T> async(block: suspend CoroutineScope.() -> T): Deferred<T> {
        if (shuttingDown)
            error("Application is shutting down!")

        val job = GlobalScope.async(block = block)
        jobs.add(job)
        job.invokeOnCompletion {
            jobs.remove(job)
        }
        return job
    }

    class ClusterOfflineException(val url: String) : RuntimeException()
}