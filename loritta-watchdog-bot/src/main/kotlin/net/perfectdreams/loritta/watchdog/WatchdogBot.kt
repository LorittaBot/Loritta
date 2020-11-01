package net.perfectdreams.loritta.watchdog

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.watchdog.listeners.MessageListener
import net.perfectdreams.loritta.watchdog.utils.Bot
import net.perfectdreams.loritta.watchdog.utils.Commands
import net.perfectdreams.loritta.watchdog.utils.config.WatchdogConfig
import org.jsoup.Jsoup
import java.awt.Color
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLPeerUnverifiedException

class WatchdogBot(val config: WatchdogConfig) {
	companion object {
		val logger = KotlinLogging.logger {}
		lateinit var INSTANCE: WatchdogBot
		const val USER_AGENT = "Loritta Watchdog"
		var jsonParser = JsonParser()
	}

	val http = HttpClient(CIO) {
		engine {
			requestTimeout = 15_000
		}

		this.expectSuccess = false
	}

	val botStuff = mutableMapOf<Long, Bot>()
	val pingTracker = DiscordPingStatusTracker(this)

	fun start() {
		INSTANCE = this

		GlobalScope.launch {
			while (true) {
				try {
					pingTracker.checkStatus()
				} catch (e: Exception) {
					logger.warn(e) { "Error while checking Discord's Status!" }
				}

				delay(25_000)
			}
		}

		val jda = JDABuilder.createLight(
				config.discordToken,
				GatewayIntent.GUILD_MESSAGES
		)
				.setStatus(OnlineStatus.IDLE)
				.addEventListeners(MessageListener(this))
				.build()
				.awaitReady()

		GlobalScope.launch {
			while (true) {
				val status = if (pingTracker.lastLatencyResult >= 200) {
					OnlineStatus.DO_NOT_DISTURB
				} else if (pingTracker.lastLatencyResult >= 100) {
					OnlineStatus.IDLE
				} else {
					OnlineStatus.ONLINE
				}

				val lastPublished = pingTracker.lastPublishedStatus

				val stringStatus = buildString {
					this.append("\uD83D\uDCE1 DPing: ${pingTracker.lastLatencyResult}ms")

					if (lastPublished != null && lastPublished.publishedDate.time >= System.currentTimeMillis() - 3_600_000L) {
						val description = lastPublished.description.value

						val jsoup = Jsoup.parse(description)

						val firstParagraph = jsoup.getElementsByTag("p").first()

						val type = firstParagraph.getElementsByTag("strong").text()

						this.append(" • \uD83D\uDCF0 ${lastPublished.title} - ${type}")
					}

					this.append(" • \uD83E\uDD70 se eu cai ;w;")
				}


				jda.presence.setPresence(
						status,
						Activity.of(
								Activity.ActivityType.WATCHING,
								stringStatus
						)
				)

				delay(25_000)
			}
		}

		Commands.startListeningForCommands()

		config.checkBots.forEach {
			val config = it
			GlobalScope.launch {
				while (true) {
					val bot = botStuff.getOrPut(it.botId) { Bot() }
					val channel = jda.getTextChannelById(it.channelId)

					val deferreds = it.clusters.map {
						val url = it.getUrl(config)

						Pair(
								it,
								GlobalScope.async {
									val time = System.currentTimeMillis()

									logger.info { "Verifying ${config.name} ${it.name} status... Time is $time" }

									withTimeout(15_000) {
										val result = http.get<HttpResponse>("https://$url/api/v1/loritta/status?t=${time}") {
											userAgent(USER_AGENT)
										}

										val body = result.readText()
										logger.info { "${config.name} ${it.name} response is ${result.status}" }

										if (result.status != HttpStatusCode.OK)
											throw HttpStatusError(result.status)

										jsonParser.parse(
												body
										)
									}
								}
						)
					}

					for ((clusterInfo, deferred) in deferreds) {
						val botCluster = bot.clusters.getOrPut(clusterInfo.id) { Bot.Cluster() }
						try {
							val result = deferred.await()

							val uptime = result["uptime"].long
							val isIgnoringRequests = result["isIgnoringRequests"].bool

							val shards = result["shards"].array
							val areAllConnected = !shards.any { it["status"].string != "CONNECTED" }

							if (botCluster.isReady) {
								if (botCluster.isIgnoringRequests != isIgnoringRequests) {
									if (isIgnoringRequests) {
										channel?.sendMessage(
												EmbedBuilder()
														.setTitle("\uD83D\uDD25 Cluster ${clusterInfo.id} (${clusterInfo.name}) está ignorando requests!")
														.setDescription("Vamos esperar normalizar... já que se continuasse a enviar requests, talvez iria levar global ban... ${Emotes.LORI_CRYING}")
														.setColor(Color.RED)
														.setTimestamp(Instant.now())
														.build()
										)?.queue()
									} else {
										channel?.sendMessage(
												EmbedBuilder()
														.setTitle("${Emotes.LORI_TEMMIE} Cluster ${clusterInfo.id} (${clusterInfo.name}) voltou a processar requests!")
														.setDescription("Yay, normalizado!")
														.setColor(Color.GREEN)
														.setTimestamp(Instant.now())
														.build()
										)?.queue()
									}
								}

								if (botCluster.startedAt >= uptime) {
									botCluster.areAllConnected = false

									val oldUptime = humanizeSpan(botCluster.startedAt)
									val newUptime = humanizeSpan(uptime)

									if (botCluster.offlineForUpdates) {
										channel?.sendMessage(
												EmbedBuilder()
														.setTitle("<:lori_cobertor:548638182665617419> Cluster ${clusterInfo.id} (${clusterInfo.name}) reiniciou com sucesso após update!")
														.setDescription("Vamos esperar voltar após a atualização :3\n\n**Uptime antigo:** `$oldUptime`\n**Uptime novo:** `$newUptime`\n**Discord Shards afetadas:** ${clusterInfo.minShard} até ${clusterInfo.maxShard}")
														.setColor(Color(252, 3, 148))
														.setTimestamp(Instant.now())
														.build()
										)?.queue()
									} else if (botCluster.dead) {
										val oldUptime = humanizeSpan(botCluster.startedAt)
										val newUptime = humanizeSpan(uptime)

										channel?.sendMessage(
												EmbedBuilder()
														.setTitle("<:lori_tristeliz:556524143281963008> Cluster ${clusterInfo.id} (${clusterInfo.name}) reiniciou inesperadamente!")
														.setDescription("**Uptime antigo:** `$oldUptime`\n**Uptime novo:** `$newUptime`\n**Discord Shards afetadas:** ${clusterInfo.minShard} até ${clusterInfo.maxShard}")
														.setColor(Color.RED)
														.setTimestamp(Instant.now())
														.build()
										)?.queue()
									} else {


										channel?.sendMessage(
												EmbedBuilder()
														.setTitle("<:lori_tristeliz:556524143281963008> Cluster ${clusterInfo.id} (${clusterInfo.name}) reiniciou inesperadamente!\n**Discord Shards afetadas:** ${clusterInfo.minShard} até ${clusterInfo.maxShard}")
														.setDescription("**Uptime antigo:** `$oldUptime`\n**Uptime novo:** `$newUptime`")
														.setColor(Color.RED)
														.setTimestamp(Instant.now())
														.build()
										)?.queue()
									}
								}

								val notConnectedShards = shards.filter { it["status"].string != "CONNECTED" }
								if (areAllConnected && !botCluster.areAllConnected) {
									channel?.sendMessage(
											EmbedBuilder()
													.setTitle("<:lori_happy:585550787426648084> Cluster ${clusterInfo.id} (${clusterInfo.name}) está de volta, o show não pode parar!")
													.setDescription("**Hora do show!** Shards ${clusterInfo.minShard} até ${clusterInfo.maxShard} estão de volta, prontas para espalhar alegria e diversão!")
													.setColor(Color(161, 235, 52))
													.setTimestamp(Instant.now())
													.build()
									)?.queue()
								}
								if (!areAllConnected && botCluster.areAllConnected) {
									channel?.sendMessage(
											EmbedBuilder()
													.setTitle("<:lori_decepcionada:556524403446513675> Cluster ${clusterInfo.id} (${clusterInfo.name}) está instável!")
													.setDescription("**Algumas shards perderam conexão com o Discord! As shards que perderam conexão foram as...**\n${notConnectedShards.joinToString("\n", transform = { "Shard ${it["id"].string}: `${it["status"].string}`"})}")
													.setColor(Color(232, 224, 0))
													.setTimestamp(Instant.now())
													.build()
									)?.queue()
								}
							}

							botCluster.isIgnoringRequests = isIgnoringRequests
							botCluster.areAllConnected = areAllConnected
							botCluster.offlineForUpdates = false
							botCluster.startedAt = uptime
							botCluster.dead = false

							logger.info { "Cluster ${config.name} ${clusterInfo.id} (${clusterInfo.name}) is alive and well ;w;" }
						} catch (e: Exception) {
							logger.error(e) { "Error while checking cluster!" }

							if (!botCluster.dead && botCluster.isReady && !botCluster.offlineForUpdates) {
								// Por algum motivo vive dando SSLPeerUnverifiedException, mesmo que a Lori não esteja offline
								// Vamos apenas ignorar tais mensagens
								if (e !is SSLPeerUnverifiedException) {
									val embed = EmbedBuilder()
											.setTitle("<a:lori_caiu:540625554282512384> Cluster ${clusterInfo.id} (${clusterInfo.name}) está offline!")
											.setColor(Color.BLACK)
											.setTimestamp(Instant.now())

									if (e is HttpStatusError) {
										embed.setDescription(
												"**Discord Shards afetadas:** ${clusterInfo.minShard} até ${clusterInfo.maxShard}\n**Código de Erro HTTP:** `${e.status.value}` (${e.status.description})"
										)
									} else {
										embed.setDescription("**Discord Shards afetadas:** ${clusterInfo.minShard} até ${clusterInfo.maxShard}\n**Erro:** `${e.message}`\n${e::class.simpleName}")
									}

									channel?.sendMessage(
											embed.build()
									)?.queue()

									botCluster.dead = true
									botCluster.areAllConnected = false
								}
							}
						}
					}

					delay(15_000)
				}
			}
		}
	}

	fun humanizeSpan(uptime: Long): String {
		var jvmUpTime = uptime
		val days = TimeUnit.MILLISECONDS.toDays(jvmUpTime)
		jvmUpTime -= TimeUnit.DAYS.toMillis(days)
		val hours = TimeUnit.MILLISECONDS.toHours(jvmUpTime)
		jvmUpTime -= TimeUnit.HOURS.toMillis(hours)
		val minutes = TimeUnit.MILLISECONDS.toMinutes(jvmUpTime)
		jvmUpTime -= TimeUnit.MINUTES.toMillis(minutes)
		val seconds = TimeUnit.MILLISECONDS.toSeconds(jvmUpTime)

		val sb = StringBuilder(64)
		sb.append(days)
		sb.append("d ")
		sb.append(hours)
		sb.append("h ")
		sb.append(minutes)
		sb.append("m ")
		sb.append(seconds)
		sb.append("s")

		return sb.toString()
	}

	data class HttpStatusError(val status: HttpStatusCode) : RuntimeException()
}