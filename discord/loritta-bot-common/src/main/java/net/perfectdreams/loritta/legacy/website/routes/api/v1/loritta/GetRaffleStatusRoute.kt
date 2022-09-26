package net.perfectdreams.loritta.legacy.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.jsonObject
import net.perfectdreams.loritta.legacy.threads.RaffleThread
import io.ktor.server.application.*
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.legacy.website.utils.extensions.respondJson

class GetRaffleStatusRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/loritta/raffle") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val jsonObject = jsonObject(
				"lastWinnerId" to RaffleThread.lastWinnerId,
				"lastWinnerPrize" to RaffleThread.lastWinnerPrize,
				"currentTickets" to RaffleThread.userIds.size,
				"usersParticipating" to RaffleThread.userIds.distinctBy { it }.size,
				"started" to RaffleThread.started
		)

		call.respondJson(jsonObject)
	}
}