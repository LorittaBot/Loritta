package net.perfectdreams.loritta.platform.discord.utils

import com.mrpowergamerbr.loritta.utils.LorittaShards
import mu.KotlinLogging
import net.dv8tion.jda.api.utils.SessionController
import net.dv8tion.jda.api.utils.SessionController.SessionConnectNode
import net.dv8tion.jda.api.utils.SessionControllerAdapter
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnegative

/**
 * A Shard Controller used for bots using the "very large bots" sharding system, splitting the shard logins into buckets (login pools)
 *
 * Thanks Mantaro! https://github.com/Mantaro/MantaroBot/blob/0abd5d98af728e24a5b0fb4a0ad63fc451ef8d0f/src/main/java/net/kodehawa/mantarobot/core/shard/jda/BucketedController.java
 */
class BucketedController(val lorittaShards: LorittaShards, @Nonnegative bucketFactor: Int = 16) : SessionControllerAdapter() {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	private val shardControllers: Array<SessionController?>

	override fun setGlobalRatelimit(ratelimit: Long) {
		setGlobalRatelimitWithoutRelay(ratelimit) // Primeiro vamos marcar o rate limit deste cluster

		if (ratelimit > 0) { // Não precisamos sincronizar se for apenas reset, os outros clusters vão resetar sozinho quando necessário
			val currentRatelimit = globalRatelimit.get()

			if (ratelimit > currentRatelimit) { // As vezes vários requests podem atingir o global ratelimit em sequência, não iremos fazer relay se o valor for menor
				logger.info { "Relaying Global Rate Limit status to other clusters..." }
				lorittaShards.queryAllLorittaClusters("/api/v1/loritta/global-rate-limit/$ratelimit")
			}
		}
	}

	fun setGlobalRatelimitWithoutRelay(ratelimit: Long) {
		globalRatelimit.set(ratelimit)
	}

	init {
		require(bucketFactor >= 1) { "Bucket factor must be at least 1" }
		shardControllers = arrayOfNulls(bucketFactor)
		for (i in 0 until bucketFactor) {
			shardControllers[i] = LoriMasterShardControllerSessionControllerAdapter()
		}
	}

	override fun appendSession(node: SessionConnectNode) {
		controllerFor(node)!!.appendSession(node)
	}

	override fun removeSession(node: SessionConnectNode) {
		controllerFor(node)!!.removeSession(node)
	}

	@CheckReturnValue
	private fun controllerFor(node: SessionConnectNode): SessionController? {
		return shardControllers[node.shardInfo.shardId % shardControllers.size]
	}
}