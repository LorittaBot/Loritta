package net.perfectdreams.loritta.watchdog

import io.ktor.client.HttpClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.perfectdreams.loritta.watchdog.listeners.MessageListener
import net.perfectdreams.loritta.watchdog.utils.config.WatchdogConfig
import java.util.*

class WatchdogBot(val config: WatchdogConfig) {
	companion object {
		val logger = KotlinLogging.logger {}
	}

	val lastReply = mutableMapOf<Long, Long>()
	val slowCount = mutableMapOf<Long, Long>()
	val http = HttpClient()

	fun start() {
		val jda = JDABuilder()
				.setToken(config.discordToken)
				.addEventListeners(MessageListener(this))
				.setStatus(OnlineStatus.IDLE)
				.setDisabledCacheFlags(EnumSet.of(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.EMOTE, CacheFlag.VOICE_STATE))
				.build()
				.awaitReady()

		jda.presence.activity = Activity.of(
				Activity.ActivityType.WATCHING,
				"se eu cai ;w;"
		)

		val staffChannel by lazy {
			jda.getTextChannelById("358774895850815488")!!
		}

		config.checkBots.forEach {
			logger.info { "Iniciando task de ${it}..." }

			GlobalScope.launch {
				delay(it.startAfter)

				while (true) {
					val textChannel = jda.getTextChannelById(it.channelId)

					if (textChannel != null) {
						logger.info { "Enviando mensagem ${it.useCommand} em ${it.channelId}"}

						val now = System.currentTimeMillis()
						lastReply[it.botId] = now

						val message = textChannel.sendMessage(it.useCommand).complete()

						GlobalScope.launch {
							delay(it.timeout)

							val whenMessageWasSent = lastReply[it.botId] ?: 0

							logger.info { "Demorou ${System.currentTimeMillis() - whenMessageWasSent}ms para responder! Ela não respondeu nada? ${now == whenMessageWasSent} Atualmente slow count é ${slowCount[it.botId]}" }

							if (now == whenMessageWasSent) {
								slowCount[it.botId] = (slowCount[it.botId] ?: 0) + 1

								if (slowCount[it.botId] ?: 0 >= it.warnAfter) {
									staffChannel.sendMessage("Okay <@&399301696892829706> parece que a <@${it.botId}> está offline! E agora??? <:lori_tristeliz:556524143281963008><a:lori_caiu:540625554282512384>").queue()
								} else {
									staffChannel.sendMessage("Parece que <@${it.botId}> está offline! <a:lori_caiu:540625554282512384>").queue()
								}
							} else {
								slowCount[it.botId] = Math.max(0, (slowCount[it.botId] ?: 0) - 1)
								// textChannel.sendMessage("<@${it.botId}> <:lori_wow:540944393692119040>").queue()
							}

							message.delete().queue()
						}
					}

					delay(it.delay)
				}
			}
		}
	}
}