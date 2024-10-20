package net.perfectdreams.loritta.morenitta.analytics

import io.micrometer.core.instrument.Tag
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import net.dv8tion.jda.internal.JDAImpl
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.devious.GatewayShardStartupResumeStatus
import java.lang.management.ManagementFactory

class LorittaMetrics(private val loritta: LorittaBot) {
    companion object {
        val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    }

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

            appMicrometerRegistry.gauge(".guilds", listOf(Tag.of("shard", shard.shardInfo.shardId.toString())), shard) {
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
}