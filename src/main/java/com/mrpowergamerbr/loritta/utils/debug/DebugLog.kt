package com.mrpowergamerbr.loritta.utils.debug

import com.mongodb.Mongo
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.threads.*
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.debug.DebugLog.logTypes
import com.mrpowergamerbr.loritta.utils.debug.DebugLog.subscribedDebugTypes
import net.pocketdreams.loriplugins.cleverbot.commands.CleverbotCommand
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

object DebugLog {
	val subscribedDebugTypes = mutableListOf<DebugType>(DebugType.WEBSITE, DebugType.COMMAND_EXECUTED, DebugType.TWITCH_THREAD, DebugType.COMMAND_STATUS)
	val logTypes = mutableListOf(DebugType.COMMAND_EXECUTED, DebugType.WEBSITE, DebugType.TWITCH_THREAD, DebugType.COMMAND_STATUS, DebugType.STACKTRACES)

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

	fun handleLine(line: String) {
		val args = line.split(" ").toMutableList()
		val command = args[0]
		args.removeAt(0)

		when (command) {
			"reload" -> {
				val arg0 = args.getOrNull(0)

				if (arg0 == "commands") {
					LorittaLauncher.loritta.loadCommandManager()
					println("${com.mrpowergamerbr.loritta.utils.loritta.commandManager.commandMap.size} comandos carregados")
					return
				}
				if (arg0 == "mongo") {
					LorittaLauncher.loritta.initMongo()
					println("MongoDB recarregado!")
					return
				}
			}
			"debug" -> {
				if (args.isNotEmpty()) {
					val todo = args[0]

					if (todo == "all") {
						subscribedDebugTypes.addAll(DebugType.values())
						return
					}
					if (todo == "none") {
						subscribedDebugTypes.clear()
						return
					}

					val type = args[1]

					if (todo == "add") {
						subscribedDebugTypes.add(DebugType.valueOf(type))

						println("$type added to the subscription list")
						return
					}
					if (todo == "remove") {
						subscribedDebugTypes.remove(DebugType.valueOf(type))

						println("$type removed from the subscription list")
						return
					}
				}
				println("Subscribed Debug Types: ${subscribedDebugTypes.joinToString(", ", transform = { it.name })}")
			}
			"log" -> {
				if (args.isNotEmpty()) {
					val todo = args[0]

					if (todo == "all") {
						logTypes.addAll(DebugType.values())
						return
					}
					if (todo == "none") {
						logTypes.clear()
						return
					}

					val type = args[1]

					if (todo == "add") {
						logTypes.add(DebugType.valueOf(type))

						println("$type added to the subscription list")
						return
					}
					if (todo == "remove") {
						logTypes.remove(DebugType.valueOf(type))

						println("$type removed from the subscription list")
						return
					}
				}
				println("Subscribed Debug Types: ${logTypes.joinToString(", ", transform = { it.name })}")
			}
			"info" -> {
				val mb = 1024 * 1024
				val runtime = Runtime.getRuntime()
				println("===[ INFO ]===")
				println("Shards: ${lorittaShards.shards.size}")
				println("Total Servers: ${lorittaShards.getGuildCount()}")
				println("Used Memory:"
						+ (runtime.totalMemory() - runtime.freeMemory()) / mb);
				println("Free Memory:"
						+ runtime.freeMemory() / mb);
				println("Total Memory:" + runtime.totalMemory() / mb);
				println("Max Memory:" + runtime.maxMemory() / mb);
			}
			"shards" -> {
				val shards = lorittaShards.shards

				for ((index, shard) in shards.withIndex()) {
					println("SHARD $index (${shard.status.name}): ${shard.guilds.size} guilds - ${shard.users.size} members")
				}
			}
			"extendedinfo" -> {
				println("===[ EXTENDED INFO ]===")
				println("commandManager.commandMap.size: ${loritta.commandManager.commandMap.size}")
				println("commandManager.defaultCmdOptions.size: ${loritta.commandManager.defaultCmdOptions.size}")
				println("dummyServerConfig.guildUserData.size: ${loritta.dummyServerConfig.guildUserData.size}")
				println("messageContextCache.size: ${loritta.messageContextCache.size}")
				println("messageInteractionCache.size: ${loritta.messageInteractionCache.size}")
				println("rawServersFanClub.size: ${loritta.rawServersFanClub.size}")
				println("serversFanClub.size: ${loritta.serversFanClub.size}")
				println("locales.size: ${loritta.locales.size}")
				println("ignoreIds.size: ${loritta.ignoreIds.size}")
				println("userCooldown.size: ${loritta.userCooldown.size}")
				println("southAmericaMemesPageCache.size: ${loritta.southAmericaMemesPageCache.size}")
				println("southAmericaMemesGroupCache.size: ${loritta.southAmericaMemesGroupCache.size}")
				println("musicManagers.size: ${loritta.musicManagers.size}")
				println("songThrottle.size: ${loritta.songThrottle.size}")
				println("youTubeKeys.size: ${loritta.youtubeKeys.size}")
				println("youTubeKeys.size: ${loritta.youtubeKeys.size}")
				println("fanArts.size: ${loritta.fanArts.size}")
				println("cleverbots.size: ${CleverbotCommand.cleverbots.size}")
				println("storedLastIds.size: ${AminoRepostThread.storedLastIds.size}")
				println("gameInfoCache.size: ${NewLivestreamThread.gameInfoCache.size}")
				println("isLivestreaming.size: ${NewLivestreamThread.isLivestreaming.size}")
				println("displayNameCache.size: ${NewLivestreamThread.displayNameCache.size}")
				println("lastItemTime.size: ${NewRssFeedThread.lastItemTime.size}")
				println("channelPlaylistIdCache.size: ${NewYouTubeVideosThread.channelPlaylistIdCache.size}")
				println("doNotReverify.size: ${NewYouTubeVideosThread.doNotReverify.size}")
				println("youTubeVideoCache.size: ${NewYouTubeVideosThread.youTubeVideoCache.size}")
			}
			"threads" -> {
				println("===[ ACTIVE THREADS ]===")
				println("eventLogExecutors: ${(loritta.eventLogExecutors as ThreadPoolExecutor).activeCount}")
				println("messageExecutors: ${(loritta.messageExecutors as ThreadPoolExecutor).activeCount}")
				println("executor: ${(loritta.executor as ThreadPoolExecutor).activeCount}")
				println("Total Thread Count: ${Thread.getAllStackTraces().keys.size}")
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
		}
	}
}

enum class DebugType {
	MESSAGE_RECEIVED, REACTION_RECEIVED, COMMAND_EXECUTED, COMMAND_STATUS, STACKTRACES, TWITCH_THREAD, RSSFEED_THREAD, WEBSITE
}

fun debug(type: DebugType, message: Any?) {
	if (subscribedDebugTypes.contains(type)) {
		System.out.println("${ConsoleColors.YELLOW_BOLD}[${type.name}]${ConsoleColors.RESET} $message")
	}
	if (logTypes.contains(type)) {
		log("[${type.name}] $message")
	}
}