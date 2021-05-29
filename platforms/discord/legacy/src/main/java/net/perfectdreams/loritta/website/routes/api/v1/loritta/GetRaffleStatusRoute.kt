package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.threads.RaffleThread
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class GetRaffleStatusRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/loritta/raffle") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val jsonObject = jsonObject(
				"lastWinnerId" to RaffleThread.lastWinnerId,
				"lastWinnerPrize" to RaffleThread.lastWinnerPrize,
				"currentTickets" to RaffleThread.userIds.size,
				"usersParticipating" to RaffleThread.userIds.distinctBy { it.first }.size,
				"started" to RaffleThread.started
		)

		call.respondJson(jsonObject)
	}
}