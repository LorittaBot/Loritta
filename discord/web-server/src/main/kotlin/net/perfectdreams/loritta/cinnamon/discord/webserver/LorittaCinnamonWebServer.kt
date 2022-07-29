package net.perfectdreams.loritta.cinnamon.discord.webserver

import io.ktor.client.*
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.utils.EventAnalyticsTask
import net.perfectdreams.loritta.cinnamon.discord.webserver.gateway.ProxyDiscordGatewayManager
import net.perfectdreams.loritta.cinnamon.discord.webserver.gateway.gatewayproxy.GatewayProxy
import net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.discord.webserver.webserver.InteractionsServer
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.pudding.Pudding

class LorittaCinnamonWebServer(
    private val config: RootConfig,
    private val languageManager: LanguageManager,
    private val services: Pudding,
    private val http: HttpClient,
    private val replicaId: Int
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val gatewayProxies = config.gatewayProxies.filter { it.replicaId == replicaId }.map {
        GatewayProxy(it.url, it.authorizationToken, it.minShard, it.maxShard)
    }

    private var lastEventCountCheck = 0
    private val eventsGatewayCount = gatewayProxies.associate {
        it to 0
    }.toMutableMap()

    fun start() {
        val cinnamon = LorittaCinnamon(
            ProxyDiscordGatewayManager(config.discordShards.totalShards, gatewayProxies),
            config.cinnamon,
            languageManager,
            services,
            http
        )

        cinnamon.start()

        gatewayProxies.forEachIndexed { index, gatewayProxy ->
            logger.info { "Starting Gateway Proxy $index (${gatewayProxy.url})" }
            gatewayProxy.start()
        }

        cinnamon.addAnalyticHandler {
            val totalEventsProcessed = gatewayProxies.sumOf { it.totalEventsReceived.get() }
            EventAnalyticsTask.logger.info { "Total Discord Events processed: $totalEventsProcessed; (+${totalEventsProcessed - lastEventCountCheck})" }
            lastEventCountCheck = totalEventsProcessed

            for (gateway in gatewayProxies) {
                val gatewayEventsProcessed = gateway.totalEventsReceived.get()
                val previousEventsProcessed = eventsGatewayCount[gateway] ?: 0
                EventAnalyticsTask.logger.info { "Discord Events processed on [${gateway.state} (${gateway.connectionTries})] ${gateway.url}: $gatewayEventsProcessed; (+${gatewayEventsProcessed - previousEventsProcessed}); Last connection: ${gateway.lastConnection}; Last disconnection: ${gateway.lastDisconnection}; Last event received at: ${gateway.lastEventReceivedAt}" }
                eventsGatewayCount[gateway] = gatewayEventsProcessed
            }
        }

        val interactionsServer = InteractionsServer(
            cinnamon.interactionsManager.interaKTionsManager,
            cinnamon.rest,
            config.cinnamon.discord.applicationId,
            config.httpInteractions.publicKey
        )

        interactionsServer.start()
    }
}