package net.perfectdreams.loritta.cinnamon.discord.utils.metrics

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.content.*
import io.ktor.http.*
import io.prometheus.client.CollectorRegistry
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.perfectdreams.loritta.common.utils.HostnameUtils
import java.io.Closeable
import java.io.StringWriter
import kotlin.time.Duration.Companion.seconds

/**
 * Writes Prometheus stats to a Prometheus remote write server at [url].
 */
class PromPushClient internal constructor(
    job: String,
    instance: String,
    val url: String
) : Closeable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val textFormat = CustomTextFormat(job, instance)
    private val http = HttpClient(CIO)
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    internal fun start() {
        coroutineScope.launch {
            while (true) {
                logger.info { "Posting stats to Prometheus..." }
                try {
                    postStats()
                    logger.info { "Successfully posted stats to Prometheus!" }
                } catch (e: Exception) {
                    logger.warn(e) { "Something went wrong while trying to post stats to Prometheus!" }
                }
                delay(5.seconds)
            }
        }
    }

    private suspend fun postStats() {
        val writer = StringWriter()

        withContext(Dispatchers.IO) {
            // Gets all registered Prometheus Metrics and writes to the StringWriter
            textFormat.write004(
                writer,
                CollectorRegistry.defaultRegistry.metricFamilySamples()
            )
        }

        val s = writer.toString()

        http.post(url) {
            setBody(
                TextContent(
                    s,
                    ContentType.Text.Plain
                )
            )
        }
    }

    override fun close() {
        http.close()
        coroutineScope.cancel()
    }
}

fun PrometheusPushClient(job: String, url: String): PromPushClient {
    val client = PromPushClient(job, HostnameUtils.getHostname(), url)
    client.start()
    return client
}