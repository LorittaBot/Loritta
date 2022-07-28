package net.perfectdreams.loritta.cinnamon.discord.utils.metrics

object KordMetrics : PrometheusMetrics() {
    val requests = createCounterWithLabels("kord_requests_count", "How many HTTP requests were made by Kord") {
        labelNames("route", "http_method")
    }
}