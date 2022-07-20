package net.perfectdreams.loritta.cinnamon.pudding.utils.metrics

import io.prometheus.client.Gauge

class PuddingMetrics {
    val availablePermits = Gauge.build("pudding_available_semaphore_permits", "Available semaphore permits for transactions")
        .register()
}