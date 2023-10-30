package net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta

import io.ktor.server.application.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.RaffleTickets
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.Raffles
import net.perfectdreams.loritta.common.utils.RaffleType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.serializable.RaffleStatus
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.countDistinct
import org.jetbrains.exposed.sql.select
import java.sql.Connection

class GetRaffleStatusRoute(loritta: LorittaBot) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/loritta/raffle") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val raffleType = call.parameters["type"]?.let { RaffleType.valueOf(it) } ?: RaffleType.ORIGINAL

		val raffleStatus = loritta.transaction(transactionIsolation = Connection.TRANSACTION_SERIALIZABLE) {
			// Get current active raffle based on the selected raffle type
			val currentRaffle = Raffles.select {
				Raffles.endedAt.isNull() and (Raffles.raffleType eq raffleType)
			}.orderBy(Raffles.endsAt, SortOrder.DESC)
				.limit(1)
				.first()

			val previousRaffle = Raffles.select {
				Raffles.endedAt.isNotNull() and (Raffles.raffleType eq raffleType)
			}.orderBy(Raffles.endedAt, SortOrder.DESC)
				.limit(1)
				.firstOrNull()

			val currentTickets = RaffleTickets.select {
				RaffleTickets.raffle eq currentRaffle[Raffles.id]
			}.count()

			val countUserDistinct = RaffleTickets.userId.countDistinct()
			val totalUsersInTheRaffle = RaffleTickets.slice(countUserDistinct).select { RaffleTickets.raffle eq currentRaffle[Raffles.id] }
				.first()[countUserDistinct]

			if (previousRaffle != null) {
				val winnerTicketId = previousRaffle[Raffles.winnerTicket]

				if (winnerTicketId != null) {
					val previousRaffleWinnerTicket = RaffleTickets.select {
						RaffleTickets.id eq winnerTicketId
					}.limit(1)
						.first()

					val prize = previousRaffle[Raffles.paidOutPrize]
					val prizeAfterTax = previousRaffle[Raffles.paidOutPrizeAfterTax]
					val userId = previousRaffleWinnerTicket[RaffleTickets.userId]

					return@transaction RaffleStatus(
						userId,
						currentTickets.toInt(),
						totalUsersInTheRaffle.toInt(),
						currentRaffle[Raffles.endsAt].toEpochMilli(),
						prize,
						prizeAfterTax,
						currentRaffle[Raffles.id].value
					)
				}
			}

			return@transaction RaffleStatus(
				null,
				currentTickets.toInt(),
				totalUsersInTheRaffle.toInt(),
				currentRaffle[Raffles.endsAt].toEpochMilli(),
				null,
				null,
				currentRaffle[Raffles.id].value
			)
		}

		call.respondJson(Json.encodeToString(raffleStatus))
	}
}