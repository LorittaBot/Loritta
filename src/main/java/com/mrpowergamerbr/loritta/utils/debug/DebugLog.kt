package com.mrpowergamerbr.loritta.utils.debug

import com.mrpowergamerbr.loritta.utils.LORITTA_SHARDS
import com.mrpowergamerbr.loritta.utils.debug.DebugLog.subscribedDebugTypes
import com.mrpowergamerbr.loritta.utils.loritta
import net.pocketdreams.loriplugins.cleverbot.commands.CleverbotCommand
import kotlin.concurrent.thread

object DebugLog {
	val subscribedDebugTypes = mutableListOf<DebugType>()

	fun startCommandListenerThread() {
		thread {
			commandLoop@ while (true) {
				try {
					val line = readLine()!!
					val args = line.split(" ").toMutableList()
					val command = args[0]
					args.removeAt(0)

					when (command) {
						"debug" -> {
							if (args.isNotEmpty()) {
								val todo = args[0]

								if (todo == "all") {
									subscribedDebugTypes.addAll(DebugType.values())
									continue@commandLoop
								}
								if (todo == "none") {
									subscribedDebugTypes.clear()
									continue@commandLoop
								}

								val type = args[1]

								if (todo == "add") {
									subscribedDebugTypes.add(DebugType.valueOf(type))

									println("$type added to the subscription list")
									continue@commandLoop
								}
								if (todo == "remove") {
									subscribedDebugTypes.remove(DebugType.valueOf(type))

									println("$type removed from the subscription list")
									continue@commandLoop
								}
							}
							println("Subscribed Debug Types: ${subscribedDebugTypes.joinToString(", ", transform = { it.name })}")
						}
						"info" -> {
							println("===[ INFO ]===")
							println("Shards: ${LORITTA_SHARDS.shards.size}")
							println("Total Servers: ${LORITTA_SHARDS.getGuilds().size}")
							println("Users: ${LORITTA_SHARDS.getUsers().size}")
						}
						"extendedinfo" -> {
							println("===[ EXTENDED INFO ]===")
							println("commandManager.commandMap.size: ${loritta.commandManager.commandMap.size}")
							println("commandManager.defaultCmdOptions.size: ${loritta.commandManager.defaultCmdOptions.size}")
							println("dummyServerConfig.userData.size: ${loritta.dummyServerConfig.userData.size}")
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
							println("famousGuilds.size: ${loritta.famousGuilds.size}")
							println("randomFamousGuilds.size: ${loritta.randomFamousGuilds.size}")
							println("isPatreon.size: ${loritta.isPatreon.size}")
							println("isDonator.size: ${loritta.isDonator.size}")
							println("youTubeKeys.size: ${loritta.youtubeKeys.size}")
							println("fanArts.size: ${loritta.fanArts.size}")
							println("cleverbots.size: ${CleverbotCommand.cleverbots.size}")
						}
					}
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
		}
	}
}

enum class DebugType {
	MESSAGE_RECEIVED, REACTION_RECEIVED, COMMAND_EXECUTED, TWITCH_THREAD
}

fun debug(type: DebugType, message: Any?) {
	if (subscribedDebugTypes.contains(type)) {
		System.out.println("[${type.name}] $message")
	}
}