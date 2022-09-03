package net.perfectdreams.loritta.cinnamon.discord.gateway

import dev.kord.common.entity.DiscordShard
import dev.kord.gateway.*
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
import net.perfectdreams.loritta.cinnamon.discord.utils.RedisKeys
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import redis.clients.jedis.JedisPool

class LorittaCinnamonGateway(
    private val config: RootConfig,
    private val languageManager: LanguageManager,
    private val services: Pudding,
    private val jedisPool: JedisPool,
    private val redisKeys: RedisKeys,
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
            true,
            gatewayManager,
            config.cinnamon,
            languageManager,
            services,
            jedisPool,
            redisKeys,
            http
        )

        cinnamon.start()

        runBlocking {
            for ((shardId, gateway) in gateways) {
                logger.info { "Setting up and starting shard $shardId..." }
                gateway.installDiscordInteraKTions(cinnamon.interaKTions)

                scope.launch {
                    gateway.start(config.cinnamon.discord.token) {
                        @OptIn(PrivilegedIntent::class)
                        intents += Intent.MessageContent
                        @OptIn(PrivilegedIntent::class)
                        intents += Intent.GuildMembers
                        intents += Intent.DirectMessages
                        intents += Intent.DirectMessagesReactions
                        intents += Intent.GuildBans
                        intents += Intent.GuildEmojis
                        intents += Intent.GuildIntegrations
                        intents += Intent.GuildInvites
                        intents += Intent.GuildMessageReactions
                        intents += Intent.GuildMessages
                        intents += Intent.GuildVoiceStates
                        intents += Intent.GuildWebhooks
                        intents += Intent.Guilds

                        shard = DiscordShard(shardId, config.discordShards.totalShards)
                    }
                }
            }
        }
    }
}