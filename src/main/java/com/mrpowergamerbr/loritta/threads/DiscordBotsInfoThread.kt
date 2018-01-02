package com.mrpowergamerbr.loritta.threads

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards

/**
 * Thread que atualiza os servidores que a Loritta no Discord Bots está a cada 10 segundos
 */
class DiscordBotsInfoThread : Thread("Discord Bot Info Thread") {
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
			val jsonObject = JsonObject()
			jsonObject["server_count"] = lorittaShards.getGuildCount()
			HttpRequest.post("https://bots.discord.pw/api/bots/" + Loritta.config.clientId + "/stats")
					.authorization(Loritta.config.discordBotsKey).acceptJson().contentType("application/json")
					.send(jsonObject.toString()).body()

			HttpRequest.post("https://discordbots.org/api/bots/${Loritta.config.clientId}/stats")
					.authorization(Loritta.config.discordBotsOrgKey).acceptJson().contentType("application/json")
					.send(jsonObject.toString()).body()
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}