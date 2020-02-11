package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.utils.config.GeneralConfig
import com.mrpowergamerbr.loritta.utils.loritta

object DiscordUtils {
	/**
	 * Gets a Discord Shard ID from the provided Guild ID
	 *
	 * @return the shard ID
	 */
	fun getLorittaClusterForGuildId(id: Long): GeneralConfig.LorittaClusterConfig {
		val shardId = getShardIdFromGuildId(id)
		return getLorittaClusterForShardId(shardId)
	}

	/**
	 * Gets a Discord Shard ID from the provided Guild ID
	 *
	 * @return the shard ID
	 */
	fun getShardIdFromGuildId(id: Long): Long {
		val maxShard = loritta.discordConfig.discord.maxShards
		return (id shr 22).rem(maxShard)
	}

	/**
	 * Gets the cluster where the guild that has the specified ID is in
	 *
	 * @return the cluster
	 */
	fun getLorittaClusterForShardId(id: Long): GeneralConfig.LorittaClusterConfig {
		val lorittaShard = loritta.config.clusters.firstOrNull { id in it.minShard..it.maxShard }
		return lorittaShard ?: throw RuntimeException("Frick! I don't know what is the Loritta Shard for Discord Shard ID $id")
	}

	/**
	 * Gets the cluster where the guild that has the specified ID is in
	 *
	 * @return the cluster ID
	 */
	fun getLorittaClusterIdForShardId(id: Long) = getLorittaClusterForShardId(id).id

	/**
	 * Gets the URL for the specified Loritta Cluster
	 *
	 * @return the url in a "test.example.com" format
	 */
	fun getUrlForLorittaClusterId(id: Long): String {
		if (id == 1L)
			return loritta.instanceConfig.loritta.website.url.substring(loritta.instanceConfig.loritta.website.url.indexOf("//") + 2).removeSuffix("/")

		return loritta.instanceConfig.loritta.website.clusterUrl.format(id)
	}
}