package net.perfectdreams.loritta.morenitta.platform.discord.utils

import com.neovisionaries.ws.client.OpeningHandshakeException
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.utils.SessionController
import net.dv8tion.jda.api.utils.SessionController.SessionConnectNode
import net.dv8tion.jda.api.utils.SessionControllerAdapter
import net.perfectdreams.loritta.cinnamon.pudding.tables.ConcurrentLoginBuckets
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.listeners.PreStartGatewayEventReplayListener
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import java.time.Instant
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Session Controller for bots migrated to the "Very Large Bots" sharding system
 *
 * This controller asks to the master shard controller if a specific shard can login.
 *
 * Thanks Nik#1234 and Xavinlol#0001 for the help!
 */
class LoriMasterShardControllerSessionControllerAdapter(val loritta: LorittaBot, val bucketedController: BucketedController) : SessionControllerAdapter() {
	override fun runWorker() {
		synchronized(lock) {
			if (workerHandle == null) {
				workerHandle = QueueWorker()
				workerHandle!!.start()
			}
		}
	}

	/**
	 * Creates a QueueWorker
	 *
	 * @param delay
	 * delay (in milliseconds) to wait between starting sessions
	 */
	private inner class QueueWorker(
		/** Delay (in milliseconds) to sleep between connecting sessions  */
		protected val delay: Long
	) : Thread("SessionControllerAdapter-Worker") {
		/**
		 * Creates a QueueWorker
		 *
		 * @param delay
		 * delay (in seconds) to wait between starting sessions
		 */
		@JvmOverloads
		constructor(delay: Int = SessionController.IDENTIFY_DELAY) : this(TimeUnit.SECONDS.toMillis(delay.toLong()))

		protected fun handleFailure(thread: Thread?, exception: Throwable?) {
			log.error("Worker has failed with throwable!", exception)
		}

		override fun run() {
			try {
				if (delay > 0) {
					val interval = System.currentTimeMillis() - lastConnect
					if (interval < delay) sleep(delay - interval)
				}
			} catch (ex: InterruptedException) {
				log.error("Unable to backoff", ex)
			}
			processQueue()
			synchronized(lock) {
				workerHandle = null
				if (!connectQueue.isEmpty()) runWorker()
			}
		}

		protected fun processQueue() {
			val reconnectingShards = LinkedBlockingQueue<SessionConnectNode>()
			val startingFromScratchShards = LinkedBlockingQueue<SessionConnectNode>()

			// Prioritize shards that are reconnecting
			while (connectQueue.isNotEmpty()) {
				val node = connectQueue.poll()
				if (node.isReconnect || loritta.preLoginStates[node.shardInfo.shardId]?.value == PreStartGatewayEventReplayListener.ProcessorState.WAITING_FOR_WEBSOCKET_CONNECTION) {
					reconnectingShards.add(node)
				} else {
					startingFromScratchShards.add(node)
				}
			}

			while (reconnectingShards.isNotEmpty()) {
				val node = reconnectingShards.poll()

				// Just a shard resuming
				try {
					node.run(false)
					lastConnect = System.currentTimeMillis()
				} catch (e: IllegalStateException) {
					val t = e.cause
					if (t is OpeningHandshakeException) log.error("Failed opening handshake, appending to queue. Message: {}", e.message) else log.error("Failed to establish connection for a node, appending to queue", e)
					appendSession(node)
				} catch (e: InterruptedException) {
					log.error("Failed to run node", e)
					appendSession(node)
					return  // caller should start a new thread
				}
			}

			while (startingFromScratchShards.isNotEmpty()) {
				val node = startingFromScratchShards.poll()

				val bucketId = node.shardInfo.shardId % loritta.config.loritta.discord.maxConcurrency

				// On this instance, can we login?
				val acquired = bucketedController.parallelLoginsSemaphore.tryAcquire()

				if (acquired) {
					try {
						// PostgreSQL should handle conflicts by itself, so if two instances try to edit the same column at the same time, a concurrent modification exception will happen
						// If randomKey == null, then this bucket is already being used
						// If randomKey != null, then this bucket isn't being used
						val randomKey = runBlocking {
							// We want to repeat indefinitely if a concurrent modification error happens
							loritta.pudding.transaction(repetitions = Int.MAX_VALUE) {
								val currentStatus = ConcurrentLoginBuckets.selectAll().where {
									ConcurrentLoginBuckets.id eq bucketId and (ConcurrentLoginBuckets.lockedAt greaterEq Instant.now()
										.minusSeconds(60))
								}.firstOrNull()

								if (currentStatus != null) {
									return@transaction null
								} else {
									val newRandomKey = Base64.getEncoder().encodeToString(Random.Default.nextBytes(20))

									ConcurrentLoginBuckets.upsert(ConcurrentLoginBuckets.id) {
										it[ConcurrentLoginBuckets.id] = bucketId
										it[ConcurrentLoginBuckets.randomKey] = newRandomKey
										it[ConcurrentLoginBuckets.lockedAt] = Instant.now()
									}

									return@transaction newRandomKey
								}
							}
						}

						if (randomKey == null) {
							log.info("Shard ${node.shardInfo.shardId} (login pool: $bucketId) can't login! Another cluster is logging in that shard, delaying login...")
							// We don't want to sleep for "delay" because the login lock may be released sooner, while the default 5s delay is only applicable for successful logins
							// So let's wait for 250ms instead
							sleep(250)
							appendSession(node)
							continue
						}

						try {
							node.run(false)

							lastConnect = System.currentTimeMillis()
							if (delay > 0) sleep(delay)
							runBlocking {
								val deletedBucketsCount = loritta.pudding.transaction {
									ConcurrentLoginBuckets.deleteWhere { ConcurrentLoginBuckets.id eq bucketId and (ConcurrentLoginBuckets.randomKey eq randomKey) }
								}
								when (deletedBucketsCount) {
									0 -> log.warn("Couldn't release lock for bucket $bucketId (shard ${node.shardInfo.shardId}) because our random key does not match or the bucket was already released!")
									else -> log.info("Successfully released lock for bucket $bucketId (shard ${node.shardInfo.shardId})!")
								}
							}
						} catch (e: IllegalStateException) {
							val t = e.cause
							if (t is OpeningHandshakeException) log.error(
								"Failed opening handshake, appending to queue. Message: {}",
								e.message
							) else log.error("Failed to establish connection for a node, appending to queue", e)
							appendSession(node)
							runBlocking {
								val deletedBucketsCount = loritta.pudding.transaction {
									ConcurrentLoginBuckets.deleteWhere { ConcurrentLoginBuckets.id eq bucketId and (ConcurrentLoginBuckets.randomKey eq randomKey) }
								}
								when (deletedBucketsCount) {
									0 -> log.warn("Couldn't release lock for bucket $bucketId (shard ${node.shardInfo.shardId}) because our random key does not match or the bucket was already released!")
									else -> log.info("Successfully released lock for bucket $bucketId (shard ${node.shardInfo.shardId})!")
								}
							}
						} catch (e: InterruptedException) {
							log.error("Failed to run node", e)
							appendSession(node)
							runBlocking {
								val deletedBucketsCount = loritta.pudding.transaction {
									ConcurrentLoginBuckets.deleteWhere { ConcurrentLoginBuckets.id eq bucketId and (ConcurrentLoginBuckets.randomKey eq randomKey) }
								}
								when (deletedBucketsCount) {
									0 -> log.warn("Couldn't release lock for bucket $bucketId (shard ${node.shardInfo.shardId}) because our random key does not match or the bucket was already released!")
									else -> log.info("Successfully released lock for bucket $bucketId (shard ${node.shardInfo.shardId})!")
								}
							}
							return  // caller should start a new thread
						}
					} finally {
						bucketedController.parallelLoginsSemaphore.release()
					}
				} else {
					log.info("Shard ${node.shardInfo.shardId} (login pool: $bucketId) can't login! This instance has hit the max parallel logins ${bucketedController.parallelLoginsSemaphore.availablePermits()}/${bucketedController.maxParallelLogins}, delaying login...")
					// We don't want to sleep for "delay" because the login lock may be released sooner, while the default 5s delay is only applicable for successful logins
					// So let's wait for 250ms instead
					sleep(250)
					appendSession(node)
				}
			}

			// Then we do this all over again!
			if (connectQueue.isNotEmpty())
				processQueue()
		}

		init {
			super.setUncaughtExceptionHandler { thread: Thread?, exception: Throwable? -> handleFailure(thread, exception) }
		}
	}
}