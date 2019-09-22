package com.mrpowergamerbr.loritta.threads

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.commands.vanilla.economy.LoraffleCommand
import com.mrpowergamerbr.loritta.dao.Profile
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
import kotlin.collections.set

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

				if (diff > 3600000 && loritta.isMaster) { // Resultados apenas saem no master server
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
			if (FeatureFlags.BOTS_CAN_HAVE_FUN_IN_THE_RAFFLE_TOO) {
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

			var winner: Pair<String, String>? = null

			if (FeatureFlags.WRECK_THE_RAFFLE_STOP_THE_WHALES) {
				val chance = loritta.config.loritta.featureFlags.firstOrNull { it.startsWith("${FeatureFlags.Names.WRECK_THE_RAFFLE_STOP_THE_WHALES}-chance-") }
						?.split("-")
						?.last()
						?.toDouble() ?: 25.0

				val shouldWeWreckTheRaffle = chance(chance)

				logger.info { "Should we wreck the raffle to stop the whales? (Chance of $chance%) $shouldWeWreckTheRaffle" }

				if (shouldWeWreckTheRaffle) {
					logger.info { "Wreck the Raffle! Stop the Whales!!" }

					if (FeatureFlags.SELECT_LOW_BETTING_USERS && chance(50.0)) {
						winner = getLowBettingWinner()
					} else if (FeatureFlags.SELECT_USERS_WITH_LESS_MONEY) {
						winner = getUserWithLessMoneyWinner()
					} else winner = getRandomWinner()
				}
			}

			if (winner == null)
				winner = getRandomWinner()

			if (FeatureFlags.BOTS_CAN_HAVE_FUN_IN_THE_RAFFLE_TOO) {
				// Se não foi a Lori, Pantufa ou a Gabi que ganhram, vamos remover todos os tickets que elas apostaram
				// Assim evita que ganhadores ganhem muitos sonhos (já que os tickets delas também são considerados e
				// dados na hora que alguém ganha na rifa!)
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

	/**
	 * Selects a random ticket for the raffle winner.
	 */
	private fun getRandomWinner(): Pair<String, String> {
		logger.info { "Using normal random ticket selection for the raffle" }
		return userIds[RANDOM.nextInt(userIds.size)]
	}

	/**
	 * Selects a pseudo-random ticket for the raffle winner, selecting winners from a "how many tickets did he bet" range.
	 */
	private fun getLowBettingWinner(): Pair<String, String> {
		logger.info { "Using SELECT_USERS_WITH_LESS_MONEY ticket selection for the raffle" }
		val countOfEveryTicket = userIds.groupingBy { it.first }.eachCount()

		val lowerBound = RANDOM.nextInt(0, 250)
		val higherBound = lowerBound + RANDOM.nextInt(0, 250)
		val theLittleTimmies = countOfEveryTicket.filter { it.value in lowerBound..higherBound }
		logger.info { "Raffle selected tickets between $lowerBound..$higherBound, there are ${theLittleTimmies.size} tickets matching the filter" }

		return if (theLittleTimmies.isEmpty())
			getRandomWinner()
		else {
			val lilTimmy = theLittleTimmies.keys.toMutableList()[RANDOM.nextInt(theLittleTimmies.size)]
			Pair(lilTimmy, userIds.first { it.first == lilTimmy }.second)
		}
	}

	/**
	 * Selects a pseudo-random ticket for the raffle winner, giving a chance to users that has less money.
	 */
	private fun getUserWithLessMoneyWinner(): Pair<String, String> {
		logger.info { "Using SELECT_LOW_BETTING_USERS ticket selection for the raffle" }

		val allUserIds = userIds.distinctBy { it.first }
		if (10 > allUserIds.size)
			return getRandomWinner()

		val countOfEveryTicket = userIds.groupingBy { it.first }.eachCount()

		val userIdsAndMoney = mutableMapOf<String, Double>()
		allUserIds.forEach {
			val howMuchMoneyTheUserHas = transaction(Databases.loritta) {
				(Profile.findById(it.first.toLong())?.money) ?: Double.MAX_VALUE
			}

			val howMuchTicketsTheUserBought = countOfEveryTicket[it.first] ?: LoraffleCommand.MAX_TICKETS_BY_USER_PER_ROUND

			userIdsAndMoney[it.first] = howMuchMoneyTheUserHas + (howMuchTicketsTheUserBought * 250)
		}

		val userIdsAndMoneySorted = userIdsAndMoney.entries.sortedBy { it.value }
		val firstMorePoorPlayers = userIdsAndMoneySorted.take(allUserIds.size / 2)

		return userIds.first { it.first == firstMorePoorPlayers.random().key }
	}
}