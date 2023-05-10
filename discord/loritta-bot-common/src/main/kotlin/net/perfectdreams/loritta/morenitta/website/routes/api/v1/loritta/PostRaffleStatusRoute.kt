package net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import net.perfectdreams.loritta.morenitta.commands.vanilla.economy.LoraffleCommand
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.RaffleTickets
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.Raffles
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.Easter2023SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.RaffleTicketsSonhosTransactionsLog
import net.perfectdreams.loritta.common.utils.RaffleType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.SonhosPaymentReason
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import org.jetbrains.exposed.sql.*
import java.time.Instant

class PostRaffleStatusRoute(loritta: LorittaBot) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/loritta/raffle") {
	companion object {
		val logger = KotlinLogging.logger {}
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val json = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()).obj }

		val userId = json["userId"].long
		val quantity = json["quantity"].int
		val localeId = json["localeId"].string
		val invokedAt = Instant.ofEpochMilli(json["invokedAt"].long)
		val type = RaffleType.valueOf(json["type"].string)

		val response = loritta.transaction {
			// The "invokedAt" is used to only get raffles triggered WHEN the user used the command
			// This way it avoids issues when Loritta took too long to receive this request, which would cause Loritta to get the new raffle instead of the "current-now-old" raffle.
			val currentRaffle = Raffles.select {
				Raffles.raffleType eq type and (Raffles.endedAt.isNull()) and (Raffles.endsAt greaterEq invokedAt)
			}.firstOrNull()

			// The raffle hasn't been created yet! The LorittaRaffleTask on the main instance should *hopefully* create the new raffle soon...
			if (currentRaffle == null) {
				return@transaction jsonObject(
					"status" to LoraffleCommand.BuyRaffleTicketStatus.STALE_RAFFLE_DATA.toString()
				)
			}

			// Get how many tickets the user has in the current raffle
			val currentUserTicketQuantity = RaffleTickets.select {
				RaffleTickets.raffle eq currentRaffle[Raffles.id] and (RaffleTickets.userId eq userId)
			}.count()

			val maxTicketsByUserPerRoundForThisRaffleType = currentRaffle[Raffles.raffleType].maxTicketsByUserPerRound.toLong()

			if (currentUserTicketQuantity + quantity > maxTicketsByUserPerRoundForThisRaffleType) {
				return@transaction if (currentUserTicketQuantity == maxTicketsByUserPerRoundForThisRaffleType) {
					jsonObject(
						"status" to LoraffleCommand.BuyRaffleTicketStatus.THRESHOLD_EXCEEDED.toString()
					)
				} else {
					jsonObject(
						"status" to LoraffleCommand.BuyRaffleTicketStatus.TOO_MANY_TICKETS.toString(),
						"ticketCount" to currentUserTicketQuantity
					)
				}
			}

			val requiredCount = quantity.toLong() * RaffleType.LORITTA.ticketPrice
			logger.info("$userId irá comprar $quantity tickets por ${requiredCount}!")

			val lorittaProfile = loritta.getOrCreateLorittaProfile(userId)

			if (lorittaProfile.money >= requiredCount) {
				lorittaProfile.takeSonhosAndAddToTransactionLogNested(
					requiredCount,
					SonhosPaymentReason.RAFFLE
				)

				val transactionLogId = SonhosTransactionsLog.insertAndGetId {
					it[user] = userId
					it[timestamp] = Instant.now()
				}

				RaffleTicketsSonhosTransactionsLog.insert {
					it[timestampLog] = transactionLogId
					it[sonhos] = requiredCount
					it[raffle] = currentRaffle[Raffles.id]
					it[ticketQuantity] = quantity.toLong()
				}

				val now = Instant.now()

				// By using shouldReturnGeneratedValues, the database won't need to synchronize on each insert
				// this increases insert performance A LOT and, because we don't need the IDs, it is very useful to make
				// tickets purchases be VERY fast
				RaffleTickets.batchInsert(0 until quantity, shouldReturnGeneratedValues = false) {
					this[RaffleTickets.userId] = userId
					this[RaffleTickets.raffle] = currentRaffle[Raffles.id]
					this[RaffleTickets.boughtAt] = now
				}

				logger.info { "$userId bought $quantity tickets for ${requiredCount}! (Before they had ${lorittaProfile.money + requiredCount}) sonhos!)" }

				return@transaction jsonObject(
					"status" to LoraffleCommand.BuyRaffleTicketStatus.SUCCESS.toString()
				)
			} else {
				return@transaction jsonObject(
					"status" to LoraffleCommand.BuyRaffleTicketStatus.NOT_ENOUGH_MONEY.toString(),
					"canOnlyPay" to requiredCount - lorittaProfile.money
				)
			}
		}

		call.respondJson(response)
	}
}