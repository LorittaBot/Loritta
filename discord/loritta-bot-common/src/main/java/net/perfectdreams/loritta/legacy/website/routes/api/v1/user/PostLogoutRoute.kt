package net.perfectdreams.loritta.legacy.website.routes.api.v1.user

import com.github.salomonbrys.kotson.jsonObject
import io.ktor.server.application.ApplicationCall
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.sessions
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.loritta.legacy.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.legacy.website.utils.extensions.respondJson

class PostLogoutRoute(val loritta: LorittaDiscord) : BaseRoute("/api/v1/users/@me/logout") {
	override suspend fun onRequest(call: ApplicationCall) {
		call.sessions.clear<LorittaJsonWebSession>()
		call.respondJson(jsonObject())
	}
}