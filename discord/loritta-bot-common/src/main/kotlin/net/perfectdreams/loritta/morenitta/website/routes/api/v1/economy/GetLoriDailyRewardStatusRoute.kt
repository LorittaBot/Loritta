package net.perfectdreams.loritta.morenitta.website.routes.api.v1.economy

import com.github.salomonbrys.kotson.jsonObject
import io.ktor.server.application.ApplicationCall
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.website.utils.extensions.trueIp
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class GetLoriDailyRewardStatusRoute(loritta: LorittaBot) :
    RequiresAPIDiscordLoginRoute(loritta, "/api/v1/economy/daily-reward-status") {
    override suspend fun onAuthenticatedRequest(
        call: ApplicationCall,
        discordAuth: TemmieDiscordAuth,
        userIdentification: LorittaJsonWebSession.UserIdentification
    ) {
        loritta as LorittaBot
        val ip = call.request.trueIp

        val userIdentification = discordAuth.getUserIdentification()

        GetLoriDailyRewardRoute.verifyIfAccountAndIpAreSafe(loritta, userIdentification, ip)
        val receivedDailyWithSameIp = GetLoriDailyRewardRoute.checkIfUserCanPayout(loritta, userIdentification, ip)

        val payload = jsonObject(
            "receivedDailyWithSameIp" to receivedDailyWithSameIp
        )

        call.respondJson(payload)
    }
}