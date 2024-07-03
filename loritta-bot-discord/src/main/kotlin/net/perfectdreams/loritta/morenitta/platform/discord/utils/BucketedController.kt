package net.perfectdreams.loritta.morenitta.platform.discord.utils

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
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
		private val logger = KotlinLogging.logger {}
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

	override fun setGlobalRatelimit(ratelimit: Long) {
		super.setGlobalRatelimit(ratelimit)

		// Após marcar o novo global ratelimit, iremos adicionar em uma lista de quantos ratelimits já recebemos neste minuto
		// Remover todos os ratelimits que foram atingidos a mais de 10 minutos
		// https://i.imgur.com/crENfcG.png
		runBlocking {
			rateLimitListMutex.withLock {
				removeOutdatedGlobalRateLimitHits()

				rateLimits.add(
					RateLimitHit(
						ratelimit,
						System.currentTimeMillis()
					)
				)
			}
		}

		val diff = System.currentTimeMillis() - lastTooManyRequestsCheck
		if (diff >= 15_000) {
			logger.info { "Doing self too many requests check... Last check was ${diff}ms ago" }
			lastTooManyRequestsCheck = System.currentTimeMillis()
		}
	}

	fun getGlobalRateLimitHitsInTheLastMinute(): Int {
		return runBlocking {
			rateLimitListMutex.withLock {
				return@runBlocking rateLimits.size
			}
		}
	}

	fun removeOutdatedGlobalRateLimitHits() {
		val activeRateLimits = rateLimits.filter { (10 * 60_000) > (System.currentTimeMillis() - it.hitAt) }
		rateLimits.clear()
		rateLimits.addAll(activeRateLimits)
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