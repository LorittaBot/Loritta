package net.perfectdreams.loritta.cinnamon.pudding.utils.metrics

import io.prometheus.client.Gauge

object PuddingMetrics {
    val availablePermits: Gauge = Gauge.build("pudding_available_semaphore_permits", "Available semaphore permits for transactions")
        .labelNames("pool")
        .register()
}