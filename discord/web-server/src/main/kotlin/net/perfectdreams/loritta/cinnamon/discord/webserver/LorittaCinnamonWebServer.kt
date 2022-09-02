package net.perfectdreams.loritta.cinnamon.discord.webserver

import io.ktor.client.*
import io.lettuce.core.RedisClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.webserver.gateway.ProcessDiscordGatewayEvents
import net.perfectdreams.loritta.cinnamon.discord.webserver.gateway.ProxyDiscordGatewayManager
import net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.discord.webserver.webserver.InteractionsServer
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.pudding.Pudding

class LorittaCinnamonWebServer(
    val config: RootConfig,
    private val languageManager: LanguageManager,
    private val services: Pudding,
    private val redisClient: RedisClient,
    private val http: HttpClient,
    private val replicaId: Int
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val replicaInstance = config.replicas.instances.firstOrNull { it.replicaId == replicaId } ?: error("Missing replica configuration for replica ID $replicaId")

    private val proxyDiscordGatewayManager = ProxyDiscordGatewayManager(
        this,
        config.discordShards.totalShards,
        replicaInstance,
        redisClient
    )

    private val discordGatewayEventsProcessor = ProcessDiscordGatewayEvents(
        this,
        config.totalEventsPerBatch,
        replicaInstance,
        redisClient.connect(),
        proxyDiscordGatewayManager.gateways
    )

    private var stats = Pair(0L, 0L)

    fun start() {
        val cinnamon = LorittaCinnamon(
            replicaId == 1,
            proxyDiscordGatewayManager,
            config.cinnamon,
            languageManager,
            services,
            http
        )

        cinnamon.start()

        // Start processing gateway events
        GlobalScope.launch(Dispatchers.IO) {
            discordGatewayEventsProcessor.run()
        }

        cinnamon.addAnalyticHandler { logger ->
            val (previousEventsProcessed, previousPollLoopsCheck) = stats

            val totalEventsProcessed = discordGatewayEventsProcessor.totalEventsProcessed
            val totalPollLoopsCheck = discordGatewayEventsProcessor.totalPollLoopsCount

            logger.info { "Total Discord Events processed: $totalEventsProcessed; (+${totalEventsProcessed - previousEventsProcessed})" }
            logger.info { "Total Poll Loops: $totalPollLoopsCheck; (+${totalPollLoopsCheck - previousPollLoopsCheck})" }

            val previousStats = stats
            logger.info { "Discord Events processed: ${discordGatewayEventsProcessor.totalEventsProcessed} (+${discordGatewayEventsProcessor.totalEventsProcessed - previousStats.first}); Current Poll Loops Count: ${discordGatewayEventsProcessor.totalPollLoopsCount} (+${discordGatewayEventsProcessor.totalPollLoopsCount - previousStats.second}); Last poll took ${discordGatewayEventsProcessor.lastPollDuration} to complete; Blocked for notifications? ${discordGatewayEventsProcessor.isBlockedForNotifications}; Last notification block duration: ${discordGatewayEventsProcessor.lastBlockDuration}" }
            stats = Pair(discordGatewayEventsProcessor.totalEventsProcessed, discordGatewayEventsProcessor.totalPollLoopsCount)
        }

        val interactionsServer = InteractionsServer(
            cinnamon.interaKTions,
            config.httpInteractions.publicKey
        )

        interactionsServer.start()
    }

    fun redisKey(key: String) = config.redis.keyPrefix + key
}