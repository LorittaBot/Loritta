package net.perfectdreams.loritta.morenitta.website.routes

import net.perfectdreams.loritta.common.locale.BaseLocale
import io.ktor.server.application.*
import io.ktor.server.response.*
import net.perfectdreams.loritta.morenitta.LorittaBot

class CommandsRoute(loritta: LorittaBot) : LocalizedRoute(loritta, "/commands") {
	override val isMainClusterOnlyRoute = true

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		call.respondText("Se você está vendo isso, quer dizer que alguma configuração deu problema!")
	}
}