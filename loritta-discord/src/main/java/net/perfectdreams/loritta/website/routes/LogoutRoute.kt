package net.perfectdreams.loritta.website.routes

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.application.ApplicationCall
import io.ktor.request.host
import io.ktor.response.respondRedirect
import io.ktor.sessions.clear
import io.ktor.sessions.sessions
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession

class LogoutRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/logout") {
	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		val hostHeader = call.request.host()
		call.sessions.clear<LorittaJsonWebSession>()
		call.respondRedirect("https://$hostHeader/", true)
	}
}