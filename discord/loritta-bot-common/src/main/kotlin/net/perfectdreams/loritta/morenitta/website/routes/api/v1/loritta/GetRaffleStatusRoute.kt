package net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta

import io.ktor.server.application.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.RaffleTickets
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.Raffles
import net.perfectdreams.loritta.common.utils.RaffleType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

class GetRaffleStatusRoute(loritta: LorittaBot) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/loritta/raffle") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val raffleType = call.parameters["type"]?.let { RaffleType.valueOf(it) } ?: RaffleType.ORIGINAL

		val response = loritta.transaction {
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
			}.toList()

			if (previousRaffle != null) {
				val winnerTicketId = previousRaffle[Raffles.winnerTicket]

				if (winnerTicketId != null) {
					val previousRaffleWinnerTicket = RaffleTickets.select {
						RaffleTickets.id eq winnerTicketId
					}.limit(1)
						.first()

					val prize = previousRaffle[Raffles.paidOutPrize]
					val userId = previousRaffleWinnerTicket[RaffleTickets.userId]

					return@transaction buildJsonObject {
						put("lastWinnerId", userId)
						put("lastWinnerPrize", prize)
						put("currentTickets", currentTickets.size)
						put("usersParticipating", currentTickets.map { it[RaffleTickets.userId] }.distinct().size)
						put("endsAt", currentRaffle[Raffles.endsAt].toEpochMilli())
					}
				}
			}

			return@transaction buildJsonObject {
				put("lastWinnerId", null)
				put("lastWinnerPrize", 0)
				put("currentTickets", currentTickets.size)
				put("usersParticipating", currentTickets.map { it[RaffleTickets.userId] }.distinct().size)
				put("endsAt", currentRaffle[Raffles.endsAt].toEpochMilli())
			}
		}

		call.respondJson(response)
	}
}