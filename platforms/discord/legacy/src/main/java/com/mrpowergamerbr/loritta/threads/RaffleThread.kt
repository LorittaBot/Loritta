package com.mrpowergamerbr.loritta.threads

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import net.perfectdreams.loritta.utils.UserPremiumPlans
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
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
		val buyingOrGivingRewardsMutex = Mutex()
	}

	override fun run() {
		super.run()

		while (true) {
			try {
				val diff = System.currentTimeMillis() - started

				if (diff > 3600000) { // Resultados apenas saem no master server
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
		runBlocking {
			buyingOrGivingRewardsMutex.withLock {
				if (userIds.isEmpty()) {
					started = System.currentTimeMillis()
					save()
				} else {
					var winner: Pair<String, String>? = null

					if (winner == null)
						winner = getRandomWinner()

					val winnerId = winner.first
					lastWinnerId = winnerId

					val currentActiveDonations = loritta.getActiveMoneyFromDonations(winnerId.toLong())
					val plan = UserPremiumPlans.getPlanFromValue(currentActiveDonations)

					val moneyWithoutTaxes = userIds.size * 250
					val money = (moneyWithoutTaxes * plan.totalLoraffleReward).toInt()
					lastWinnerPrize = money

					val lorittaProfile = loritta.getOrCreateLorittaProfile(winnerId)
					logger.info("$lastWinnerId ganhou $lastWinnerPrize sonhos ($moneyWithoutTaxes without taxes; antes ele possuia ${lorittaProfile.money} sonhos) na Rifa!")

					transaction(Databases.loritta) {
						lorittaProfile.addSonhosAndAddToTransactionLogNested(
							money.toLong(),
							SonhosPaymentReason.RAFFLE
						)
					}

					val totalTicketsBoughtByTheUser = userIds.count { it.first == winnerId }
					val totalTickets = userIds.size
					val totalUsersInTheRaffle = userIds.map { it.first }.distinct().size

					userIds.clear()

					val locale = loritta.localeManager.getLocaleById(winner.second)
					val user = runBlocking { lorittaShards.retrieveUserById(lastWinnerId!!) }

					if (user != null && !user.isBot) {
						try {
							val embed = EmbedBuilder()
							embed.setThumbnail("attachment://loritta_money.png")
							embed.setColor(Color(47, 182, 92))
							embed.setTitle("\uD83C\uDF89 ${locale["commands.command.raffle.victory.title"]}!")
							embed.setDescription(
								locale.getList(
									"commands.command.raffle.victory.description",
									totalTicketsBoughtByTheUser,
									lastWinnerPrize,
									totalUsersInTheRaffle,
									totalTickets,
									totalTicketsBoughtByTheUser / totalTickets.toDouble(),
									Emotes.LORI_RICH,
									Emotes.LORI_NICE
								).joinToString("\n")
							)

							embed.setTimestamp(Instant.now())
							val message = MessageBuilder().setContent(" ").setEmbed(embed.build()).build()
							user.openPrivateChannel().queue {
								it.sendMessage(message)
									.addFile(File(Loritta.ASSETS, "loritta_money_discord.png"), "loritta_money.png")
									.queue()
							}
						} catch (e: Exception) {
						}
					}
					started = System.currentTimeMillis()
					save()
				}
			}
		}
	}

	/**
	 * Selects a random ticket for the raffle winner.
	 */
	private fun getRandomWinner(): Pair<String, String> {
		logger.info { "Using normal random ticket selection for the raffle" }
		return userIds[RANDOM.nextInt(userIds.size)]
	}
}
