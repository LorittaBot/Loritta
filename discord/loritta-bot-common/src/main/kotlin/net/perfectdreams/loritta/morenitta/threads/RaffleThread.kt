package net.perfectdreams.loritta.morenitta.threads

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.LorittaBot.Companion.RANDOM
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.utils.SonhosPaymentReason
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import java.awt.Color
import java.io.File
import java.time.Instant
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Thread que atualiza o status da Loritta a cada 1s segundos
 */
class RaffleThread(val loritta: LorittaBot) : Thread("Raffle Thread") {
	companion object {
		var lastWinnerId: Long? = null
		var lastWinnerPrize = 0
		var started: Long = System.currentTimeMillis()
		var isReady = false
		var userIds = CopyOnWriteArrayList<Long>()
		val logger = KotlinLogging.logger {}
		val buyingOrGivingRewardsMutex = Mutex()
		var raffleRandomUniqueId = UUID.randomUUID()
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
		val loteriaFile = File(LorittaBot.FOLDER, "raffle.json")

		val json = JsonObject()

		json["started"] = started
		json["lastWinnerId"] = lastWinnerId
		json["lastWinnerPrize"] = lastWinnerPrize
		json["userIds"] = LorittaBot.GSON.toJsonTree(userIds)

		logger.info { "Salvando raffle.json..." }
		logger.info { "Iniciou às: $started" }
		logger.info { "Último vencedor: $lastWinnerId" }
		logger.info { "Prémio do último vencedor: $lastWinnerPrize" }
		logger.info { "Tickets: ${userIds.size}" }

		loteriaFile.writeText(json.toString())
	}

	fun handleWin() {
		runBlocking {
			// Everything is done, change the Unique ID to force all pending requests to be stale
			raffleRandomUniqueId = UUID.randomUUID()
			
			buyingOrGivingRewardsMutex.withLock {
				if (userIds.isEmpty()) {
					started = System.currentTimeMillis()
					save()
				} else {
					var winner: Long? = null

					if (winner == null)
						winner = getRandomWinner()

					val winnerId = winner
					lastWinnerId = winnerId

					val currentActiveDonations = loritta.getActiveMoneyFromDonations(winnerId.toLong())
					val plan = UserPremiumPlans.getPlanFromValue(currentActiveDonations)

					val moneyWithoutTaxes = userIds.size * 250
					val money = (moneyWithoutTaxes * plan.totalLoraffleReward).toInt()
					lastWinnerPrize = money

					val lorittaProfile = loritta.getOrCreateLorittaProfile(winnerId)
					logger.info("$lastWinnerId ganhou $lastWinnerPrize sonhos ($moneyWithoutTaxes without taxes; antes ele possuia ${lorittaProfile.money} sonhos) na Rifa!")

					loritta.pudding.transaction {
						lorittaProfile.addSonhosAndAddToTransactionLogNested(
							money.toLong(),
							SonhosPaymentReason.RAFFLE
						)
					}

					val totalTicketsBoughtByTheUser = userIds.count { it == winnerId }
					val totalTickets = userIds.size
					val totalUsersInTheRaffle = userIds.map { it }.distinct().size

					userIds.clear()

					// TODO: Locales, maybe get the preferred user locale ID?
					val locale = loritta.localeManager.getLocaleById("default")
					val user = runBlocking { loritta.lorittaShards.retrieveUserById(lastWinnerId!!) }

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
									.addFile(File(LorittaBot.ASSETS, "loritta_money_discord.png"), "loritta_money.png")
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
	private fun getRandomWinner(): Long {
		logger.info { "Using normal random ticket selection for the raffle" }
		return userIds[RANDOM.nextInt(userIds.size)]
	}
}
