package net.perfectdreams.loritta.deviousfun.gateway

import dev.kord.common.entity.DiscordShard
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.*
import dev.kord.gateway.retry.LinearRetry
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.deviousfun.DeviousFun
import kotlin.time.Duration.Companion.seconds

class GatewayManager(
    val deviousFun: DeviousFun,
    val token: String,
    minShards: Int,
    maxShards: Int,
    val totalShards: Int
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val _gateways = mutableMapOf<Int, DeviousGateway>()
    val gateways: Map<Int, DeviousGateway>
        get() = _gateways

    val scope = CoroutineScope(Dispatchers.Default)

    init {
        for (shardId in minShards..maxShards) {
            val identifyRateLimiter = ParallelIdentifyRateLimiter(
                deviousFun.loritta,
                shardId,
                shardId % deviousFun.loritta.config.loritta.discord.maxConcurrency
            )

            val defaultGateway = DeviousGateway(
                deviousFun,
                DefaultGateway {
                    // The default reconnectRetry is 10, but let's try reconnecting indefinitely (well, kind of, it will try reconnecting MAX_VALUE times)
                    this.reconnectRetry = LinearRetry(2.seconds, 20.seconds, Int.MAX_VALUE)

                    this.identifyRateLimiter = identifyRateLimiter
                },
                identifyRateLimiter,
                shardId
            )
            _gateways[shardId] = defaultGateway
        }
    }

    suspend fun start() {
        for ((shardId, gateway) in gateways) {
            scope.launch {
                val gatewaySession = deviousFun.cacheManager.gatewaySessions[shardId]

                val sessionId = gatewaySession?.sessionId
                val resumeGatewayUrl = gatewaySession?.resumeGatewayUrl
                val sequence = gatewaySession?.sequence

                val builder: GatewayConfigurationBuilder.() -> (Unit) = {
                    @OptIn(PrivilegedIntent::class)
                    intents = Intents {
                        +Intent.Guilds
                        +Intent.GuildMembers
                        +Intent.MessageContent
                        +Intent.GuildEmojis
                        +Intent.GuildBans
                        +Intent.GuildInvites
                        +Intent.GuildMessageReactions
                        +Intent.GuildVoiceStates
                        +Intent.GuildMessages
                        +Intent.DirectMessages
                        +Intent.DirectMessagesReactions
                    }

                    presence {
                        deviousFun.createDefaultPresence(shardId).invoke(this)
                    }

                    shard = DiscordShard(shardId, totalShards)
                }

                if (sessionId != null && resumeGatewayUrl != null && sequence != null) {
                    logger.info { "Resuming shard $shardId... Hang tight!" }
                    gateway.status.value = DeviousGateway.Status.RESUMING
                    gateway.kordGateway.resume(token, GatewaySession(sessionId, resumeGatewayUrl, sequence), builder)
                } else {
                    logger.info { "Starting shard $shardId... Hang tight!" }
                    gateway.status.value = DeviousGateway.Status.WAITING_TO_CONNECT
                    gateway.kordGateway.start(token, builder)
                }
            }
        }
    }

    /**
     * Gets a Gateway connection related to the [guildId], by converting the [guildId] into a Shard ID
     *
     * @param guildId the guild's ID
     * @return a proxied gateway connection, or null if this instance does not handle the [guildId]
     */
    fun getGatewayForGuildOrNull(guildId: Snowflake) = getGatewayForShardOrNull(getShardIdFromGuildId(guildId.toLong()))

    /**
     * Gets a Gateway connection related to the [guildId], by converting the [guildId] into a Shard ID
     *
     * @param guildId the guild's ID
     * @return a proxied gateway connection, or null if this instance does not handle the [guildId]
     */
    fun getGatewayForGuild(guildId: Snowflake) =
        getGatewayForGuildOrNull(guildId) ?: error("This instance does not handle guild $guildId!")

    /**
     * Gets a Gateway connection for the [shardId]
     *
     * @param shardId the shard's ID
     * @return a proxied gateway connection, or null if this instance does not handle the [shardId]
     */
    fun getGatewayForShardOrNull(shardId: Int) = gateways[shardId]

    /**
     * Gets a Gateway connection for the [shardId]
     *
     * @param shardId the shard's ID
     * @return a proxied gateway connection, or null if this instance does not handle the [shardId]
     */
    fun getGatewayForShard(shardId: Int) = getGatewayForShardOrNull(shardId) ?: error("This instance does not handle shard $shardId!")

    /**
     * Gets a Discord Shard ID from the provided Guild ID
     *
     * @return the shard ID
     */
    private fun getShardIdFromGuildId(id: Long): Int {
        val maxShard = totalShards
        return (id shr 22).rem(maxShard).toInt()
    }
}