package net.perfectdreams.loritta.deviousfun

import dev.kord.common.entity.Snowflake
import dev.kord.gateway.Event
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway
import net.perfectdreams.loritta.deviousfun.hooks.ListenerAdapter

class DeviousShards(
    val maxShards: Int,
    shards: List<DeviousShard>
) {
    val shards = shards.associateBy {
        it.shardId
    }

    // To avoid suspending while there are shards with a null cache manager, we will only get shards that have a non-null cache manager
    // However, this may still suspend due to race conditions
    val connectedShards = shards.filter { it.cacheManagerDoNotUseThisUnlessIfYouKnowWhatYouAreDoing.value != null }

    fun registerListeners(vararg listeners: ListenerAdapter) = shards.values.forEach {
        it.registerListeners(*listeners)
    }

    suspend fun getUserById(id: Snowflake) = connectedShards.firstNotNullOfOrNull {
        it.getUserById(id)
    }

    suspend fun retrieveUserOrNullById(id: Snowflake) = connectedShards.firstNotNullOfOrNull {
        it.getUserById(id)
    }

    suspend fun getGuildById(id: String) = connectedShards.firstNotNullOfOrNull {
        it.getGuildById(id)
    }

    suspend fun getGuildById(id: Long) = connectedShards.firstNotNullOfOrNull {
        it.getGuildById(id)
    }

    suspend fun getGuildById(id: Snowflake) = connectedShards.firstNotNullOfOrNull {
        it.getGuildById(id)
    }

    suspend fun getChannelById(id: Long) = connectedShards.firstNotNullOfOrNull {
        it.getChannelById(id)
    }

    suspend fun getChannelById(id: Snowflake) = connectedShards.firstNotNullOfOrNull {
        it.getChannelById(id)
    }

    suspend fun getGuildCount() = connectedShards.sumOf { it.getGuildCount() }

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
    fun getGatewayForShardOrNull(shardId: Int) = shards[shardId]?.deviousGateway

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
        val maxShard = maxShards
        return (id shr 22).rem(maxShard).toInt()
    }
}