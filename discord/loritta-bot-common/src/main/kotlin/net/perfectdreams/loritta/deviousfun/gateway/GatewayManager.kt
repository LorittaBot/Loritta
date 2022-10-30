package net.perfectdreams.loritta.deviousfun.gateway

import dev.kord.common.entity.DiscordShard
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.*
import dev.kord.gateway.retry.LinearRetry
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.deviouscache.requests.GetGatewaySessionRequest
import net.perfectdreams.loritta.deviouscache.responses.GetGatewaySessionResponse
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
                val gatewaySession = deviousFun.rpc.execute(GetGatewaySessionRequest(shardId))

                val sessionId = (gatewaySession as? GetGatewaySessionResponse)?.sessionId
                val resumeGatewayUrl = (gatewaySession as? GetGatewaySessionResponse)?.resumeGatewayUrl
                val sequence = (gatewaySession as? GetGatewaySessionResponse)?.sequence

                val builder: GatewayConfigurationBuilder.() -> (Unit) = {
                    @OptIn(PrivilegedIntent::class)
                    intents += Intents {
                        +Intent.GuildMembers
                        +Intent.MessageContent
                        +Intent.GuildEmojis
                        +Intent.GuildBans
                        +Intent.GuildInvites
                        +Intent.GuildMessageReactions
                        +Intent.GuildVoiceStates
                        +Intent.DirectMessages
                        +Intent.DirectMessagesReactions
                    }

                    presence {
                        this.status = deviousFun.loritta.config.loritta.discord.status

                        val activityText =
                            "${deviousFun.loritta.config.loritta.discord.activity.name} | Cluster ${deviousFun.loritta.lorittaCluster.id} [$shardId]"
                        when (deviousFun.loritta.config.loritta.discord.activity.type) {
                            "PLAYING" -> this.playing(activityText)
                            "STREAMING" -> this.streaming(activityText, "https://twitch.tv/mrpowergamerbr")
                            "LISTENING" -> this.listening(activityText)
                            "WATCHING" -> this.watching(activityText)
                            "COMPETING" -> this.competing(activityText)
                            else -> error("I don't know how to handle ${deviousFun.loritta.config.loritta.discord.activity.type}!")
                        }
                    }

                    shard = DiscordShard(shardId, totalShards)
                }

                if (sessionId != null && resumeGatewayUrl != null && sequence != null) {
                    logger.info { "Resuming shard $shardId... Hang tight!" }
                    gateway.kordGateway.resume(token, GatewaySession(sessionId, resumeGatewayUrl, sequence), builder)
                } else {
                    logger.info { "Starting shard $shardId... Hang tight!" }
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
    fun getGatewayForShardOrNull(shardId: Int) =
        gateways[shardId] ?: error("This instance does not handle shard $shardId!")

    /**
     * Gets a Gateway connection for the [shardId]
     *
     * @param shardId the shard's ID
     * @return a proxied gateway connection, or null if this instance does not handle the [shardId]
     */
    fun getGatewayForShard(shardId: Int) =
        getGatewayForShardOrNull(shardId) ?: error("This instance does not handle shard $shardId!")

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