package net.perfectdreams.loritta.deviousfun.gateway

import dev.kord.common.entity.ActivityType
import dev.kord.common.entity.DiscordShard
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.deviousfun.JDA
import net.perfectdreams.loritta.deviousfun.listeners.KordListener

class GatewayManager(
    val jda: JDA,
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
            val defaultGateway = DeviousGateway(jda, DefaultGateway {}, shardId)
            _gateways[shardId] = defaultGateway
        }
    }

    suspend fun start() {
        for ((shardId, gateway) in gateways) {
            scope.launch {
                logger.info { "Starting shard $shardId... Hang tight!" }
                gateway.kordGateway.start(token) {
                    @OptIn(PrivilegedIntent::class)
                    intents += Intents {
                        + Intent.GuildMembers
                        + Intent.MessageContent
                        + Intent.GuildEmojis
                        + Intent.GuildBans
                        + Intent.GuildInvites
                        + Intent.GuildMessageReactions
                        + Intent.GuildVoiceStates
                        + Intent.DirectMessages
                        + Intent.DirectMessagesReactions
                    }

                    presence {
                        this.status = jda.loritta.config.loritta.discord.status

                        val activityText = "${jda.loritta.config.loritta.discord.activity.name} | Cluster ${jda.loritta.lorittaCluster.id} [$shardId]"
                        when (jda.loritta.config.loritta.discord.activity.type) {
                            "PLAYING" -> this.playing(activityText)
                            "STREAMING" -> this.streaming(activityText, "https://twitch.tv/mrpowergamerbr")
                            "LISTENING" -> this.listening(activityText)
                            "WATCHING" -> this.watching(activityText)
                            "COMPETING" -> this.competing(activityText)
                            else -> error("I don't know how to handle ${jda.loritta.config.loritta.discord.activity.type}!")
                        }
                    }

                    shard = DiscordShard(shardId, totalShards)
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
    fun getGatewayForGuild(guildId: Snowflake) = getGatewayForGuildOrNull(guildId) ?: error("This instance does not handle guild $guildId!")

    /**
     * Gets a Gateway connection for the [shardId]
     *
     * @param shardId the shard's ID
     * @return a proxied gateway connection, or null if this instance does not handle the [shardId]
     */
    fun getGatewayForShardOrNull(shardId: Int) = gateways[shardId] ?: error("This instance does not handle shard $shardId!")

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