package net.perfectdreams.loritta.watchdog.utils

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.perfectdreams.loritta.watchdog.WatchdogBot
import net.perfectdreams.loritta.watchdog.utils.config.LorittaClusterConfig
import net.perfectdreams.loritta.watchdog.utils.config.WatchdogConfig
import java.util.*
import kotlin.concurrent.thread

object Commands {
	val logger = KotlinLogging.logger {}

	fun startListeningForCommands() {
		GlobalScope.launch {
			while (true) {
				try {
					val line = readLine()!!

					val split = line.split(" ")
					val cmd = split.getOrNull(0)

					if (cmd == "action") {
						val botName = split.getOrNull(1)!!
						val actionsString = split.getOrNull(2)!!

						val actions = actionsString.split("+").map { it.toLowerCase() }

						logger.info { "$botName -> $actions" }

						val bot = WatchdogBot.INSTANCE.config.checkBots.first { it.name == botName }

						if (actions.contains("deploy")) {
							logger.info { "Deploying ${bot.name} master cluster to slaves..." }
						}

						var howMuchShouldItBeDelayed = 0L

						for (slave in bot.clusters.filter { it.id != 1L }) {
							if (!actions.contains("silent")) {

							}

							GlobalScope.launch {
								if (actions.contains("rolling")) {
									val shardCount = (slave.maxShard - slave.minShard) + 1

									logger.info { "Waiting ${howMuchShouldItBeDelayed}ms until ${slave.id} ${slave.name} deploy..." }
									delay(howMuchShouldItBeDelayed)

									howMuchShouldItBeDelayed += (shardCount * bot.rollingDelayPerShard)
								}

								if (actions.contains("deploy")) {
									deployChangesToCluster(bot, slave)
								}
								if (actions.contains("restart")) {

								}
							}
						}
					}
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
		}
	}

	fun deployChangesToCluster(bot: WatchdogConfig.BotConfig, slave: LorittaClusterConfig) {
		logger.info { "Deploying to ${bot.name} cluster ${slave.id} ${slave.name}" }

		val randomPort = SplittableRandom()
				.nextInt(49152, 65535)
		logger.info { "Port forwarding ${slave.ipPortForward} -> 127.0.0.1:$randomPort..." }

		val sshPortForwardProcessBuilder = ProcessBuilder(
				"ssh",
				"-oStrictHostKeyChecking=no",
				"-L",
				"127.0.0.1:$randomPort:${slave.ipPortForward}",
				"root@${slave.targetIp}",
				"-i",
				slave.key
		).redirectErrorStream(true)

		val sshPortForwardProcess = sshPortForwardProcessBuilder.start()

		showProcessOutputOnConsole("SSH-PortForward", sshPortForwardProcess)

		logger.info { "Waiting 2.5s (connection!) before we start copying it..." }

		Thread.sleep(2_500)

		logger.info { "Preparing rsync..." }

		val rsyncProcessBuilder = ProcessBuilder(
				"rsync",
				"-avz",
				"-e",
				"sshpass -p \"${slave.password}\" ssh -oStrictHostKeyChecking=no -p $randomPort",
				"root@127.0.0.1:${bot.packFiles.joinToString(" ")}",
				slave.folder
		).redirectErrorStream(true)

		val rsync = rsyncProcessBuilder.start()
		showProcessOutputOnConsole("rsync", rsync)
		val statusCode = rsync.waitFor()

		if (statusCode != 0) {
			logger.error { "Something went wrong while deploying changes to ${bot.name} cluster ${slave.id} ${slave.name}! rsync error code #statusCode" }
		}

		logger.info { "Finished! Stopping all processes..." }
		rsync.destroyForcibly()
		sshPortForwardProcess.destroyForcibly()
	}

	fun showProcessOutputOnConsole(prefix: String, process: Process) {
		thread {
			val bufR = process.inputStream.bufferedReader()

			while (true) {
				val line = bufR.readLine() ?: break

				logger.info { "[$prefix] $line" }
			}
		}
	}
}