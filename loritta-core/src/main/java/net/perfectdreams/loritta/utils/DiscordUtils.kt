package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.utils.loritta

object DiscordUtils {
	fun getShardIdFromGuildId(id: Long): Long {
		val maxShard = loritta.discordConfig.discord.maxShards
		return (id shr 22).rem(maxShard)
	}

	fun getLorittaShardIdForShardId(id: Long): Long {
		val lorittaShard = loritta.config.shards.firstOrNull { id in it.minShard..it.maxShard }
		return lorittaShard?.id ?: throw RuntimeException("Frick! I don't know what is the Loritta Shard for Discord Shard ID $id")
	}

	fun getLorittaUrlForLorittaShardId(id: Long): String {
		if (id == 1L)
			return loritta.config.loritta.website.url.substring(loritta.config.loritta.website.url.indexOf("//") + 2).removeSuffix("/")

		return loritta.discordConfig.discord.shardUrl.format(id)
	}
}