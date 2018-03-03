package com.mrpowergamerbr.loritta.threads

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import org.slf4j.LoggerFactory
import java.util.concurrent.ThreadPoolExecutor

/**
 * Thread que atualiza os servidores que a Loritta no Discord Bots está a cada 10 segundos
 */
class DiscordBotsInfoThread : Thread("Discord Bot Info Thread") {
	companion object {
		val logger = LoggerFactory.getLogger(DiscordBotsInfoThread::class.java)
	}

	override fun run() {
		super.run()

		while (true) {
			try {
				updateStatus();
			} catch (e: Exception) {
				e.printStackTrace()
			}
			Thread.sleep(10000)
		}
	}

	fun updateStatus() {
		try {
			logger.info("Enviando informações da Loritta para o Discord Bots...")
			val jsonObject = JsonObject()
			jsonObject["server_count"] = lorittaShards.getGuildCount()
			HttpRequest.post("https://bots.discord.pw/api/bots/" + Loritta.config.clientId + "/stats")
					.authorization(Loritta.config.discordBotsKey).acceptJson().contentType("application/json")
					.send(jsonObject.toString()).body()
			logger.info("Enviando informações da Loritta para o Discord Bot List...")
			HttpRequest.post("https://discordbots.org/api/bots/${Loritta.config.clientId}/stats")
					.authorization(Loritta.config.discordBotsOrgKey).acceptJson().contentType("application/json")
					.send(jsonObject.toString()).body()

			logger.info("Informações sobre threads:")
			logger.info("eventLogExecutors: ${(loritta.eventLogExecutors as ThreadPoolExecutor).activeCount}")
			logger.info("messageExecutors: ${(loritta.messageExecutors as ThreadPoolExecutor).activeCount}")
			logger.info("executor: ${(loritta.executor as ThreadPoolExecutor).activeCount}")
			logger.info("Total Thread Count: ${Thread.getAllStackTraces().keys.size}")
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}