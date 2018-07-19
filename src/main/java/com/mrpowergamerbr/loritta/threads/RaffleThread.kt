package com.mrpowergamerbr.loritta.threads

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.save
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Thread que atualiza o status da Loritta a cada 1s segundos
 */
class RaffleThread : Thread("Raffle Thread") {
	companion object {
		var lastWinnerId: String? = null
		var lastWinnerPrize = 0
		var started: Long = System.currentTimeMillis()
		// user ID + locale ID
		var userIds = CopyOnWriteArrayList<Pair<String, String>>()
		val logger = LoggerFactory.getLogger(RaffleThread::class.java)
	}

	override fun run() {
		super.run()

		while (true) {
			try {
				val diff = System.currentTimeMillis() - started

				if (diff > 3600000) {
					handleWin()
				}
			} catch (e: Exception) {
				e.printStackTrace()
			}
			Thread.sleep(1000);
		}
	}

	fun save() {
		val loteriaFile = File(Loritta.FOLDER, "raffle.json")

		val json = JsonObject()

		json["started"] = started
		json["lastWinnerId"] = lastWinnerId
		json["lastWinnerPrize"] = lastWinnerPrize
		json["userIds"] = Loritta.GSON.toJsonTree(userIds)

		logger.info("""Salvando raffle.json...
			|Iniciou às: $started
			|Último vencedor: $lastWinnerId
			|Prémio do último vencedor: $lastWinnerPrize
			|Tickets: ${userIds.size}
		""".trimMargin())

		loteriaFile.writeText(json.toString())
	}

	fun handleWin() {
		if (userIds.isEmpty()) {
			started = System.currentTimeMillis()
			save()
		} else {
			val winner = userIds[RANDOM.nextInt(userIds.size)]
			val winnerId = winner.first
			lastWinnerId = winnerId

			val money = userIds.size * 250
			lastWinnerPrize = money

			val lorittaProfile = loritta.getLorittaProfileForUser(winnerId)
			logger.info("$lastWinnerId ganhou $lastWinnerPrize sonhos (antes ele possuia ${lorittaProfile.dreams} sonhos) na Rifa!")

			lorittaProfile.dreams += money
			loritta save lorittaProfile
			userIds.clear()

			val locale = loritta.getLocaleById(winner.second)
			val user = lorittaShards.getUserById(lastWinnerId)

			if (user != null) {
				try {
					val embed = EmbedBuilder()
					embed.setThumbnail("attachment://loritta_money.png")
					embed.setColor(Constants.LORITTA_AQUA)
					embed.setTitle("\uD83C\uDF89 ${locale["RAFFLE_Congratulations"]}!")
					embed.setDescription("${locale["RAFFLE_YouEarned", lastWinnerPrize]} \uD83E\uDD11")
					embed.setTimestamp(Instant.now())
					val message = MessageBuilder().setContent(" ").setEmbed(embed.build()).build()
					user.openPrivateChannel().complete().sendFile(File(Loritta.ASSETS, "loritta_money_discord.png"), "loritta_money.png", message).complete()
				} catch (e: Exception) {}
			}
			started = System.currentTimeMillis()
			save()
		}
	}
}