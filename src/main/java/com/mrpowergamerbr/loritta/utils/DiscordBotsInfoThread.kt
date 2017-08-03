package com.mrpowergamerbr.loritta.utils

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.Loritta

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
			Thread.sleep(10000);
		}
	}

	fun updateStatus() {
		try {
			val discordBotsPw = HttpRequest.post("https://bots.discord.pw/api/bots/" + Loritta.config.clientId + "/stats")
					.authorization(Loritta.config.discordBotsKey).acceptJson().contentType("application/json")
					.send("{ \"server_count\": " + loritta.lorittaShards.getGuilds().size + " }").body()

			val discordBotsOrg = HttpRequest.post("https://discordbots.org/api/bots/${Loritta.config.clientId}/stats")
					.authorization(Loritta.config.discordBotsOrgKey).acceptJson().contentType("application/json")
					.send("{ \"server_count\": " + loritta.lorittaShards.getGuilds().size + " }").body()
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}