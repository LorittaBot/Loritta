package net.perfectdreams.loritta.cinnamon.discord.webserver

import io.ktor.client.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.utils.RedisKeys
import net.perfectdreams.loritta.cinnamon.discord.webserver.gateway.ProcessDiscordGatewayEvents
import net.perfectdreams.loritta.cinnamon.discord.webserver.gateway.ProxyDiscordGatewayManager
import net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.discord.webserver.webserver.InteractionsServer
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import redis.clients.jedis.JedisPool

class LorittaCinnamonWebServer(
    val config: RootConfig,
    private val languageManager: LanguageManager,
    private val services: Pudding,
    private val jedisPool: JedisPool,
    private val redisKeys: RedisKeys,
    private val http: HttpClient,
    private val replicaId: Int
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val replicaInstance = config.replicas.instances.firstOrNull { it.replicaId == replicaId } ?: error("Missing replica configuration for replica ID $replicaId")

    private val proxyDiscordGatewayManager = ProxyDiscordGatewayManager(
        redisKeys,
        config.discordShards.totalShards,
        replicaInstance,
        jedisPool
    )

    private val discordGatewayEventsProcessors = (0 until config.eventProcessors).map {
        ProcessDiscordGatewayEvents(
            jedisPool,
            redisKeys,
            config.totalEventsPerBatch,
            config.eventProcessors,
            it,
            replicaInstance,
            proxyDiscordGatewayManager.gateways
        )
    }

    private val stats = mutableMapOf<Int, Pair<Long, Long>>()

    fun start() {
        val cinnamon = LorittaCinnamon(
            replicaId == 1,
            proxyDiscordGatewayManager,
            config.cinnamon,
            languageManager,
            services,
            jedisPool,
            redisKeys,
            http
        )

        cinnamon.start()

        // Start processing gateway events
        for (processor in discordGatewayEventsProcessors) {
            GlobalScope.launch(Dispatchers.IO) {
                processor.run()
            }
        }

        cinnamon.addAnalyticHandler { logger ->
            val statsValues = stats.values
            val previousEventsProcessed = statsValues.sumOf { it.first }
            val previousPollLoopsCheck = statsValues.sumOf { it.second }

            val totalEventsProcessed = discordGatewayEventsProcessors.sumOf { it.totalEventsProcessed }
            val totalPollLoopsCheck = discordGatewayEventsProcessors.sumOf { it.totalPollLoopsCount }

            logger.info { "Total Discord Events processed: $totalEventsProcessed; (+${totalEventsProcessed - previousEventsProcessed})" }
            logger.info { "Total Poll Loops: $totalPollLoopsCheck; (+${totalPollLoopsCheck - previousPollLoopsCheck})" }
            for (processor in discordGatewayEventsProcessors) {
                val previousStats = stats[processor.connectionId] ?: Pair(0L, 0L)
                logger.info { "Processor shardId % ${processor.totalConnections} == ${processor.connectionId}: Discord Events processed: ${processor.totalEventsProcessed} (+${processor.totalEventsProcessed - previousStats.first}); Current Poll Loops Count: ${processor.totalPollLoopsCount} (+${processor.totalPollLoopsCount - previousStats.second}); Last poll took ${processor.lastPollDuration} to complete; Blocked for notifications? ${processor.isBlockedForNotifications}; Last notification block duration: ${processor.lastBlockDuration}" }
                stats[processor.connectionId] = Pair(processor.totalEventsProcessed, processor.totalPollLoopsCount)
            }
        }

        val interactionsServer = InteractionsServer(
            cinnamon.interaKTions,
            config.httpInteractions.publicKey
        )

        interactionsServer.start()
    }
}