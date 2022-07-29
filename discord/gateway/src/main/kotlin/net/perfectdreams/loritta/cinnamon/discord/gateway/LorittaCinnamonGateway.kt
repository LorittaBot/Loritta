package net.perfectdreams.loritta.cinnamon.discord.gateway

import dev.kord.common.entity.DiscordShard
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.DefaultGateway
import dev.kord.gateway.start
import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.platforms.kord.installDiscordInteraKTions
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.gateway.gateway.KordDiscordGatewayManager
import net.perfectdreams.loritta.cinnamon.discord.gateway.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.pudding.Pudding

class LorittaCinnamonGateway(
    private val config: RootConfig,
    private val languageManager: LanguageManager,
    private val services: Pudding,
    private val http: HttpClient
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    fun start() {
        // Create all gateway instances
        val gateways = (config.discordShards.minShard..config.discordShards.maxShard).associateWith { DefaultGateway {} }

        val gatewayManager = KordDiscordGatewayManager(
            config.discordShards.totalShards,
            gateways
        )

        val cinnamon = LorittaCinnamon(
            gatewayManager,
            config.cinnamon,
            languageManager,
            services,
            http
        )

        cinnamon.start()

        runBlocking {
            for ((shardId, gateway) in gateways) {
                logger.info { "Setting up and starting shard $shardId..." }
                gateway.installDiscordInteraKTions(
                    Snowflake(config.cinnamon.discord.applicationId),
                    cinnamon.rest,
                    cinnamon.interactionsManager.interactionsRegistry.interaKTionsManager
                )

                scope.launch {
                    gateway.start(config.cinnamon.discord.token) {
                        shard = DiscordShard(shardId, config.discordShards.totalShards)
                    }
                }
            }
        }
    }
}