package com.mrpowergamerbr.loritta.utils.debug

import com.mongodb.Mongo
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.amino.AminoRepostTask
import com.mrpowergamerbr.loritta.listeners.EventLogListener
import com.mrpowergamerbr.loritta.modules.InviteLinkModule
import com.mrpowergamerbr.loritta.threads.NewRssFeedTask
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.debug.DebugProbes
import mu.KotlinLogging
import net.perfectdreams.loritta.website.LorittaWebsite
import java.io.File
import java.io.PrintStream
import java.lang.management.ManagementFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicInteger
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
					val line = readLine()!!
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
		logger.info("> Command Stuff")
		logger.info("commandManager.commandMap.size: ${loritta.legacyCommandManager.commandMap.size}")
		logger.info("commandManager.defaultCmdOptions.size: ${loritta.legacyCommandManager.defaultCmdOptions.size}")
		logger.info("messageInteractionCache.size: ${loritta.messageInteractionCache.size}")
		logger.info("locales.size: ${loritta.legacyLocales.size}")
		logger.info("ignoreIds.size: ${loritta.ignoreIds.size}")
		logger.info("userCooldown.size: ${loritta.userCooldown.size}")
		logger.info("> Music Stuff")
		logger.info("musicManagers.size: ${loritta.audioManager.musicManagers.size}")
		logger.info("songThrottle.size: ${loritta.audioManager.songThrottle.size}")
		logger.info("youTubeKeys.size: ${loritta.youtubeKeys.size}")
		logger.info("> Tasks Stuff")
		logger.info("storedLastEntries.size: ${AminoRepostTask.storedLastIds.size}")
		logger.info("loritta.twitch.cachedGames: ${loritta.twitch.cachedGames.size}")
		logger.info("loritta.twitch.cachedStreamerInfo: ${loritta.twitch.cachedStreamerInfo.size}")
		logger.info("gameInfoCache.size: ${loritta.twitch.cachedGames.size}")
		logger.info("storedLastEntries.size: ${NewRssFeedTask.storedLastEntries.size}")
		logger.info("> Invite Stuff")
		logger.info("cachedInviteLinks.size: ${InviteLinkModule.cachedInviteLinks.size}")
		logger.info("detectedInviteLinks.size: ${InviteLinkModule.detectedInviteLinks.size}")
		logger.info("> Misc Stuff")
		logger.info("fanArts.size: ${loritta.fanArts.size}")
		logger.info("eventLogListener.downloadedAvatarJobs: ${EventLogListener.downloadedAvatarJobs}")
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
			"reload" -> {
				val arg0 = args.getOrNull(0)

				if (arg0 == "commands") {
					LorittaLauncher.loritta.loadCommandManager()
					println("${com.mrpowergamerbr.loritta.utils.loritta.legacyCommandManager.commandMap.size} comandos carregados")
					return
				}
				if (arg0 == "mongo") {
					LorittaLauncher.loritta.initMongo()
					println("MongoDB recarregado!")
					return
				}
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

				for ((index, shard) in shards.withIndex()) {
					println("SHARD $index (${shard.status.name} - ${shard.gatewayPing}ms): ${shard.guilds.size} guilds - ${shard.users.size} members")
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
			"mongo" -> {
				println("===[ MONGODB ]===")
				println("isLocked: " + loritta.mongo.isLocked)

				val clusterField = Mongo::class.java.getDeclaredField("cluster")
				clusterField.isAccessible = true
				val cluster = clusterField.get(loritta.mongo)
				println(cluster)
				val serverField = cluster::class.java.getDeclaredField("server")
				serverField.isAccessible = true
				val defServer = serverField.get(cluster)
				println(defServer)
				val conPoolField = defServer::class.java.getDeclaredField("connectionPool")
				conPoolField.isAccessible = true
				val conPool = conPoolField.get(defServer)
				println(conPool)
				val waitQueueField = conPool::class.java.getDeclaredField("waitQueueSize")
				waitQueueField.isAccessible = true
				val waitQueueSize = waitQueueField.get(conPool) as AtomicInteger
				println("Wait Queue Size: " + waitQueueSize.get())
			}
			"bomdiaecia" -> {
				loritta.bomDiaECia.handleBomDiaECia(true)
			}
			"posts" -> {
				LorittaWebsite.INSTANCE.blog.posts = LorittaWebsite.INSTANCE.blog.loadAllBlogPosts()
			}
		}
	}
}