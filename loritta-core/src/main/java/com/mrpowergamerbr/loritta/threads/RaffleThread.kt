package com.mrpowergamerbr.loritta.threads

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.commands.vanilla.economy.LoraffleCommand
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.chance
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.perfectdreams.loritta.utils.FeatureFlags
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
		const val MAX_TICKET_ABUSE_THRESHOLD_LORI = 4_000
		const val LORI_ID = 297153970613387264L
		const val MAX_TICKET_ABUSE_THRESHOLD_PANTUFA = 8_000
		const val PANTUFA_ID = 390927821997998081L
		const val MAX_TICKET_ABUSE_THRESHOLD_GABI = 12_000
		const val GABI_ID = 481901252007952385L
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
			if (FeatureFlags.isEnabled(FeatureFlags.BOTS_CAN_HAVE_FUN_IN_THE_RAFFLE_TOO)) {
				// Para evitar que a "casa" nunca ganhe nada, dependendo de quantos tickets são apostados na rifa a Lori, Pantufa e a Gabi irão apostar nela.
				// Se elas ganharem, quem apostou na rifa perde dinheiro!
				val ticketCount = userIds.size

				when {
					ticketCount >= MAX_TICKET_ABUSE_THRESHOLD_LORI -> {
						logger.info { "Sorry, we don't do that around here. Ticket threshold $ticketCount > $MAX_TICKET_ABUSE_THRESHOLD_LORI... Loritta will take care of this. ^-^" }

						for (i in 0 until LoraffleCommand.MAX_TICKETS_BY_USER_PER_ROUND) {
							userIds.add(Pair(LORI_ID.toString(), "default"))
						}
					}
					ticketCount >= MAX_TICKET_ABUSE_THRESHOLD_PANTUFA -> {
						logger.info { "Sorry, we don't do that around here. Ticket threshold $ticketCount > $MAX_TICKET_ABUSE_THRESHOLD_PANTUFA... Pantufa will take care of this. ^-^" }

						for (i in 0 until LoraffleCommand.MAX_TICKETS_BY_USER_PER_ROUND) {
							userIds.add(Pair(PANTUFA_ID.toString(), "default"))
						}
					}
					ticketCount >= MAX_TICKET_ABUSE_THRESHOLD_GABI -> {
						logger.info { "Sorry, we don't do that around here. Ticket threshold $ticketCount > $MAX_TICKET_ABUSE_THRESHOLD_GABI... Gabriela will take care of this. ^-^" }

						for (i in 0 until LoraffleCommand.MAX_TICKETS_BY_USER_PER_ROUND) {
							userIds.add(Pair(GABI_ID.toString(), "default"))
						}
					}
				}
			}

			val winner = if (FeatureFlags.isEnabled(FeatureFlags.WRECK_THE_RAFFLE_STOP_THE_WHALES) && chance(25.0)) {
				logger.info { "Wreck the Raffle! Stop the Whales!" }

				val countOfEveryTicket = userIds.groupingBy { it.first }.eachCount()

				val theLittleTimmies = countOfEveryTicket.filter { 250 >= it.value }
				if (theLittleTimmies.isEmpty())
					userIds[RANDOM.nextInt(userIds.size)]
				else {
					val lilTimmy = theLittleTimmies.keys.toMutableList()[RANDOM.nextInt(theLittleTimmies.size)]
					Pair(lilTimmy, userIds.first { it.first == lilTimmy }.second)
				}
			} else {
				userIds[RANDOM.nextInt(userIds.size)]
			}

			if (FeatureFlags.isEnabled(FeatureFlags.BOTS_CAN_HAVE_FUN_IN_THE_RAFFLE_TOO)) {
				// Se não foi a Lori, Pantufa ou a Gabi que ganhram, vamos remover todos os tickets que elas apostaram
				// Assim evita que ganhadores ganhem muitos sonhos (já que os tickets delas também são considerados e
				// dados na hora que alguém ganha na rifa!
				if (winner.first !in arrayOf(LORI_ID.toString(), PANTUFA_ID.toString(), GABI_ID.toString())) {
					userIds.removeIf {
						winner.first in arrayOf(LORI_ID.toString(), PANTUFA_ID.toString(), GABI_ID.toString())
					}
				}
			}

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

			if (user != null && !user.isBot) {
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