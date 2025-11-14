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
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import java.sql.Timestamp
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
				val currentUserTicketQuantity = RaffleTickets.selectAll().where {
					RaffleTickets.raffle eq currentRaffle[Raffles.id] and (RaffleTickets.userId eq userId)
				}.count()

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
					val sqlTimestampOfNow = Timestamp.from(now)

					// This is even a BETTER optimization: This... is to go... even further beyond! https://youtu.be/8TGalu36BHA
					// Because we are always inserting the same data over and over, we can go even further beyond!
					// Instead of using batch inserts and inserting the same data, we can use generate_series!
					val jdbcConnection = (this.connection as JdbcConnectionImpl).connection

					// The generate_series mean that $quantity values will be inserted into the database!
					// Yes, we need to use SELECT instead of VALUES
					val insertSQL = "INSERT INTO ${RaffleTickets.tableName} (\"${RaffleTickets.userId.name}\", \"${RaffleTickets.raffle.name}\", \"${RaffleTickets.boughtAt.name}\") SELECT ?, ?, ? FROM generate_series(1, $quantity)"

					val preparedStatement = jdbcConnection.prepareStatement(insertSQL)
					preparedStatement.setLong(1, userId)
					preparedStatement.setLong(2, currentRaffle[Raffles.id].value)
					preparedStatement.setTimestamp(3, sqlTimestampOfNow)

					// Execute the statement
					preparedStatement.execute()

					// This is an optimization: batchInsert ends up using a LOT of memory because it keeps everything in the "data" ArrayList
					// For this, this is unviable, because if someone buys a lot of tickets (800k+), Loritta crashes and burns (uses 500MB+ memory!) trying to process it
					// As a workaround, we will fully skip the batchInsert and manually insert the data via JDBC
					// val jdbcConnection = (this.connection as JdbcConnectionImpl).connection

					// val insertSQL = "INSERT INTO ${RaffleTickets.tableName} (\"${RaffleTickets.userId.name}\", \"${RaffleTickets.raffle.name}\", \"${RaffleTickets.boughtAt.name}\") VALUES (?, ?, ?)"
					// val preparedStatement = jdbcConnection.prepareStatement(insertSQL)

					// repeat(quantity) {
					// 	  // Add batch data
					// 	  preparedStatement.setLong(1, userId)
					// 	  preparedStatement.setLong(2, currentRaffle[Raffles.id].value)
					// 	  preparedStatement.setTimestamp(3, sqlTimestampOfNow)
					// 	  preparedStatement.addBatch()
					// }

					// Execute the batch insert
					// preparedStatement.executeBatch()

					// By using shouldReturnGeneratedValues, the database won't need to synchronize on each insert
					// this increases insert performance A LOT and, because we don't need the IDs, it is very useful to make
					// tickets purchases be VERY fast
					// RaffleTickets.batchInsert(0 until quantity, shouldReturnGeneratedValues = false) {
					// 	  this[RaffleTickets.userId] = userId
					// 	  this[RaffleTickets.raffle] = currentRaffle[Raffles.id]
					// 	  this[RaffleTickets.boughtAt] = now
					// }

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