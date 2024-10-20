package net.perfectdreams.loritta.morenitta.analytics

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import net.perfectdreams.loritta.cinnamon.discord.utils.RunnableCoroutine
import net.perfectdreams.loritta.cinnamon.pudding.services.UsersService
import net.perfectdreams.loritta.cinnamon.pudding.tables.BoughtStocks
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.TickerPrices
import net.perfectdreams.loritta.cinnamon.pudding.tables.TotalSonhosStats
import net.perfectdreams.loritta.morenitta.LorittaBot
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.sum
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

/**
 * A class that periodically stores stats to the database, "poor man's Prometheus"
 */
class MagicStats(val loritta: LorittaBot) : RunnableCoroutine {
    private val totalSonhosGauge = LazyGauge(
        registry = LorittaMetrics.appMicrometerRegistry,
        gaugeName = "loritta.total_sonhos"
    )
    private val totalSonhosOfBannedUsersGauge = LazyGauge(
        registry = LorittaMetrics.appMicrometerRegistry,
        gaugeName = "loritta.total_sonhos_of_banned_users"
    )
    private val totalSonhosBrokerGauge = LazyGauge(
        registry = LorittaMetrics.appMicrometerRegistry,
        gaugeName = "loritta.total_sonhos_broker"
    )

    override suspend fun run() {
        class Result(
            val totalSonhos: Long,
            val totalSonhosOfBannedUsers: Long,
            val totalSonhosBroker: Long
        )

        if (loritta.isMainInstance) {
            val result = loritta.transaction {
                val sumField = Profiles.money.sum()
                val totalSonhos = Profiles.select(sumField)
                    .where {
                        Profiles.money greater 0
                    }
                    .first()[sumField] ?: 0

                val totalSonhosOfBannedUsers = Profiles.select(sumField)
                    .where {
                        Profiles.money greater 0 and (Profiles.id inSubQuery UsersService.validBannedUsersList(System.currentTimeMillis()))
                    }
                    .first()[sumField] ?: 0

                val sonhosBrokerSumField = TickerPrices.value.sum()
                val totalSonhosBroker =
                    BoughtStocks.innerJoin(TickerPrices, { BoughtStocks.ticker }, { TickerPrices.ticker })
                        .select(sonhosBrokerSumField)
                        .first()[sonhosBrokerSumField] ?: 0

                TotalSonhosStats.insert {
                    it[TotalSonhosStats.timestamp] = Instant.now()
                    it[TotalSonhosStats.totalSonhos] = totalSonhos
                    it[TotalSonhosStats.totalSonhosOfBannedUsers] = totalSonhosOfBannedUsers
                    it[TotalSonhosStats.totalSonhosBroker] = totalSonhosBroker
                }

                return@transaction Result(totalSonhos, totalSonhosOfBannedUsers, totalSonhosBroker)
            }

            totalSonhosGauge.setValue(result.totalSonhos)
            totalSonhosOfBannedUsersGauge.setValue(result.totalSonhosOfBannedUsers)
            totalSonhosBrokerGauge.setValue(result.totalSonhosBroker)
        }
    }

    data class LazyGauge(
        private val registry: MeterRegistry,
        private val gaugeName: String
    ) {
        private val atomicLong = AtomicLong()

        // Flag to ensure that gauge is registered only once
        @Volatile
        private var isGaugeCreated = false

        // Function to set the value to AtomicLong and lazily create a gauge
        fun setValue(value: Long) {
            atomicLong.set(value)

            if (!isGaugeCreated) {
                synchronized(this) {
                    if (!isGaugeCreated) {
                        createGauge()
                        isGaugeCreated = true
                    }
                }
            }
        }

        // The actual gauge creation logic
        private fun createGauge() {
            Gauge.builder(gaugeName) { atomicLong.get().toDouble() }
                .register(registry)
        }

        // Getter for AtomicLong value
        fun getValue(): Long = atomicLong.get()
    }
}