package net.perfectdreams.loritta.cinnamon.dashboard.backend.utils

import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse

object DiscordUtils {
    /**
     * Gets a Discord Shard ID from the provided Guild ID
     *
     * @return the shard ID
     */
    fun getLorittaClusterForGuildId(loritta: LorittaDashboardBackend, id: Long): LorittaInternalRPCResponse.GetLorittaInfoResponse.LorittaCluster {
        val shardId = getShardIdFromGuildId(loritta, id)
        return getLorittaClusterForShardId(loritta, shardId)
    }

    /**
     * Gets a Discord Shard ID from the provided Guild ID
     *
     * @return the shard ID
     */
    fun getShardIdFromGuildId(loritta: LorittaDashboardBackend, id: Long) = getShardIdFromGuildId(id, loritta.lorittaInfo.maxShards)

    /**
     * Gets a Discord Shard ID from the provided Guild ID
     *
     * @return the shard ID
     */
    fun getShardIdFromGuildId(id: Long, maxShards: Int) = (id shr 22).rem(maxShards).toInt()

    /**
     * Gets the cluster where the guild that has the specified ID is in
     *
     * @return the cluster
     */
    fun getLorittaClusterForShardId(loritta: LorittaDashboardBackend, id: Int): LorittaInternalRPCResponse.GetLorittaInfoResponse.LorittaCluster {
        val lorittaShard = loritta.lorittaInfo.instances.firstOrNull { id in it.minShard..it.maxShard }
        return lorittaShard ?: throw RuntimeException("Frick! I don't know what is the Loritta Shard for Discord Shard ID $id")
    }

    /**
     * Gets the cluster where the guild that has the specified ID is in
     *
     * @return the cluster ID
     */
    fun getLorittaClusterIdForShardId(loritta: LorittaDashboardBackend, id: Int) = getLorittaClusterForShardId(loritta, id).id

}