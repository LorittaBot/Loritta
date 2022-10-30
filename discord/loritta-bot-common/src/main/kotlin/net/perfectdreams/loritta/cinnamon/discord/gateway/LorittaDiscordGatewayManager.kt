package net.perfectdreams.loritta.cinnamon.discord.gateway

import dev.kord.common.entity.Snowflake
import dev.kord.gateway.Gateway
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong

abstract class LorittaDiscordGatewayManager(val totalShards: Int) {
    abstract val gateways: Map<Int, Gateway>

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
        getGatewayForGuildOrNull(guildId) ?: error("This instance does not handle $guildId!")

    /**
     * Gets a Gateway connection for the [shardId]
     *
     * @param shardId the shard's ID
     * @return a proxied gateway connection, or null if this instance does not handle the [shardId]
     */
    abstract fun getGatewayForShardOrNull(shardId: Int): Gateway?

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