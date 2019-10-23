package net.perfectdreams.loritta.watchdog.listeners

import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.Gson
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.http.userAgent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.watchdog.WatchdogBot
import net.perfectdreams.loritta.watchdog.utils.Commands
import java.awt.Color
import java.time.Instant
import java.time.ZoneId

class MessageListener(val m: WatchdogBot) : ListenerAdapter() {
	val logger = KotlinLogging.logger {}

	override fun onMessageReceived(event: MessageReceivedEvent) {
		if (event.author.idLong != 123170274651668480L)
			return

		val jda = event.jda

		val contentRaw = event.message.contentRaw
		val split = contentRaw.split(" ")
		val mention = split.first().replace("!", "")

		if (mention != "<@${event.jda.selfUser.idLong}>")
			return

		val command = split.getOrNull(1)
		val args = split.toMutableList().drop(2)

		if (command != null) {
			when (command) {
				"action" -> {
					val botName = args.getOrNull(0)!!
					val actionsString = args.getOrNull(1)!!
					val clusterList = args.getOrNull(2)?.split(",")

					val actions = actionsString.split("+").map { it.toLowerCase() }

					logger.info { "$botName -> $actions" }

					val bot = WatchdogBot.INSTANCE.config.checkBots.first { it.name == botName }

					if (actions.contains("deploy")) {
						logger.info { "Deploying ${bot.name} master cluster to slaves..." }
					}

					val clustersToBeUsed = bot.clusters.filter {
						if (clusterList != null)
							it.id.toString() in clusterList
						else
							true
					}

					for (clusterInfo in clustersToBeUsed) {
						GlobalScope.launch {
							var shardsBeforeThisOne = 0L
							val clustersBeforeThisOne = bot.clusters.filter { clusterInfo.id > it.id }
							var howMuchShouldItBeDelayed = 60_000L

							for (cluster in clustersBeforeThisOne) {
								shardsBeforeThisOne += (clusterInfo.maxShard - clusterInfo.minShard)
							}

							howMuchShouldItBeDelayed += shardsBeforeThisOne * bot.rollingDelayPerShard
							var now = System.currentTimeMillis()
							val syncronizeWithZeroSeconds = now % 60_000
							now -= syncronizeWithZeroSeconds
							val whenItWillBeRestarted = (now + howMuchShouldItBeDelayed)

							logger.info { "There are $shardsBeforeThisOne shards in clusters before this one!" }

							if (actions.contains("rolling")) {
								if (!actions.contains("silent") && actions.contains("restart")) {
									try {
										withTimeout(25_000) {
											m.http.post<HttpResponse>("https://${clusterInfo.getUrl(bot)}/api/v1/loritta/update") {
												userAgent(WatchdogBot.USER_AGENT)
												header("Authorization", clusterInfo.apiKey)
												body = Gson().toJson(
														jsonObject(
																"willRestartAt" to whenItWillBeRestarted
														)
												)
											}
										}
									} catch (e: Exception) {
										e.printStackTrace()
									}

									val channel = jda.getTextChannelById(bot.channelId)

									val instant = Instant.ofEpochMilli(whenItWillBeRestarted).atZone(ZoneId.systemDefault())

									val time = "${instant.hour.toString().padStart(2, '0')}:${instant.minute.toString().padStart(2, '0')}:${instant.second.toString().padStart(2, '0')}"
									channel?.sendMessage(
											EmbedBuilder()
													.setTitle("<:loriShrug:611371352452104202> Inatividade Agendada em Cluster ${clusterInfo.id} (${clusterInfo.name})")
													.setDescription("Cluster irá reiniciar às $time!\n\nShards ${clusterInfo.minShard} até ${clusterInfo.maxShard} ficarão offline durante a manutenção.")
													.setColor(Color(206, 7, 232))
													.setTimestamp(Instant.now())
													.build()
									)?.queue()
								}

								logger.info { "Waiting ${howMuchShouldItBeDelayed}ms until ${clusterInfo.id} ${clusterInfo.name} deploy..." }
								delay(whenItWillBeRestarted - System.currentTimeMillis())
							}

							if (actions.contains("deploy") && clusterInfo.id != 1L) {
								Commands.deployChangesToCluster(bot, clusterInfo)
							}

							if (actions.contains("restart")) {
								try {
									withTimeout(25_000) {
										m.http.get<HttpResponse>("https://${clusterInfo.getUrl(bot)}/api/v1/loritta/update") {
											header("Authorization", clusterInfo.apiKey)
											userAgent(WatchdogBot.USER_AGENT)
										}
									}
								} catch (e: Exception) {}

								val botCluster = m.botStuff[bot.botId]!!.clusters[clusterInfo.id]!!
								botCluster.offlineForUpdates = true

								val channel = jda.getTextChannelById(bot.channelId)

								channel?.sendMessage(
										EmbedBuilder()
												.setTitle("<:loriShrug:611371352452104202> Reiniciando Cluster ${clusterInfo.id} (${clusterInfo.name}) devido a atualização")
												.setDescription("Shards ${clusterInfo.minShard} até ${clusterInfo.maxShard} estão offline!")
												.setColor(Color(0, 65, 168))
												.setTimestamp(Instant.now())
												.build()
								)?.queue()
							}
						}
					}

					event.message.addReaction("lori_cheese:592779169059045379").queue()
				}
			}
		}
	}
}