package net.perfectdreams.loritta.cinnamon.platform.webserver.webserver.routes.api.v1.cinnamon

import io.ktor.application.*
import io.ktor.response.*
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.perfectdreams.sequins.ktor.BaseRoute
import java.io.StringWriter

class GetPrometheusMetricsRoute : BaseRoute("/api/v1/cinnamon/metrics") {
    override suspend fun onRequest(call: ApplicationCall) {
        val writer = StringWriter()

        withContext(Dispatchers.IO) {
            // Gets all registered Prometheus Metrics and writes to the StringWriter
            TextFormat.write004(
                writer,
                CollectorRegistry.defaultRegistry.metricFamilySamples()
            )
        }

        call.respondText(writer.toString())
    }
}