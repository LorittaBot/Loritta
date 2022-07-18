package net.perfectdreams.loritta.cinnamon.platform.utils.metrics

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.content.*
import io.ktor.http.*
import io.prometheus.client.Collector
import io.prometheus.client.Collector.MetricFamilySamples
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.utils.HostnameUtils
import java.io.Closeable
import java.io.IOException
import java.io.StringWriter
import java.io.Writer
import java.util.*
import kotlin.time.Duration.Companion.seconds

/**
 * Writes Prometheus stats to a Promscale server at [url].
 */
class PromClient internal constructor(
    job: String,
    instance: String,
    val url: String
) : Closeable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val textFormat = CustomTextFormat(job, instance)
    private val http = HttpClient(CIO)
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    internal fun start() {
        coroutineScope.launch {
            while (true) {
                try {
                    postStats()
                } catch (e: Exception) {
                    logger.warn(e) { "Something went wrong while trying to post stats to Promscale!" }
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

        http.post("$url/write") {
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

fun PromscaleClient(job: String, url: String): PromClient {
    val client = PromClient(job, HostnameUtils.getHostname(), url.removeSuffix("/"))
    client.start()
    return client
}