package net.perfectdreams.loritta.website.routes.api.v1.loritta

import io.ktor.application.*
import io.ktor.response.*
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.BaseRoute
import java.io.StringWriter

class GetPrometheusMetricsRoute(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/loritta/metrics") {
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