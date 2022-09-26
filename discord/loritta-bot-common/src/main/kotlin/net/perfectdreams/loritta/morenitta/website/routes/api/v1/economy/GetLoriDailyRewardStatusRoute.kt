package net.perfectdreams.loritta.morenitta.website.routes.api.v1.economy

import com.github.salomonbrys.kotson.jsonObject
import net.perfectdreams.loritta.morenitta.Loritta
import io.ktor.server.application.ApplicationCall
import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.website.utils.extensions.trueIp
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class GetLoriDailyRewardStatusRoute(loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/economy/daily-reward-status") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		loritta as Loritta
		val ip = call.request.trueIp

		val userIdentification = discordAuth.getUserIdentification()

		GetLoriDailyRewardRoute.verifyIfAccountAndIpAreSafe(userIdentification, ip)
		val receivedDailyWithSameIp = GetLoriDailyRewardRoute.checkIfUserCanPayout(userIdentification, ip)

		val payload = jsonObject(
				"receivedDailyWithSameIp" to receivedDailyWithSameIp
		)

		call.respondJson(payload)
	}
}