package net.perfectdreams.loritta.morenitta.platform.discord.utils

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.utils.SessionController
import net.dv8tion.jda.api.utils.SessionController.SessionConnectNode
import net.dv8tion.jda.api.utils.SessionControllerAdapter
import net.perfectdreams.loritta.morenitta.LorittaBot
import java.util.concurrent.Semaphore
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnegative

/**
 * A Shard Controller used for bots using the "very large bots" sharding system, splitting the shard logins into buckets (login pools)
 *
 * Thanks Mantaro! https://github.com/Mantaro/MantaroBot/blob/0abd5d98af728e24a5b0fb4a0ad63fc451ef8d0f/src/main/java/net/kodehawa/mantarobot/core/shard/jda/BucketedController.java
 */
class BucketedController(val loritta: LorittaBot, @Nonnegative bucketFactor: Int = 16, @Nonnegative val maxParallelLogins: Int = 16) : SessionControllerAdapter() {
	companion object {
		private val logger by HarmonyLoggerFactory.logger {}
	}

	private val shardControllers: Array<SessionController?>
	private val rateLimits = mutableListOf<RateLimitHit>()
	private val rateLimitListMutex = Mutex()
	private var lastTooManyRequestsCheck = -1L
	val parallelLoginsSemaphore = Semaphore(maxParallelLogins)

	init {
		require(bucketFactor >= 1) { "Bucket factor must be at least 1" }
		shardControllers = arrayOfNulls(bucketFactor)
		for (i in 0 until bucketFactor) {
			shardControllers[i] = LoriMasterShardControllerSessionControllerAdapter(loritta, this)
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

	private data class RateLimitHit(
		val wait: Long,
		val hitAt: Long
	)
}