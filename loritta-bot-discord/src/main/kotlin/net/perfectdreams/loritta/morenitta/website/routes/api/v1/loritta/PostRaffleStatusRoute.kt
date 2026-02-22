package net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.RaffleTickets
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.Raffles
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.RaffleType
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.RaffleCommand
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.serializable.SonhosPaymentReason
import net.perfectdreams.loritta.serializable.StoredRaffleTicketsTransaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import java.time.Instant


class PostRaffleStatusRoute(loritta: LorittaBot) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/loritta/raffle") {
	companion object {
		val logger by HarmonyLoggerFactory.logger {}
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val json = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()).obj }

		val userId = json["userId"].long
		val quantity = json["quantity"].int
		val localeId = json["localeId"].string
		val invokedAt = Instant.ofEpochMilli(json["invokedAt"].long)
		val type = RaffleType.valueOf(json["type"].string)

		// Before, we used TRANSACTION_SERIALIZABLE because REPEATABLE READ will cause issues if someone buys raffle tickets when the LorittaRaffleTask is processing the current raffle winners
		// However, SERIALIZABLE is also a bit bad since we need to fully run the transaction separetely from everything
		// To workaround this, we will use a coroutine mutex, yay!
		// This way, we don't block all transactions, while still letting other transactions work
		val response = loritta.raffleResultsMutex.withLock {
			loritta.transaction {
				// The "invokedAt" is used to only get raffles triggered WHEN the user used the command
				// This way it avoids issues when Loritta took too long to receive this request, which would cause Loritta to get the new raffle instead of the "current-now-old" raffle.
				val currentRaffle = Raffles.selectAll().where {
					Raffles.raffleType eq type and (Raffles.endedAt.isNull()) and (Raffles.endsAt greaterEq invokedAt) and (Raffles.startedAt lessEq invokedAt)
				}.firstOrNull()

				// The raffle hasn't been created yet! The LorittaRaffleTask on the main instance should *hopefully* create the new raffle soon...
				if (currentRaffle == null) {
					return@transaction jsonObject(
						"status" to RaffleCommand.BuyRaffleTicketStatus.STALE_RAFFLE_DATA.toString()
					)
				}

				// Get how many tickets the user has in the current raffle
				val userTicketsSum = RaffleTickets.boughtTickets.sum()
				val currentUserTicketQuantity = RaffleTickets.select(userTicketsSum).where {
					RaffleTickets.raffle eq currentRaffle[Raffles.id] and (RaffleTickets.userId eq userId)
				}.first()[userTicketsSum] ?: 0L

				val maxTicketsByUserPerRoundForThisRaffleType = currentRaffle[Raffles.raffleType].maxTicketsByUserPerRound.toLong()

				if (currentUserTicketQuantity + quantity > maxTicketsByUserPerRoundForThisRaffleType) {
					return@transaction if (currentUserTicketQuantity == maxTicketsByUserPerRoundForThisRaffleType) {
						jsonObject(
							"status" to RaffleCommand.BuyRaffleTicketStatus.THRESHOLD_EXCEEDED.toString()
						)
					} else {
						jsonObject(
							"status" to RaffleCommand.BuyRaffleTicketStatus.TOO_MANY_TICKETS.toString(),
							"ticketCount" to currentUserTicketQuantity
						)
					}
				}

				val requiredCount = quantity.toLong() * type.ticketPrice
				logger.info { "$userId irá comprar $quantity tickets por ${requiredCount}!" }

				val lorittaProfile = loritta.getOrCreateLorittaProfile(userId)

				if (lorittaProfile.money >= requiredCount) {
					lorittaProfile.takeSonhosAndAddToTransactionLogNested(
						requiredCount,
						SonhosPaymentReason.RAFFLE
					)

					// Cinnamon transaction log
					SimpleSonhosTransactionsLogUtils.insert(
						userId,
						Instant.now(),
						TransactionType.RAFFLE,
						requiredCount,
						StoredRaffleTicketsTransaction(currentRaffle[Raffles.id].value, quantity)
					)

					val now = Instant.now()

					// Instead of inserting N rows (one per ticket), we insert a single row with boughtTickets = quantity.
					// This drastically reduces disk usage (e.g., 1 row instead of 800k+ rows for a single purchase).
					RaffleTickets.insert {
						it[RaffleTickets.userId] = userId
						it[RaffleTickets.raffle] = currentRaffle[Raffles.id]
						it[RaffleTickets.boughtAt] = now
						it[RaffleTickets.boughtTickets] = quantity.toLong()
					}

					logger.info { "$userId bought $quantity tickets for ${requiredCount}! (Before they had ${lorittaProfile.money + requiredCount}) sonhos!)" }

					return@transaction jsonObject(
						"status" to RaffleCommand.BuyRaffleTicketStatus.SUCCESS.toString()
					)
				} else {
					return@transaction jsonObject(
						"status" to RaffleCommand.BuyRaffleTicketStatus.NOT_ENOUGH_MONEY.toString(),
						"canOnlyPay" to requiredCount - lorittaProfile.money
					)
				}
			}
		}

		call.respondJson(response)
	}
}