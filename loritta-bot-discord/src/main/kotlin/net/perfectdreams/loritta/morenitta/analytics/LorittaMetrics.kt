package net.perfectdreams.loritta.morenitta.analytics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import net.dv8tion.jda.internal.JDAImpl
import net.perfectdreams.loritta.cinnamon.pudding.services.UsersService
import net.perfectdreams.loritta.cinnamon.pudding.tables.*
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.devious.GatewayShardStartupResumeStatus
import org.jetbrains.exposed.sql.*
import java.lang.management.ManagementFactory
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

class LorittaMetrics(private val loritta: LorittaBot) {
    companion object {
        val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    }

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
    private val bannedUsersGauge = LazyGauge(
        registry = LorittaMetrics.appMicrometerRegistry,
        gaugeName = "loritta.banned_users"
    )

    fun registerMetrics() {
        appMicrometerRegistry.gaugeCollectionSize("loritta.pending_messages", emptyList(), loritta.pendingMessages)
        appMicrometerRegistry.gauge("jvm.uptime", ManagementFactory.getRuntimeMXBean()) {
            it.uptime.toDouble()
        }

        for (shard in loritta.lorittaShards.shardManager.shardCache) {
            appMicrometerRegistry.gauge("jda.status", listOf(Tag.of("shard", shard.shardInfo.shardId.toString())), shard) {
                shard.status.ordinal.toDouble()
            }

            appMicrometerRegistry.gauge("loritta.gateway_startup_resume_status", listOf(Tag.of("shard", shard.shardInfo.shardId.toString())), shard) {
                (loritta.gatewayShardsStartupResumeStatus[it.shardInfo.shardId] ?: GatewayShardStartupResumeStatus.UNKNOWN).ordinal.toDouble()
            }

            appMicrometerRegistry.gauge("jda.gateway_ping", listOf(Tag.of("shard", shard.shardInfo.shardId.toString())), shard) {
                shard.gatewayPing.toDouble()
            }

            appMicrometerRegistry.gauge("jda.response_total", listOf(Tag.of("shard", shard.shardInfo.shardId.toString())), shard) {
                shard.responseTotal.toDouble()
            }

            appMicrometerRegistry.gauge("jda.guilds", listOf(Tag.of("shard", shard.shardInfo.shardId.toString())), shard) {
                shard.guildCache.size().toDouble()
            }

            appMicrometerRegistry.gauge("jda.unavailable_guilds", listOf(Tag.of("shard", shard.shardInfo.shardId.toString())), shard) {
                (it as JDAImpl).guildSetupController.unavailableGuilds.size().toDouble()
            }

            appMicrometerRegistry.gauge("jda.cached_users", listOf(Tag.of("shard", shard.shardInfo.shardId.toString())), shard) {
                it.userCache.size().toDouble()
            }

            appMicrometerRegistry.gauge("jda.cached_channels", listOf(Tag.of("shard", shard.shardInfo.shardId.toString())), shard) {
                it.channelCache.size().toDouble()
            }

            appMicrometerRegistry.gauge("jda.cached_roles", listOf(Tag.of("shard", shard.shardInfo.shardId.toString())), shard) {
                it.roleCache.size().toDouble()
            }

            appMicrometerRegistry.gauge("jda.cached_emojis", listOf(Tag.of("shard", shard.shardInfo.shardId.toString())), shard) {
                it.emojiCache.size().toDouble()
            }

            appMicrometerRegistry.gauge("jda.cached_audiomanagers", listOf(Tag.of("shard", shard.shardInfo.shardId.toString())), shard) {
                it.audioManagerCache.size().toDouble()
            }

            appMicrometerRegistry.gauge("jda.cached_scheduledevents", listOf(Tag.of("shard", shard.shardInfo.shardId.toString())), shard) {
                it.scheduledEventCache.size().toDouble()
            }
        }
    }

    /**
     * Updates any metrics that require external blocking/async calls
     */
    suspend fun updateMetrics() {
        class Result(
            val totalSonhos: Long,
            val totalSonhosOfBannedUsers: Long,
            val totalSonhosBroker: Long,
            val bannedUsers: Long
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
                val totalSonhosBroker = BoughtStocks.innerJoin(TickerPrices, { BoughtStocks.ticker }, { TickerPrices.ticker })
                    .select(sonhosBrokerSumField)
                    .first()[sonhosBrokerSumField] ?: 0

                TotalSonhosStats.insert {
                    it[TotalSonhosStats.timestamp] = Instant.now()
                    it[TotalSonhosStats.totalSonhos] = totalSonhos
                    it[TotalSonhosStats.totalSonhosOfBannedUsers] = totalSonhosOfBannedUsers
                    it[TotalSonhosStats.totalSonhosBroker] = totalSonhosBroker
                }

                val countDistinct = BannedUsers.userId.countDistinct()
                val bannedUsers = BannedUsers.select(countDistinct)
                    .where {
                        (BannedUsers.userId inSubQuery UsersService.validBannedUsersList(System.currentTimeMillis()))
                    }
                    .first()[countDistinct]

                return@transaction Result(totalSonhos, totalSonhosOfBannedUsers, totalSonhosBroker, bannedUsers)
            }

            totalSonhosGauge.set(result.totalSonhos)
            totalSonhosOfBannedUsersGauge.set(result.totalSonhosOfBannedUsers)
            totalSonhosBrokerGauge.set(result.totalSonhosBroker)
            bannedUsersGauge.set(result.bannedUsers)
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
        fun set(value: Long) {
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
            registry.gauge(gaugeName, atomicLong)
        }

        // Getter for AtomicLong value
        fun getValue(): Long = atomicLong.get()
    }
}