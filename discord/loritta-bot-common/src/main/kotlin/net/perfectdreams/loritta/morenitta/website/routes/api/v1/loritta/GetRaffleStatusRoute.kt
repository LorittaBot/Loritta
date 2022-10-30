package net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.jsonObject
import net.perfectdreams.loritta.morenitta.threads.RaffleThread
import io.ktor.server.application.*
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson

class GetRaffleStatusRoute(loritta: LorittaBot) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/loritta/raffle") {
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