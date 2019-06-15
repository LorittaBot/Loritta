package com.mrpowergamerbr.loritta.threads

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import org.jetbrains.exposed.sql.transactions.transaction
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
		// TODO: Alterar userId para um long (para usar menos memória)
		var userIds = CopyOnWriteArrayList<Pair<String, String>>()
		val logger = KotlinLogging.logger {}
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
				logger.warn(e) { "Exception while trying to handleWin()! We have ${userIds.size} stored IDs, started = $started"}
			}
			Thread.sleep(1000)
		}
	}

	fun save() {
		val loteriaFile = File(Loritta.FOLDER, "raffle.json")

		val json = JsonObject()

		json["started"] = started
		json["lastWinnerId"] = lastWinnerId
		json["lastWinnerPrize"] = lastWinnerPrize
		json["userIds"] = Loritta.GSON.toJsonTree(userIds)

		logger.info { "Salvando raffle.json..." }
		logger.info { "Iniciou às: $started" }
		logger.info { "Último vencedor: $lastWinnerId" }
		logger.info { "Prémio do último vencedor: $lastWinnerPrize" }
		logger.info { "Tickets: ${userIds.size}" }

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

			val lorittaProfile = loritta.getOrCreateLorittaProfile(winnerId)
			logger.info("$lastWinnerId ganhou $lastWinnerPrize sonhos (antes ele possuia ${lorittaProfile.money} sonhos) na Rifa!")

			transaction(Databases.loritta){
				lorittaProfile.money += money
			}

			userIds.clear()

			val locale = loritta.getLegacyLocaleById(winner.second)
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
					user.openPrivateChannel().queue {
						it.sendMessage(message).addFile(File(Loritta.ASSETS, "loritta_money_discord.png"), "loritta_money.png").queue()
					}
				} catch (e: Exception) {}
			}
			started = System.currentTimeMillis()
			save()
		}
	}
}