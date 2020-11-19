package com.mrpowergamerbr.loritta.utils.debug

import com.mrpowergamerbr.loritta.listeners.EventLogListener
import com.mrpowergamerbr.loritta.modules.InviteLinkModule
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.debug.DebugProbes
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.LorittaWebsite
import java.io.File
import java.io.PrintStream
import java.lang.management.ManagementFactory
import java.util.concurrent.ThreadPoolExecutor
import kotlin.concurrent.thread

object DebugLog {
	private val logger = KotlinLogging.logger {}
	var cancelAllEvents = false
		get() {
			if (field)
				logger.warn { "All received events are cancelled and ignored!" }
			return field
		}

	fun startCommandListenerThread() {
		thread {
			commandLoop@ while (true) {
				try {
					val line = readLine() ?: continue
					handleLine(line)
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
		}
	}

	fun showExtendedInfo() {
		val mb = 1024 * 1024
		val runtime = Runtime.getRuntime()

		logger.info("===[ EXTENDED INFO ]===")
		logger.info("Used Memory:"
				+ (runtime.totalMemory() - runtime.freeMemory()) / mb)
		logger.info("Free Memory:"
				+ runtime.freeMemory() / mb)
		logger.info("Total Memory:" + runtime.totalMemory() / mb)
		logger.info("Max Memory:" + runtime.maxMemory() / mb)
		logger.info("coroutineExecutor: ${(loritta.coroutineExecutor as ThreadPoolExecutor).activeCount}")
		logger.info("Pending Requests: ${loritta.rateLimitChecker.getAllPendingRequests().size}")
		logger.info("Global Rate Limit Hits in the last 10m: ${loritta.bucketedController?.getGlobalRateLimitHitsInTheLastMinute()} / ${loritta.discordConfig.discord.requestLimiter.maxRequestsPer10Minutes}")
		logger.info("> Command Stuff")
		logger.info("commandManager.commandMap.size: ${loritta.legacyCommandManager.commandMap.size}")
		logger.info("messageInteractionCache.size: ${loritta.messageInteractionCache.size}")
		logger.info("locales.size: ${loritta.legacyLocales.size}")
		logger.info("ignoreIds.size: ${loritta.ignoreIds.size}")
		logger.info("> Tasks Stuff")
		logger.info("loritta.twitch.cachedGames: ${loritta.twitch.cachedGames.size}")
		logger.info("loritta.twitch.cachedStreamerInfo: ${loritta.twitch.cachedStreamerInfo.size}")
		logger.info("gameInfoCache.size: ${loritta.twitch.cachedGames.size}")
		logger.info("> Invite Stuff")
		logger.info("cachedInviteLinks.size: ${InviteLinkModule.cachedInviteLinks.size}")
		logger.info("> Misc Stuff")
		logger.info("fanArts.size: ${loritta.fanArts.size}")
		logger.info("eventLogListener.downloadedAvatarJobs: ${EventLogListener.downloadedAvatarJobs}")
		logger.info("Cached Retrieved Users: ${lorittaShards.cachedRetrievedUsers.size()}")
		logger.info("> Executors")

		val pendingMessagesSize = loritta.pendingMessages.size
		val availableProcessors = LorittaDiscord.MESSAGE_EXECUTOR_THREADS
		val isMessagesOverloaded = pendingMessagesSize > availableProcessors
		logger.info("Pending Messages ($pendingMessagesSize): Active: ${loritta.pendingMessages.filter { it.isActive }.count()}; Cancelled: ${loritta.pendingMessages.filter { it.isCancelled }.count()}; Complete: ${loritta.pendingMessages.filter { it.isCompleted }.count()};")
		if (isMessagesOverloaded)
			logger.warn { "Loritta is overloaded! There are $pendingMessagesSize messages pending to be executed, ${pendingMessagesSize - availableProcessors} more than it should be!" }
	}

	fun dumpCoroutinesToFile() {
		println("Dumping Coroutines...")
		DebugProbes.dumpCoroutines(
				PrintStream(
						File("coroutines_dump.txt")
								.outputStream()
				)
		)
		println("Coroutines dumped!")
	}

	fun handleLine(line: String) {
		val args = line.split(" ").toMutableList()
		val command = args[0]
		args.removeAt(0)

		when (command) {
			"toggleevents", "te" -> {
				val toggleState = args.getOrNull(0)?.toBoolean() ?: !cancelAllEvents
				cancelAllEvents = toggleState

				println("Cancel all events: $cancelAllEvents")
			}
			"info" -> {
				val mb = 1024 * 1024
				val runtime = Runtime.getRuntime()
				println("===[ INFO ]===")
				println("Shards: ${lorittaShards.getShards().size}")
				println("Total Servers: ${lorittaShards.getGuildCount()}")
				println("Used Memory:"
						+ (runtime.totalMemory() - runtime.freeMemory()) / mb)
				println("Free Memory:"
						+ runtime.freeMemory() / mb)
				println("Total Memory:" + runtime.totalMemory() / mb)
				println("Max Memory:" + runtime.maxMemory() / mb)
			}
			"shards" -> {
				val shards = lorittaShards.getShards()

				for (shard in shards.sortedByDescending { it.shardInfo.shardId }) {
					println("SHARD ${shard.shardInfo.shardId} (${shard.status.name} - ${shard.gatewayPing}ms): ${shard.guilds.size} guilds - ${shard.users.size} members")
				}
			}
			"extendedinfo" -> {
				showExtendedInfo()
			}
			"threads" -> {
				println("===[ ACTIVE THREADS ]===")
				println("coroutineExecutor: ${(loritta.coroutineExecutor as ThreadPoolExecutor).activeCount}")
				println("Total Thread Count: ${ManagementFactory.getThreadMXBean().threadCount}")
			}
			"bomdiaecia" -> {
				loritta.bomDiaECia.handleBomDiaECia(true)
			}
			"posts" -> {
				LorittaWebsite.INSTANCE.blog.posts = LorittaWebsite.INSTANCE.blog.loadAllBlogPosts()
			}
			"dumpc" -> {
				dumpCoroutinesToFile()
			}
		}
	}
}