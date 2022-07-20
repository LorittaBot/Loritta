package net.perfectdreams.loritta.cinnamon.platform.utils.metrics

object KordMetrics : PrometheusMetrics() {
    val requests = createCounterWithLabels("kord_requests_count", "How many HTTP requests were made by Kord") {
        labels("route", "http_method")
    }
}