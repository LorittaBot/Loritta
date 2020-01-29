package net.perfectdreams.loritta.platform.discord.utils

import com.mrpowergamerbr.loritta.utils.loritta
import com.neovisionaries.ws.client.OpeningHandshakeException
import io.ktor.client.request.delete
import io.ktor.client.request.put
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.userAgent
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.utils.SessionController
import net.dv8tion.jda.api.utils.SessionControllerAdapter
import net.perfectdreams.loritta.utils.NetAddressUtils
import java.util.concurrent.TimeUnit

/**
 * Session Controller for bots migrated to the "Very Large Bots" sharding system
 *
 * This controller asks to the master shard controller if a specific shard can login.
 *
 * Thanks Nik#1234 and Xavinlol#0001 for the help!
 */
class LoriMasterShardControllerSessionControllerAdapter : SessionControllerAdapter() {
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
			while (!connectQueue.isEmpty()) {
				val node = connectQueue.poll()

				fun setLoginPoolLockToShardController(): ControllerResponseType {
					return runBlocking {
						try {
							val status = loritta.http.put<HttpResponse>("http://${NetAddressUtils.fixIp(loritta.discordConfig.shardController.url)}/api/v1/shard/${node.shardInfo.shardId}") {
								userAgent(loritta.lorittaCluster.getUserAgent())
							}.status

							if (status == HttpStatusCode.OK)
								ControllerResponseType.OK
							else if (status == HttpStatusCode.Conflict)
								ControllerResponseType.CONFLICT
							else {
								log.error("Weird status code while fetching shard ${node.shardInfo.shardId} login pool status, status code: ${status}")
								ControllerResponseType.OFFLINE
							}
						} catch (e: Exception) {
							log.error("Exception while checking if shard ${node.shardInfo.shardId} can login", e)
							ControllerResponseType.OFFLINE
						}
					}
				}

				fun removeLoginPoolLockFromShardController() {
					runBlocking {
						try {
							loritta.http.delete<HttpResponse>("http://${NetAddressUtils.fixIp(loritta.discordConfig.shardController.url)}/api/v1/shard/${node.shardInfo.shardId}") {
								userAgent(loritta.lorittaCluster.getUserAgent())
							}
						} catch (e: Exception) {
							log.error("Exception while telling master shard controller that shard ${node.shardInfo.shardId} already logged in! Other clusters may have temporary issues while logging in...", e)
						}
					}
				}

				val canLogin = setLoginPoolLockToShardController()

				if (canLogin == ControllerResponseType.CONFLICT) {
					log.info("Shard ${node.shardInfo.shardId} (login pool: ${node.shardInfo.shardId % 16}) can't login! Another cluster is logging in that shard, delaying login...")
					if (delay > 0) sleep(delay)
					appendSession(node)
					continue
				}

				try {
					node.run(false)

					lastConnect = System.currentTimeMillis()
					if (delay > 0) sleep(delay)
					removeLoginPoolLockFromShardController()
				} catch (e: IllegalStateException) {
					val t = e.cause
					if (t is OpeningHandshakeException) log.error("Failed opening handshake, appending to queue. Message: {}", e.message) else log.error("Failed to establish connection for a node, appending to queue", e)
					appendSession(node)
					removeLoginPoolLockFromShardController()
				} catch (e: InterruptedException) {
					log.error("Failed to run node", e)
					appendSession(node)
					removeLoginPoolLockFromShardController()
					return  // caller should start a new thread
				}
			}
		}

		init {
			super.setUncaughtExceptionHandler { thread: Thread?, exception: Throwable? -> handleFailure(thread, exception) }
		}
	}

	enum class ControllerResponseType {
		OK,
		CONFLICT,
		OFFLINE
	}
}