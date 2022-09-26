package net.perfectdreams.loritta.cinnamon.discord.utils.metrics

object CinnamonMetrics : PrometheusMetrics() {
    val permissionsCacheInconsistencyFixed = createCounterWithLabels("cinnamon_permissions_cache_inconsistency_fixed_total", "How many times a cache inconsistency was detected and fixed") {
        labelNames("result")
    }
}