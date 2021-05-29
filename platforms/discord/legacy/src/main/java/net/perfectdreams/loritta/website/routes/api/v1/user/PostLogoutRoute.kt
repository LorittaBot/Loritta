package net.perfectdreams.loritta.website.routes.api.v1.user

import com.github.salomonbrys.kotson.jsonObject
import io.ktor.application.ApplicationCall
import io.ktor.sessions.clear
import io.ktor.sessions.sessions
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class PostLogoutRoute(val loritta: LorittaDiscord) : BaseRoute("/api/v1/users/@me/logout") {
	override suspend fun onRequest(call: ApplicationCall) {
		call.sessions.clear<LorittaJsonWebSession>()
		call.respondJson(jsonObject())
	}
}